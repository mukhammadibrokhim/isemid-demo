package uz.uzinfocom.isemid.features.forecast.service;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import uz.uzinfocom.isemid.commons.exceptions.ApiException;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.forecast.service.Form058MonthlyAggregationService.MonthSeriesPoint;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;

class HoltWintersCalculatorTest {

    private final HoltWintersCalculator calculator = new HoltWintersCalculator(mock(MessageUtil.class));

    // 24 months of data (2 full seasons, season=12)
    private static final long[] DATA_24 = {
        90, 94, 96, 103, 110, 115, 120, 117, 112, 108, 105, 97,   // 2024
        100, 104, 106, 112, 119, 125, 130, 127, 121, 117, 115, 107 // 2025
    };

    // ── Initialization ────────────────────────────────────────────────────────

    @Test
    void firstSeasonPointsHaveNullForecastAndNullError() {
        var result = calculator.compute(monthlyPoints(DATA_24), 0.3, 0.1, 0.2, 12, 0, false);
        for (int i = 0; i < 12; i++) {
            var p = result.history().get(i);
            assertThat(p.forecast()).as("history[%d].forecast", i).isNull();
            assertThat(p.error()).as("history[%d].error", i).isNull();
        }
    }

    @Test
    void firstSeasonPointsHaveInitialSeasonalIndex() {
        var result = calculator.compute(monthlyPoints(DATA_24), 0.3, 0.1, 0.2, 12, 0, false);
        double L = initialLevel(DATA_24, 12);
        assertThat(result.history().get(0).seasonal())
                .isCloseTo(DATA_24[0] - L, offset(1e-6));
        assertThat(result.history().get(11).seasonal())
                .isCloseTo(DATA_24[11] - L, offset(1e-6));
    }

    @Test
    void initialLevelAndTrendShownAtEndOfFirstSeason() {
        var result = calculator.compute(monthlyPoints(DATA_24), 0.3, 0.1, 0.2, 12, 0, false);
        double expectedL = initialLevel(DATA_24, 12);
        double expectedT = initialTrend(DATA_24, 12);

        assertThat(result.history().get(11).level()).isCloseTo(expectedL, offset(1e-6));
        assertThat(result.history().get(11).trend()).isCloseTo(expectedT, offset(1e-6));

        for (int i = 0; i < 11; i++) {
            assertThat(result.history().get(i).level()).as("history[%d].level", i).isNull();
            assertThat(result.history().get(i).trend()).as("history[%d].trend", i).isNull();
        }
    }

    // ── Recursive formulas ────────────────────────────────────────────────────

    @Test
    void firstForecastEqualsLevelPlusTrendPlusSeasonalOffset() {
        var result = calculator.compute(monthlyPoints(DATA_24), 0.3, 0.1, 0.2, 12, 0, false);
        double L  = result.history().get(11).level();
        double T  = result.history().get(11).trend();
        double S1 = result.history().get(0).seasonal();
        assertThat(result.history().get(12).forecast()).isCloseTo(L + T + S1, offset(1e-6));
    }

    @Test
    void errorEqualsActualMinusForecast() {
        var result = calculator.compute(monthlyPoints(DATA_24), 0.3, 0.1, 0.2, 12, 0, false);
        for (int i = 12; i < 24; i++) {
            var p = result.history().get(i);
            assertThat(p.error()).as("error at [%d]", i)
                    .isCloseTo(p.y() - p.forecast(), offset(1e-9));
        }
    }

    @Test
    void pointsFromSecondSeasonHaveAllFieldsFilled() {
        var result = calculator.compute(monthlyPoints(DATA_24), 0.3, 0.1, 0.2, 12, 0, false);
        for (int i = 12; i < 24; i++) {
            var p = result.history().get(i);
            assertThat(p.forecast()).as("[%d].forecast", i).isNotNull();
            assertThat(p.error()).as("[%d].error", i).isNotNull();
            assertThat(p.level()).as("[%d].level", i).isNotNull();
            assertThat(p.trend()).as("[%d].trend", i).isNotNull();
            assertThat(p.seasonal()).as("[%d].seasonal", i).isNotNull();
        }
    }

