package uz.uzinfocom.isemid.features.report.repository;

import java.sql.Types;
import java.time.LocalDate;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.isemid.features.report.dtos.response.LocationReportRow;
import uz.uzinfocom.isemid.features.report.service.scope.ReportScope;

/**
 * Native, set-based location report.
 *
 * <p>The {@code cls_catalog} dimension drives the result, so every location of the target
 * granularity yields a row even with zero cases (no Java-side merge). Localized name,
 * confirmed/unconfirmed split, the sanepid visibility matrix, and the grand total are all
 * computed inside PostgreSQL in a single round-trip. No entities are materialized.</p>
 *
 */
@Repository
@Slf4j
public class LocationReportRepository {

    private final NamedParameterJdbcTemplate template;

    public LocationReportRepository(NamedParameterJdbcTemplate template) {
        this.template = template;
    }

    /**
     * @param toDate exclusive upper bound (half-open interval {@code [fromDate, toDate)})
     */
    public List<LocationReportRow> fetch(
            String mkb10Code,
            LocalDate fromDate,
            LocalDate toDate,
            ReportScope scope,
            String locale) {

        log.info("Fetching location reports for {}", scope);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("locale", locale, Types.VARCHAR)
                .addValue("fromDate", fromDate, Types.DATE)
                .addValue("toDate", toDate, Types.DATE)
                .addValue("mkb10Code", mkb10Code, Types.VARCHAR)
                .addValue("parentCode", scope.parentCode(), Types.VARCHAR)
                .addValue("currentOrganizationId", scope.currentOrganizationId())
                .addValue("hasPermission", scope.hasPermission(), Types.BOOLEAN);

        return template.query(scope.specification(), params, (rs, row) -> new LocationReportRow(
                rs.getString("code"),
                rs.getString("name"),
                rs.getInt("confirmed"),
                rs.getInt("unconfirmed"),
                rs.getInt("is_total") == 1));
    }
}
