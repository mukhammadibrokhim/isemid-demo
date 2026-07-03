package uz.uzinfocom.isemid.features.forecast.service;

import org.junit.jupiter.api.Test;
import uz.uzinfocom.isemid.features.forecast.models.projection.DayCountProjection;
import uz.uzinfocom.isemid.features.forecast.repositories.Form058ForecastRepository;
import uz.uzinfocom.isemid.features.forecast.service.Form058MonthlyAggregationService.MonthSeriesPoint;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class Form058MonthlyAggregationServiceTest {

    private final Form058ForecastRepository repo = mock(Form058ForecastRepository.class);
    private final Form058MonthlyAggregationService service =
            new Form058MonthlyAggregationService(repo, new MonthBucketService());

    @Test
    void earliestRecordAfterRequestedStart_clampsLeadingBuckets() {
        when(repo.earliestRecordDate(any())).thenReturn(LocalDate.of(2024, 6, 15));
        when(repo.countCreatedByDay(any(), any(), any(), any())).thenReturn(List.of());

        var result = service.monthlyCounts(null, LocalDate.of(2023, 1, 1), LocalDate.of(2024, 8, 31), null, null);

        // no synthetic zero months before June 2024, even though the caller asked for Jan 2023
        assertThat(result).extracting(MonthSeriesPoint::from)
                .containsExactly(
                        LocalDate.of(2024, 6, 1),
                        LocalDate.of(2024, 7, 1),
                        LocalDate.of(2024, 8, 1));
    }

    @Test
    void noRecordsAtAll_returnsEmptyAndSkipsCountQuery() {
        when(repo.earliestRecordDate(any())).thenReturn(null);

        var result = service.monthlyCounts(null, LocalDate.of(2023, 1, 1), LocalDate.of(2024, 8, 31), null, null);

        assertThat(result).isEmpty();
        verify(repo, never()).countCreatedByDay(any(), any(), any(), any());
    }

    @Test
    void earliestRecordBeforeRequestedStart_doesNotExpandRangeBackward() {
        when(repo.earliestRecordDate(any())).thenReturn(LocalDate.of(2020, 1, 1));
        when(repo.countCreatedByDay(any(), any(), any(), any())).thenReturn(List.of());

        var result = service.monthlyCounts(null, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 29), null, null);

        assertThat(result).extracting(MonthSeriesPoint::from)
                .containsExactly(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1));
    }

    @Test
    void earliestRecordAfterRequestedEnd_returnsEmpty() {
        when(repo.earliestRecordDate(any())).thenReturn(LocalDate.of(2025, 1, 1));

        var result = service.monthlyCounts(null, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 6, 30), null, null);

        assertThat(result).isEmpty();
    }

    @Test
    void earliestRecordLookup_ignoresDiagnosisFilter_usesResolvedStateCode() {
        when(repo.earliestRecordDate(eq("TASH"))).thenReturn(LocalDate.of(2024, 1, 1));
        when(repo.countCreatedByDay(any(), any(), any(), any())).thenReturn(List.of());

        service.monthlyCounts(null, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31), "TASH", "A00");

        verify(repo).earliestRecordDate(eq("TASH"));
    }

    @Test
    void currentInProgressMonth_isExcludedFromTheSeries() {
        LocalDate today = LocalDate.now();
        LocalDate farBack = today.minusYears(3);
        when(repo.earliestRecordDate(any())).thenReturn(farBack);
        when(repo.countCreatedByDay(any(), any(), any(), any())).thenReturn(List.of());

        var result = service.monthlyCounts(null, farBack, today, null, null);

        YearMonth currentMonth = YearMonth.from(today);
        assertThat(result).extracting(MonthSeriesPoint::from)
                .noneMatch(from -> YearMonth.from(from).equals(currentMonth));
        assertThat(result.get(result.size() - 1).to())
                .isEqualTo(currentMonth.minusMonths(1).atEndOfMonth());
    }

    @Test
    void sparseDayRows_areZeroFilledForMissingDaysWithinActiveBuckets() {
        when(repo.earliestRecordDate(any())).thenReturn(LocalDate.of(2024, 1, 1));
        // only 2 of the ~60 days across Jan-Feb 2024 have any rows
        when(repo.countCreatedByDay(any(), any(), any(), any())).thenReturn(List.of(
                dayCount(LocalDate.of(2024, 1, 5), 3L),
                dayCount(LocalDate.of(2024, 2, 20), 7L)
        ));

        var result = service.monthlyCounts(null, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 29), null, null);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).y()).isEqualTo(3L);  // January: only the 1 matching day
        assertThat(result.get(1).y()).isEqualTo(7L);  // February: only the 1 matching day
    }

    private static DayCountProjection dayCount(LocalDate day, long cnt) {
        return new DayCountProjection() {
            @Override
            public LocalDate getDay() { return day; }

            @Override
            public long getCnt() { return cnt; }
        };
    }
}