    // ── MAPE & quality ────────────────────────────────────────────────────────

    @Test
    void constantSeries_mapeIsZero_qualityExcellent() {
        long[] constant = new long[24];
        Arrays.fill(constant, 100L);
        var result = calculator.compute(monthlyPoints(constant), 0.3, 0.1, 0.2, 12, 0, false);
        assertThat(result.metrics().mape()).isZero();
        assertThat(result.metrics().quality()).isEqualTo("Аъло");
    }

    @Test
    void highlyVariableSecondSeason_qualityPoor() {
        long[] wild = new long[24];
        Arrays.fill(wild, 0, 12, 100L);
        Arrays.fill(wild, 12, 24, 1L);   // forecast ≈ 100, actual = 1 → huge MAPE
        var result = calculator.compute(monthlyPoints(wild), 0.3, 0.1, 0.2, 12, 0, false);
        assertThat(result.metrics().quality()).isEqualTo("Ёмон");
    }

    @Test
    void zeroActualPoints_areSkippedFromMape_notCountedAsZeroError() {
        // Mix of zero and non-zero actuals in the recursion range: MAPE must
        // be the mean over only the non-zero-actual points, not diluted by
        // treating the zero points as if they had 0% error.
        long[] mixed = new long[24];
        Arrays.fill(mixed, 0, 12, 100L);
        // second season: alternate 0 and 200 — forecast will be far from
        // both, but the zero-actual months must not count toward MAPE at all
        for (int i = 12; i < 24; i++) mixed[i] = (i % 2 == 0) ? 0L : 200L;
        var result = calculator.compute(monthlyPoints(mixed), 0.3, 0.1, 0.2, 12, 0, false);

        // recompute expected MAPE independently, skipping y=0 points entirely
        double sumFirst = 0;
        for (int i = 0; i < 12; i++) sumFirst += mixed[i];
        double L = sumFirst / 12;
        double sumSecond = 0;
        for (int i = 12; i < 24; i++) sumSecond += mixed[i];
        double T = (sumSecond / 12 - L) / 12;
        double[] S = new double[24];
        for (int i = 0; i < 12; i++) S[i] = mixed[i] - L;
        double mapeSum = 0;
        int mCount = 0;
        for (int i = 12; i < 24; i++) {
            double yi = mixed[i];
            double forecast = L + T + S[i - 12];
            double newL = 0.3 * (yi - S[i - 12]) + 0.7 * (L + T);
            double newT = 0.1 * (newL - L) + 0.9 * T;
            S[i] = 0.2 * (yi - newL) + 0.8 * S[i - 12];
            L = newL;
            T = newT;
            if (yi != 0) {
                mapeSum += Math.abs((yi - forecast) / yi) * 100.0;
                mCount++;
            }
        }
        double expectedMape = mapeSum / mCount;
        assertThat(result.metrics().mape()).isCloseTo(expectedMape, offset(1e-9));
    }

    // ── Future forecasts ──────────────────────────────────────────────────────

    @Test
    void futureSize_equalsHorizonParam() {
        var result = calculator.compute(monthlyPoints(DATA_24), 0.3, 0.1, 0.2, 12, 7, false);
        assertThat(result.future()).hasSize(7);
    }

