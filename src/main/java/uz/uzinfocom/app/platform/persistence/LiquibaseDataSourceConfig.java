package uz.uzinfocom.app.platform.persistence;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.boot.liquibase.autoconfigure.LiquibaseDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.regex.Pattern;

/**
 * Gives Liquibase its own small, short-lived connection pool instead of
 * borrowing from the main {@code IsemidHikariPool}.
 *
 * <p>A first-run migration against an empty schema legitimately holds one
 * connection for minutes while it streams the {@code base/*.csv} seeds
 * (~12k professions, ~9k neighborhoods). On the main pool that trips
 * {@code spring.datasource.hikari.leak-detection-threshold=60000} and logs a
 * scary — but false — "Apparent connection leak detected" WARN on the
 * {@code restartedMain} thread. This dedicated pool disables leak detection
 * (migrations are the one place a multi-minute borrow is expected) and is
 * closed as soon as Liquibase finishes, so it costs nothing at runtime.
 *
 * <p>Connection settings (URL — including {@code currentSchema}, credentials,
 * driver) are inherited from {@code spring.datasource.*}; the
 * {@code spring.liquibase.default-schema}/{@code liquibase-schema} overrides
 * still apply on top. The {@code hikari.*} tuning on the main pool
 * (init SQL, sizing) is deliberately not copied — Liquibase does not need it.
 */
@Configuration
public class LiquibaseDataSourceConfig {

    private static final Pattern SCHEMA_NAME = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");

    @Bean
    @LiquibaseDataSource
    public DataSource liquibaseDataSource(DataSourceProperties properties,
                                          @Value("${spring.liquibase.default-schema:}") String defaultSchema) {
        HikariDataSource dataSource = properties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
        dataSource.setPoolName("IsemidLiquibasePool");
        dataSource.setMaximumPoolSize(2);
        dataSource.setMinimumIdle(0);
        // 0 = off: a migration holding a connection for the length of a bulk
        // CSV load is expected, not a leak.
        dataSource.setLeakDetectionThreshold(0);
        // Liquibase qualifies the objects it generates (createTable, insert, ...)
        // with default-schema, but hand-written SQL (<sql>, sqlCheck preconditions)
        // is sent as-is and resolves through search_path. If DB_URL lacks
        // currentSchema, that SQL would silently run against `public` (legacy data)
        // while the tables live in the default schema. Pin it so it cannot.
        if (StringUtils.hasText(defaultSchema)) {
            if (!SCHEMA_NAME.matcher(defaultSchema).matches()) {
                throw new IllegalStateException("Invalid spring.liquibase.default-schema: " + defaultSchema);
            }
            dataSource.setConnectionInitSql("SET search_path TO " + defaultSchema);
        }
        return dataSource;
    }
}
