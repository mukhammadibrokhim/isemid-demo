package uz.uzinfocom.app.integration.outbound.monitoring.infrastructure;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import uz.uzinfocom.app.integration.outbound.monitoring.application.MonitoringDynamicsQuery;
import uz.uzinfocom.app.integration.outbound.monitoring.application.MonitoringDynamicsWindow;
import uz.uzinfocom.app.integration.outbound.monitoring.application.MonitoringFilter;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * No database here: checks that the generated SQL is well-formed text (no
 * leftover format specifiers) and that every named parameter it mentions is
 * bound - and nothing else is - for each filter / granularity combination.
 */
class MonitoringQueryRepositoryTest {

    private static final Pattern NAMED_PARAMETER = Pattern.compile("(?<!:):([a-zA-Z][a-zA-Z0-9]*)");
    private static final ZonedDateTime NOW =
            ZonedDateTime.of(2026, 11, 2, 14, 30, 0, 0, MonitoringDynamicsWindow.ZONE);

    private final EntityManager entityManager = mock(EntityManager.class);
    private final Query nativeQuery = mock(Query.class);
    private final MonitoringQueryRepository repository = new MonitoringQueryRepository(entityManager);

    private Captured run(Runnable call) {
        when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);
        when(nativeQuery.setParameter(anyString(), org.mockito.ArgumentMatchers.any())).thenReturn(nativeQuery);
        when(nativeQuery.getResultList()).thenReturn(List.of());

        call.run();

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(entityManager).createNativeQuery(sql.capture());
        ArgumentCaptor<String> names = ArgumentCaptor.forClass(String.class);
        verify(nativeQuery, org.mockito.Mockito.atLeast(0)).setParameter(names.capture(), org.mockito.ArgumentMatchers.any());
        return new Captured(sql.getValue(), new HashSet<>(names.getAllValues()));
    }

    private record Captured(String sql, Set<String> boundNames) {
        void assertWellFormed() {
            assertThat(sql).doesNotContain("%").doesNotContain("$s");
            Set<String> used = new HashSet<>();
            Matcher matcher = NAMED_PARAMETER.matcher(sql);
            while (matcher.find()) {
                used.add(matcher.group(1));
            }
            assertThat(boundNames).isEqualTo(used);
        }
    }

    @Test
    void locationStatisticsSqlBindsExactlyItsParameters() {
        for (String region : new String[]{null, "1726"}) {
            for (String diagnosis : new String[]{null, "A00-A09"}) {
                var filter = new MonitoringFilter(
                        LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), region, diagnosis);
                reset();
                run(() -> repository.findLocationStatistics(filter)).assertWellFormed();
            }
        }
    }

    @Test
    void diseaseStatisticsSqlBindsExactlyItsParameters() {
        for (String region : new String[]{null, "1726"}) {
            for (String diagnosis : new String[]{null, "A00-A09"}) {
                var filter = new MonitoringFilter(
                        LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), region, diagnosis);
                reset();
                run(() -> repository.findDiseaseStatistics(filter)).assertWellFormed();
            }
        }
    }

    @Test
    void dynamicsSqlBindsExactlyItsParametersForEveryGranularityAndFilter() {
        LocalDate[][] ranges = {
                {LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 1)},   // hour
                {LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 20)},  // day
                {LocalDate.of(2026, 7, 1), LocalDate.of(2026, 9, 1)},     // week
                {LocalDate.of(2026, 1, 1), LocalDate.of(2026, 10, 20)}    // month
        };
        String[][] filters = {
                {null, null, null},
                {"1726", null, null},
                {"1726", "1726264", "A00-A09"},
                {null, null, "A00"}
        };
        for (LocalDate[] range : ranges) {
            for (String[] f : filters) {
                var query = new MonitoringDynamicsQuery(
                        MonitoringDynamicsWindow.of(range[0], range[1], NOW), f[0], f[1], f[2]);
                reset();
                Captured captured = run(() -> repository.findDynamics(query));
                captured.assertWellFormed();
                assertThat(captured.sql()).contains("generate_series");
            }
        }
    }

    private void reset() {
        org.mockito.Mockito.reset(entityManager, nativeQuery);
    }
}