    @Test
    void futureMonths_areConsecutiveCalendarMonths() {
        var result = calculator.compute(monthlyPoints(DATA_24), 0.3, 0.1, 0.2, 12, 3, false);
        var f = result.future();
        assertThat(f.get(0).from()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(f.get(0).to()).isEqualTo(LocalDate.of(2026, 1, 31));
        assertThat(f.get(1).from()).isEqualTo(LocalDate.of(2026, 2, 1));
        assertThat(f.get(1).to()).isEqualTo(LocalDate.of(2026, 2, 28));
        assertThat(f.get(2).from()).isEqualTo(LocalDate.of(2026, 3, 1));
        assertThat(f.get(2).to()).isEqualTo(LocalDate.of(2026, 3, 31));
    }

    @Test
    void horizonZero_returnEmptyFutureList() {
        var result = calculator.compute(monthlyPoints(DATA_24), 0.3, 0.1, 0.2, 12, 0, false);
        assertThat(result.future()).isEmpty();
    }

    @Test
    void horizonAboveSeason_cyclesSeasonalIndicesWithoutError() {
        assertThatNoException().isThrownBy(() ->
                calculator.compute(monthlyPoints(DATA_24), 0.3, 0.1, 0.2, 12, 24, false));
    }

    @Test
    void monthlyFuture_notAffectedByWeeklyFlag() {
        var weeklyOff = calculator.compute(monthlyPoints(DATA_24), 0.3, 0.1, 0.2, 12, 2, false);
        assertThat(weeklyOff.future().get(0).from()).isEqualTo(LocalDate.of(2026, 1, 1));
    }

    @Test
    void weeklyFuture_datesStepBySevenDays_notByCalendarMonth() {
        // small season (4) with exactly 2 seasons of data so the seasonal
        // path runs; only the date cadence is under test here.
        var result = calculator.compute(
                weeklyPoints(new long[]{10, 12, 9, 15, 11, 14, 13, 16}),
                0.3, 0.1, 0.2, 4, 3, true);
        var lastHistory = result.history().get(7);
        var f = result.future();
        assertThat(f.get(0).from()).isEqualTo(lastHistory.to().plusDays(1));
        assertThat(f.get(0).to()).isEqualTo(f.get(0).from().plusDays(6));
        assertThat(f.get(1).from()).isEqualTo(f.get(0).to().plusDays(1));
        assertThat(f.get(2).from()).isEqualTo(f.get(1).to().plusDays(1));
    }

    // ── Empty repository (all y = 0) ──────────────────────────────────────────

    @Test
    void emptyRepo_fullRange_noDivisionByZero() {
        long[] zeros = new long[24];
        assertThatNoException().isThrownBy(() ->
                calculator.compute(monthlyPoints(zeros), 0.3, 0.1, 0.2, 12, 12, false));
    }

    @Test
    void emptyRepo_fullRange_allForecastsAreZero() {
        long[] zeros = new long[24];
        var result = calculator.compute(monthlyPoints(zeros), 0.3, 0.1, 0.2, 12, 12, false);
        for (int i = 12; i < 24; i++) {
            assertThat(result.history().get(i).forecast()).as("history[%d]", i).isZero();
        }
        result.future().forEach(fp ->
                assertThat(fp.forecast()).as("future h=%d", fp.h()).isZero());
    }

    @Test
    void emptyRepo_fullRange_mapeIsZeroAndQualityExcellent() {
        long[] zeros = new long[24];
        var result = calculator.compute(monthlyPoints(zeros), 0.3, 0.1, 0.2, 12, 12, false);
        assertThat(result.metrics().mape()).isZero();
        assertThat(result.metrics().quality()).isEqualTo("Аъло");
    }

    @Test
    void emptyRepo_fullRange_historyAndFutureSizesAreCorrect() {
        long[] zeros = new long[24];
        var result = calculator.compute(monthlyPoints(zeros), 0.3, 0.1, 0.2, 12, 12, false);
        assertThat(result.history()).hasSize(24);
        assertThat(result.future()).hasSize(12);
    }

    @Test
    void emptyRepo_fullRange_errorsAreZero() {
        long[] zeros = new long[24];
        var result = calculator.compute(monthlyPoints(zeros), 0.3, 0.1, 0.2, 12, 12, false);
        for (int i = 12; i < 24; i++) {
            assertThat(result.history().get(i).error()).as("history[%d].error", i).isZero();
        }
    }

    @Test
    void emptyRepo_partiallyZero_nonZeroSecondSeasonUpdatesLevel() {
        long[] mixed = new long[24];
        Arrays.fill(mixed, 12, 24, 100L);  // first season = 0, second = 100
        var result = calculator.compute(monthlyPoints(mixed), 0.3, 0.1, 0.2, 12, 0, false);
        assertThat(result.history().get(23).level()).isGreaterThan(0);
    }

    // ── Insufficient data → validation error (not a silent fallback) ──────────

    @Test
    void insufficientData_throwsBadRequestApiException() {
        assertThatThrownBy(() ->
                calculator.compute(monthlyPoints(new long[]{1, 2, 3}), 0.3, 0.1, 0.2, 12, 12, false))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void fewerThanTwoPoints_throwsBadRequestApiException() {
        assertThatThrownBy(() ->
                calculator.compute(monthlyPoints(new long[]{1}), 0.3, 0.1, 0.2, 12, 12, false))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void nullList_throwsBadRequestApiException() {
        assertThatThrownBy(() -> calculator.compute(null, 0.3, 0.1, 0.2, 12, 0, false))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void exactlyOneSeason_stillThrows() {
        // n == season < 2*season: one full season alone isn't enough to
        // initialize both L/T/S (T needs a second season to estimate).
        long[] zeros = new long[12];
        assertThatThrownBy(() -> calculator.compute(monthlyPoints(zeros), 0.3, 0.1, 0.2, 12, 0, false))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void exactlyTwoSeasons_doesNotThrow() {
        // n == 2*season is the minimum sufficient case — boundary check.
        assertThatNoException().isThrownBy(() ->
                calculator.compute(monthlyPoints(DATA_24), 0.3, 0.1, 0.2, 12, 0, false));
    }

    // ── Validation ──────────────────────────────────────────────────────────

    @Test
    void alphaOutOfRange_throwsBadRequest() {
        assertThatThrownBy(() -> calculator.compute(List.of(), 0.0, 0.5, 0.5, 12, 0, false))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
        assertThatThrownBy(() -> calculator.compute(List.of(), 1.0, 0.5, 0.5, 12, 0, false))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void betaOutOfRange_throwsBadRequest() {
        assertThatThrownBy(() -> calculator.compute(List.of(), 0.5, 0.0, 0.5, 12, 0, false))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void gammaOutOfRange_throwsBadRequest() {
        assertThatThrownBy(() -> calculator.compute(List.of(), 0.5, 0.5, 1.0, 12, 0, false))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void seasonLessThanTwo_throwsBadRequest() {
        assertThatThrownBy(() -> calculator.compute(List.of(), 0.3, 0.1, 0.2, 1, 0, false))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void horizonNegative_throwsBadRequest() {
        assertThatThrownBy(() -> calculator.compute(List.of(), 0.3, 0.1, 0.2, 12, -1, false))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void responseCarriesCorrectParameters() {
        var result = calculator.compute(monthlyPoints(DATA_24), 0.3, 0.1, 0.2, 12, 0, false);
        assertThat(result.alpha()).isEqualTo(0.3);
        assertThat(result.beta()).isEqualTo(0.1);
        assertThat(result.gamma()).isEqualTo(0.2);
        assertThat(result.season()).isEqualTo(12);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static List<MonthSeriesPoint> monthlyPoints(long[] values) {
        var list = new ArrayList<MonthSeriesPoint>(values.length);
        LocalDate cur = LocalDate.of(2024, 1, 1);
        for (int i = 0; i < values.length; i++) {
            LocalDate from = cur.withDayOfMonth(1);
            LocalDate to   = cur.withDayOfMonth(cur.lengthOfMonth());
            list.add(new MonthSeriesPoint(i + 1, from, to, values[i]));
            cur = cur.plusMonths(1);
        }
        return list;
    }

    private static List<MonthSeriesPoint> weeklyPoints(long[] values) {
        var list = new ArrayList<MonthSeriesPoint>(values.length);
        LocalDate cur = LocalDate.of(2024, 1, 1);
        for (int i = 0; i < values.length; i++) {
            LocalDate from = cur;
            LocalDate to   = cur.plusDays(6);
            list.add(new MonthSeriesPoint(i + 1, from, to, values[i]));
            cur = cur.plusDays(7);
        }
        return list;
    }

    private static double initialLevel(long[] y, int season) {
        long sum = 0;
        for (int i = 0; i < season; i++) sum += y[i];
        return (double) sum / season;
    }

    private static double initialTrend(long[] y, int season) {
        long sum1 = 0, sum2 = 0;
        for (int i = 0; i < season; i++) sum1 += y[i];
        for (int i = season; i < 2 * season; i++) sum2 += y[i];
        return ((double) sum2 / season - (double) sum1 / season) / season;
    }
}
