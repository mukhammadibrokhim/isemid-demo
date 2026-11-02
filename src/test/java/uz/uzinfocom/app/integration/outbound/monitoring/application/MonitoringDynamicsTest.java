package uz.uzinfocom.app.integration.outbound.monitoring.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import uz.uzinfocom.app.integration.outbound.monitoring.infrastructure.MonitoringQueryRepository;
import uz.uzinfocom.app.integration.outbound.monitoring.web.dto.MonitoringDynamicsResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MonitoringDynamicsTest {

    private static final ZonedDateTime NOW =
            ZonedDateTime.of(2026, 11, 2, 14, 30, 0, 0, MonitoringDynamicsWindow.ZONE);

    @Test
    void granularityFollowsTheRangeLength() {
        assertThat(MonitoringGranularity.forRange(0)).isEqualTo(MonitoringGranularity.HOUR);
        assertThat(MonitoringGranularity.forRange(1)).isEqualTo(MonitoringGranularity.HOUR);
        assertThat(MonitoringGranularity.forRange(2)).isEqualTo(MonitoringGranularity.DAY);
        assertThat(MonitoringGranularity.forRange(30)).isEqualTo(MonitoringGranularity.DAY);
        assertThat(MonitoringGranularity.forRange(31)).isEqualTo(MonitoringGranularity.WEEK);
        assertThat(MonitoringGranularity.forRange(180)).isEqualTo(MonitoringGranularity.WEEK);
        assertThat(MonitoringGranularity.forRange(181)).isEqualTo(MonitoringGranularity.MONTH);
    }

    @Test
    void pastSingleDayIsTwentyFourHourBuckets() {
        LocalDate day = LocalDate.of(2026, 10, 1);

        MonitoringDynamicsWindow window = MonitoringDynamicsWindow.of(day, day, NOW);

        assertThat(window.granularity()).isEqualTo(MonitoringGranularity.HOUR);
        assertThat(window.seriesStart()).isEqualTo(LocalDateTime.of(2026, 10, 1, 0, 0));
        assertThat(window.seriesEnd()).isEqualTo(LocalDateTime.of(2026, 10, 1, 23, 0));
    }

    @Test
    void todayStopsAtTheCurrentHour() {
        LocalDate today = LocalDate.of(2026, 11, 2);

        MonitoringDynamicsWindow window = MonitoringDynamicsWindow.of(today, today, NOW);

        assertThat(window.seriesEnd()).isEqualTo(LocalDateTime.of(2026, 11, 2, 14, 0));
        assertThat(window.toExclusive()).isEqualTo(NOW.toInstant());
    }

    @Test
    void daySeriesEndsOnTheLastRequestedDay() {
        MonitoringDynamicsWindow window = MonitoringDynamicsWindow.of(
                LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 10), NOW);

        assertThat(window.granularity()).isEqualTo(MonitoringGranularity.DAY);
        assertThat(window.seriesStart()).isEqualTo(LocalDateTime.of(2026, 10, 1, 0, 0));
        assertThat(window.seriesEnd()).isEqualTo(LocalDateTime.of(2026, 10, 10, 0, 0));
    }

    @Test
    void weekSeriesIsAnchoredAtFromDateInSevenDaySteps() {
        LocalDate from = LocalDate.of(2026, 7, 1);

        MonitoringDynamicsWindow window = MonitoringDynamicsWindow.of(from, LocalDate.of(2026, 8, 20), NOW);

        // 50 days after from -> 7 full steps (49 days) -> bucket starting 2026-08-19
        assertThat(window.granularity()).isEqualTo(MonitoringGranularity.WEEK);
        assertThat(window.weekAnchor()).isEqualTo(from);
        assertThat(window.seriesStart()).isEqualTo(LocalDateTime.of(2026, 7, 1, 0, 0));
        assertThat(window.seriesEnd()).isEqualTo(LocalDateTime.of(2026, 8, 19, 0, 0));
    }

    @Test
    void monthSeriesStartsAtTheFirstOfTheMonths() {
        MonitoringDynamicsWindow window = MonitoringDynamicsWindow.of(
                LocalDate.of(2026, 1, 15), LocalDate.of(2026, 10, 20), NOW);

        assertThat(window.granularity()).isEqualTo(MonitoringGranularity.MONTH);
        assertThat(window.seriesStart()).isEqualTo(LocalDateTime.of(2026, 1, 1, 0, 0));
        assertThat(window.seriesEnd()).isEqualTo(LocalDateTime.of(2026, 10, 1, 0, 0));
    }

    @Test
    void rangeEndingInTheFutureIsClampedToNow() {
        MonitoringDynamicsWindow window = MonitoringDynamicsWindow.of(
                LocalDate.of(2026, 10, 25), LocalDate.of(2026, 12, 31), NOW);

        assertThat(window.toExclusive()).isEqualTo(NOW.toInstant());
    }

    @Test
    void rangeStartingInTheFutureIsRejected() {
        LocalDate future = LocalDate.of(2026, 11, 3);

        assertThatThrownBy(() -> MonitoringDynamicsWindow.of(future, future, NOW))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void foldsRowsPerPeriodAndLocationAndKeepsEmptyPeriods() {
        MonitoringDynamicsResponseMapper mapper = new MonitoringDynamicsResponseMapper();
        List<MonitoringDynamicsRow> rows = List.of(
                // 2026-10-01 / 1726: two blocks plus an undiagnosed case
                new MonitoringDynamicsRow("2026-10-01", "1726", "A00-A09", 1, 1, 1, 1, 1, 0, 0, 0, 1, 0, 0, 0),
                new MonitoringDynamicsRow("2026-10-01", "1726", "(B15-B19)", 1, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0),
                new MonitoringDynamicsRow("2026-10-01", "1726", null, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0),
                // empty period
                new MonitoringDynamicsRow("2026-10-02", null, null, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
        );

        MonitoringDynamicsResponse response = mapper.toResponse(LocalDate.of(2026, 10, 2), rows);

        assertThat(response.toDate()).isEqualTo(LocalDate.of(2026, 10, 2));
        assertThat(response.report()).hasSize(2);

        MonitoringDynamicsResponse.Item first = response.report().getFirst();
        assertThat(first.period()).isEqualTo("2026-10-01");
        assertThat(first.locationCode()).isEqualTo("1726");
        assertThat(first.totalCases()).isEqualTo(4);
        assertThat(first.confirmed()).isEqualTo(2);
        assertThat(first.unconfirmed()).isEqualTo(2);
        assertThat(first.male()).isEqualTo(2);
        assertThat(first.female()).isEqualTo(2);
        assertThat(first.ageGroups().age_0_3()).isEqualTo(1);
        assertThat(first.ageGroups().age_18_25()).isEqualTo(1);
        assertThat(first.ageGroups().age_25_45()).isEqualTo(1);
        assertThat(first.ageGroups().age_45_65()).isEqualTo(1);
        assertThat(first.diseaseType())
                .hasSize(2)
                .containsEntry("(A00-A09)", 2)
                .containsEntry("(B15-B19)", 1);

        MonitoringDynamicsResponse.Item empty = response.report().get(1);
        assertThat(empty.locationCode()).isNull();
        assertThat(empty.totalCases()).isZero();
        assertThat(empty.diseaseType()).isEmpty();
    }

    @Test
    void serviceNormalizesFiltersAndPassesTheWindow() {
        MonitoringQueryRepository repository = mock(MonitoringQueryRepository.class);
        when(repository.findDynamics(any())).thenReturn(List.of());
        MonitoringQueryService service = new MonitoringQueryService(
                repository, new MonitoringResponseMapper(), new MonitoringDynamicsResponseMapper());

        service.getDynamics(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 10), " 1726 ", "  ", " (a00) ");

        ArgumentCaptor<MonitoringDynamicsQuery> captor = ArgumentCaptor.forClass(MonitoringDynamicsQuery.class);
        verify(repository).findDynamics(captor.capture());
        MonitoringDynamicsQuery query = captor.getValue();
        assertThat(query.regionCode()).isEqualTo("1726");
        assertThat(query.hasDistrict()).isFalse();
        assertThat(query.diagnosisCode()).isEqualTo("A00");
        assertThat(query.fallbackLocation()).isEqualTo("1726");
        assertThat(query.window().granularity()).isEqualTo(MonitoringGranularity.DAY);
    }

    @Test
    void districtTakesOverTheFallbackLocation() {
        MonitoringDynamicsQuery query = new MonitoringDynamicsQuery(null, "1726", "1726264", null);

        assertThat(query.fallbackLocation()).isEqualTo("1726264");
    }
}
