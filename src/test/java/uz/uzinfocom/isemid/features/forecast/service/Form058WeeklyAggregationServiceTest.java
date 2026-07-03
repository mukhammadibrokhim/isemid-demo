package uz.uzinfocom.isemid.features.forecast.service;

import org.junit.jupiter.api.Test;
import uz.uzinfocom.isemid.features.forecast.repositories.Form058ForecastRepository;
import uz.uzinfocom.isemid.features.forecast.service.Form058WeeklyAggregationService.WeekSeriesPoint;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class Form058WeeklyAggregationServiceTest {

    private final Form058ForecastRepository repo = mock(Form058ForecastRepository.class);
    private final Form058WeeklyAggregationService service =
            new Form058WeeklyAggregationService(repo, new WeekBucketService());

    @Test
    void earliestRecordAfterRequestedStart_clampsLeadingBuckets() {
        when(repo.earliestRecordDate(any())).thenReturn(LocalDate.of(2024, 1, 15));
        when(repo.countCreatedByDay(any(), any(), any(), any())).thenReturn(List.of());

        var result = service.weeklyCounts(null, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 28), null, null);

        // no synthetic zero weeks before Jan 15, even though the caller asked for Jan 1
        assertThat(result).extracting(WeekSeriesPoint::from)
                .allMatch(from -> !from.isBefore(LocalDate.of(2024, 1, 15)));
        assertThat(result.get(0).from()).isEqualTo(LocalDate.of(2024, 1, 15));
    }

    @Test
    void noRecordsAtAll_returnsEmptyAndSkipsCountQuery() {
        when(repo.earliestRecordDate(any())).thenReturn(null);

        var result = service.weeklyCounts(null, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 28), null, null);

        assertThat(result).isEmpty();
        verify(repo, never()).countCreatedByDay(any(), any(), any(), any());
    }

    @Test
    void earliestRecordBeforeRequestedStart_doesNotExpandRangeBackward() {
        when(repo.earliestRecordDate(any())).thenReturn(LocalDate.of(2020, 1, 1));
        when(repo.countCreatedByDay(any(), any(), any(), any())).thenReturn(List.of());

        var result = service.weeklyCounts(null, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 14), null, null);

        assertThat(result.get(0).from()).isEqualTo(LocalDate.of(2024, 1, 1));
    }

    @Test
    void earliestRecordAfterRequestedEnd_returnsEmpty() {
        when(repo.earliestRecordDate(any())).thenReturn(LocalDate.of(2025, 1, 1));

        var result = service.weeklyCounts(null, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 6, 30), null, null);

        assertThat(result).isEmpty();
    }

    @Test
    void bucketsAreFullIsoWeeks_evenWhenRequestedRangeIsMidWeek() {
        // 2024-01-01 is a Monday; 2024-01-17 is a Wednesday. Buckets must
        // still be complete Monday..Sunday weeks, so the last one extends
        // to 2024-01-21 even though only through the 17th was requested.
        when(repo.earliestRecordDate(any())).thenReturn(LocalDate.of(2024, 1, 1));
        when(repo.countCreatedByDay(any(), any(), any(), any())).thenReturn(List.of());

        var result = service.weeklyCounts(null, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 17), null, null);

        result.forEach(w -> {
            assertThat(w.from().getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
            assertThat(w.to().getDayOfWeek()).isEqualTo(DayOfWeek.SUNDAY);
        });
        assertThat(result.get(result.size() - 1).to()).isEqualTo(LocalDate.of(2024, 1, 21));
    }

    @Test
    void isoWeekStraddlingYearBoundary_isOneBucketOwnedByTheWeekBasedYear() {
        // The ISO week containing 2024-12-31 runs Mon 2024-12-30 .. Sun
        // 2025-01-05, and belongs to week-based-year 2025 (it owns the
        // Thursday, Jan 2 2025) — not two separate buckets split at Dec 31.
        when(repo.earliestRecordDate(any())).thenReturn(LocalDate.of(2024, 12, 1));
        when(repo.countCreatedByDay(any(), any(), any(), any())).thenReturn(List.of());

        var result = service.weeklyCounts(null, LocalDate.of(2024, 12, 30), LocalDate.of(2025, 1, 5), null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).from()).isEqualTo(LocalDate.of(2024, 12, 30));
        assertThat(result.get(0).to()).isEqualTo(LocalDate.of(2025, 1, 5));
    }

    @Test
    void weekNo_reflectsActualIsoWeekOfYear_notASequentialCounter() {
        when(repo.earliestRecordDate(any())).thenReturn(LocalDate.of(2024, 1, 1));
        when(repo.countCreatedByDay(any(), any(), any(), any())).thenReturn(List.of());

        var result = service.weeklyCounts(null, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 14), null, null);

        assertThat(result.get(0).weekNo()).isEqualTo(1);
        assertThat(result.get(1).weekNo()).isEqualTo(2);
    }

    @Test
    void currentInProgressIsoWeek_isExcludedFromTheSeries() {
        LocalDate today = LocalDate.now();
        LocalDate farBack = today.minusYears(2);
        when(repo.earliestRecordDate(any())).thenReturn(farBack);
        when(repo.countCreatedByDay(any(), any(), any(), any())).thenReturn(List.of());

        var result = service.weeklyCounts(null, farBack, today, null, null);

        LocalDate currentWeekMonday = today.with(DayOfWeek.MONDAY);
        assertThat(result).extracting(WeekSeriesPoint::from)
                .noneMatch(from -> from.equals(currentWeekMonday));
        assertThat(result.get(result.size() - 1).to()).isEqualTo(currentWeekMonday.minusDays(1));
    }
}
