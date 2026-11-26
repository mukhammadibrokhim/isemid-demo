package uz.uzinfocom.app.modules.report.shared;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.Field;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@code age()} reports year 0 for a birth date a few months after the case date,
 * so every age the catalog-driven reports compute must be guarded.
 */
class CatalogReportAgeGuardTest {

    private static final Pattern AGE = Pattern.compile("extract\\((?:year|month) from age\\(");

    private static final Pattern GUARDED_AGE = Pattern.compile(
            "case when p\\.birth_date <= f\\.created_at::date then extract\\((?:year|month) from age\\("
    );

    @ParameterizedTest
    @ValueSource(strings = {"12", "13", "281", "282"})
    void everyAgeInTheSourceQueriesIsGuarded(String form) throws Exception {
        Class<?> repository = Class.forName("uz.uzinfocom.app.modules.report.form" + form
                + ".infrastructure.persistence.repository.Form" + form + "ReportRepository");

        for (String name : new String[]{"UNION_SOURCE_TEMPLATE", "UNION_SOURCE_WITH_ORG_TEMPLATE"}) {
            Field field = repository.getDeclaredField(name);
            field.setAccessible(true);
            String sql = (String) field.get(null);

            long ages = AGE.matcher(sql).results().count();
            assertThat(ages).as(name).isPositive();
            assertThat(GUARDED_AGE.matcher(sql).results().count()).as(name).isEqualTo(ages);
        }
    }
}
