package uz.uzinfocom.isemid.features.forecast.service;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import uz.uzinfocom.isemid.commons.exceptions.ApiException;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.forecast.service.Form058MonthlyAggregationService.MonthSeriesPoint;
import uz.uzinfocom.isemid.features.forecast.service.Form058WeeklyAggregationService.WeekSeriesPoint;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class Form058HoltWintersFacadeTest {

    private final Form058MonthlyAggregationService monthlyAggregationService = mock(Form058MonthlyAggregationService.class);
    private final Form058WeeklyAggregationService weeklyAggregationService = mock(Form058WeeklyAggregationService.class);
    private final HoltWintersCalculator calculator = new HoltWintersCalculator(mock(MessageUtil.class));

    private final Form058HoltWintersFacade facade = new Form058HoltWintersFacade(
            monthlyAggregationService, weeklyAggregationService, calculator, mock(MessageUtil.class));

    @Test
    void monthly_fetchesTwoSeasonsOfHistoryBeforeRequestedStart() {
        LocalDate requestedStart = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2025, 3, 31);

        when(monthlyAggregationService.monthlyCounts(any(), any(), any(), any(), any()))
                .thenReturn(monthlyPoints(LocalDate.of(2023, 1, 1), 27));

        var startCaptor = ArgumentCaptor.forClass(LocalDate.class);
        facade.holt(null, requestedStart, end, 0.3, 0.1, 0.2, null, null, "MONTHLY", null, null);

        verify(monthlyAggregationService)
                .monthlyCounts(isNull(), startCaptor.capture(), eq(end), isNull(), isNull());

        // default season for monthly is 12 → fetch starts 24 months before the requested start
        assertThat(startCaptor.getValue()).isEqualTo(LocalDate.of(2023, 1, 1));
    }

    @Test
    void monthly_historyIsTrimmedBackToRequestedWindow() {
        LocalDate requestedStart = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2025, 3, 31);

        when(monthlyAggregationService.monthlyCounts(any(), any(), any(), any(), any()))
                .thenReturn(monthlyPoints(LocalDate.of(2023, 1, 1), 27));

        var result = facade.holt(null, requestedStart, end, 0.3, 0.1, 0.2, null, null, "MONTHLY", null, null);

        // only the 3 requested months (Jan-Mar 2025) should come back, not the 24 warm-up months
        assertThat(result.history()).hasSize(3);
        assertThat(result.history().get(0).from()).isEqualTo(LocalDate.of(2025, 1, 1));
        assertThat(result.history().get(2).from()).isEqualTo(LocalDate.of(2025, 3, 1));
    }

    @Test
    void weekly_fetchesTwoSeasonsOfHistoryBeforeRequestedStart() {
        LocalDate requestedStart = LocalDate.of(2025, 6, 2);
        LocalDate end = LocalDate.of(2025, 6, 15);

        when(weeklyAggregationService.weeklyCounts(any(), any(), any(), any(), any()))
                .thenReturn(weeklyPoints(requestedStart.minusDays(7L * 2 * 52), 2 * 52 + 2));

        var startCaptor = ArgumentCaptor.forClass(LocalDate.class);
        facade.holt(null, requestedStart, end, 0.3, 0.1, 0.2, null, null, "WEEKLY", null, null);

        verify(weeklyAggregationService)
                .weeklyCounts(isNull(), startCaptor.capture(), eq(end), isNull(), isNull());

        // default season for weekly is 52 → fetch starts 104 weeks before the requested start
        assertThat(startCaptor.getValue()).isEqualTo(requestedStart.minusDays(7L * 104));
    }

    @Test
    void weekly_historyIsTrimmedBackToRequestedWindow() {
        LocalDate requestedStart = LocalDate.of(2025, 6, 2);
        LocalDate end = LocalDate.of(2025, 6, 15);
        LocalDate fetchStart = requestedStart.minusDays(7L * 104);

        when(weeklyAggregationService.weeklyCounts(any(), any(), any(), any(), any()))
                .thenReturn(weeklyPoints(fetchStart, 104 + 2));

        var result = facade.holt(null, requestedStart, end, 0.3, 0.1, 0.2, null, null, "WEEKLY", null, null);

        // only the 2 requested weeks should come back, not the 104 warm-up weeks
        assertThat(result.history()).hasSize(2);
        assertThat(result.history().get(0).from()).isEqualTo(requestedStart);
        // future dates must still step by 7 days, not by calendar month
        assertThat(result.future().get(0).from()).isEqualTo(end.plusDays(1));
        assertThat(result.future().get(0).to()).isEqualTo(end.plusDays(7));
        assertThat(result.future().get(1).from()).isEqualTo(result.future().get(0).to().plusDays(1));
    }

    @Test
    void nullStartDate_throwsApiException() {
        assertThatThrownBy(() ->
                facade.holt(null, null, LocalDate.of(2025, 1, 1), 0.3, 0.1, 0.2, null, null, "MONTHLY", null, null))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void startDateAfterEndDate_throwsApiException() {
        assertThatThrownBy(() ->
                facade.holt(null, LocalDate.of(2025, 6, 1), LocalDate.of(2025, 1, 1),
                        0.3, 0.1, 0.2, null, null, "MONTHLY", null, null))
                .isInstanceOf(ApiException.class);
    }

    private static List<MonthSeriesPoint> monthlyPoints(LocalDate startMonth, int count) {
        var list = new ArrayList<MonthSeriesPoint>(count);
        var cur = startMonth;
        for (int i = 0; i < count; i++) {
            LocalDate from = cur.withDayOfMonth(1);
            LocalDate to = cur.withDayOfMonth(cur.lengthOfMonth());
            list.add(new MonthSeriesPoint(i + 1, from, to, 10 + i));
            cur = cur.plusMonths(1);
        }
        return list;
    }

    private static List<WeekSeriesPoint> weeklyPoints(LocalDate startWeek, int count) {
        var list = new ArrayList<WeekSeriesPoint>(count);
        var cur = startWeek;
        for (int i = 0; i < count; i++) {
            LocalDate from = cur;
            LocalDate to = cur.plusDays(6);
            list.add(new WeekSeriesPoint(i + 1, from, to, 10 + i));
            cur = cur.plusDays(7);
        }
        return list;
    }
}
