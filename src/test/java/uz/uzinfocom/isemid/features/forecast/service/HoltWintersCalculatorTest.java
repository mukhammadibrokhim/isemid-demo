package uz.uzinfocom.isemid.features.forecast.service;

import org.junit.jupiter.api.Test;
import uz.uzinfocom.isemid.features.forecast.service.Form058MonthlyAggregationService.MonthSeriesPoint;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class HoltWintersCalculatorTest {

    private final HoltWintersCalculator calculator = new HoltWintersCalculator();

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

    // ── Weekly cadence (period=WEEKLY) ──────────────────────────────────────────

    @Test
    void weeklyFuture_datesStepBySevenDays_notByCalendarMonth() {
        // 6 weeks of data, season=52 → n < 2*season → non-seasonal fallback,
        // but future dates must still advance week-by-week, not month-by-month.
        var result = calculator.compute(weeklyPoints(new long[]{10, 12, 9, 15, 11, 14}),
                0.3, 0.1, 0.2, 52, 3, true);
        var lastHistory = result.history().get(5);
        var f = result.future();
        assertThat(f.get(0).from()).isEqualTo(lastHistory.to().plusDays(1));
        assertThat(f.get(0).to()).isEqualTo(f.get(0).from().plusDays(6));
        assertThat(f.get(1).from()).isEqualTo(f.get(0).to().plusDays(1));
        assertThat(f.get(2).from()).isEqualTo(f.get(1).to().plusDays(1));
    }

    @Test
    void monthlyFuture_notAffectedByWeeklyFlag() {
        var weeklyOff = calculator.compute(monthlyPoints(DATA_24), 0.3, 0.1, 0.2, 12, 2, false);
        assertThat(weeklyOff.future().get(0).from()).isEqualTo(LocalDate.of(2026, 1, 1));
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
    void emptyRepo_exactlyOneSeason_fallsBackToNonSeasonal_recursesAfterFirstPoint() {
        // n == season < 2*season: not enough for seasonal decomposition,
        // so this falls back to plain Holt, which only needs the first
        // point to seed level/trend before it starts recursing.
        long[] zeros = new long[12];
        var result = calculator.compute(monthlyPoints(zeros), 0.3, 0.1, 0.2, 12, 0, false);
        assertThat(result.history()).hasSize(12);
        assertThat(result.history().get(0).forecast()).as("first period").isNull();
        for (int i = 1; i < 12; i++) {
            assertThat(result.history().get(i).forecast()).as("period %d", i).isNotNull();
            assertThat(result.history().get(i).seasonal()).as("period %d seasonal", i).isNull();
        }
    }

    @Test
    void emptyRepo_exactlyOneSeason_mapeDefaultsToZero() {
        long[] zeros = new long[12];
        var result = calculator.compute(monthlyPoints(zeros), 0.3, 0.1, 0.2, 12, 0, false);
        assertThat(result.metrics().mape()).isZero();
        assertThat(result.metrics().quality()).isEqualTo("Аъло");
    }

    @Test
    void emptyRepo_exactlyOneSeason_futureIsZeroWithHorizon() {
        long[] zeros = new long[12];
        var result = calculator.compute(monthlyPoints(zeros), 0.3, 0.1, 0.2, 12, 3, false);
        assertThat(result.future()).hasSize(3);
        result.future().forEach(fp ->
                assertThat(fp.forecast()).as("future h=%d", fp.h()).isZero());
    }

    @Test
    void emptyRepo_lessThanOneSeason_fallsBackToNonSeasonal_stillReturnsData() {
        long[] zeros = new long[11];
        var result = calculator.compute(monthlyPoints(zeros), 0.3, 0.1, 0.2, 12, 12, false);
        assertThat(result.history()).hasSize(11);
        assertThat(result.future()).hasSize(12);
        result.future().forEach(fp ->
                assertThat(fp.forecast()).as("future h=%d", fp.h()).isZero());
    }

    @Test
    void emptyRepo_partiallyZero_nonZeroSecondSeasonUpdatesLevel() {
        long[] mixed = new long[24];
        Arrays.fill(mixed, 12, 24, 100L);  // first season = 0, second = 100
        var result = calculator.compute(monthlyPoints(mixed), 0.3, 0.1, 0.2, 12, 0, false);
        assertThat(result.history().get(23).level()).isGreaterThan(0);
    }

    // ── Edge cases & validation ───────────────────────────────────────────────

    @Test
    void fewerThanTwoPoints_returnsEmptyHistoryAndFuture() {
        var result = calculator.compute(monthlyPoints(new long[]{1}), 0.3, 0.1, 0.2, 12, 12, false);
        assertThat(result.history()).isEmpty();
        assertThat(result.future()).isEmpty();
    }

    @Test
    void nullList_returnsEmptyResult() {
        var result = calculator.compute(null, 0.3, 0.1, 0.2, 12, 0, false);
        assertThat(result.history()).isEmpty();
    }

    // ── Non-seasonal fallback (n < 2*season) ──────────────────────────────────

    @Test
    void shortRange_fewerThanOneSeason_stillProducesHistoryAndForecast() {
        var result = calculator.compute(monthlyPoints(new long[]{100, 105, 108, 112, 115, 120}),
                0.3, 0.1, 0.2, 12, 3, false);
        assertThat(result.history()).hasSize(6);
        assertThat(result.future()).hasSize(3);
        assertThat(result.history().get(0).forecast()).as("first period").isNull();
        for (int i = 1; i < 6; i++) {
            assertThat(result.history().get(i).forecast()).as("period %d", i).isNotNull();
            assertThat(result.history().get(i).seasonal()).as("period %d seasonal", i).isNull();
        }
    }

    @Test
    void shortRange_futureForecast_isPlainTrendExtrapolation() {
        var result = calculator.compute(monthlyPoints(new long[]{100, 105, 108, 112, 115, 120}),
                0.3, 0.1, 0.2, 12, 2, false);
        var last = result.history().get(5);
        double expectedH1 = Math.max(0.0, last.level() + last.trend());
        double expectedH2 = Math.max(0.0, last.level() + 2 * last.trend());
        assertThat(result.future().get(0).forecast()).isCloseTo(expectedH1, offset(1e-6));
        assertThat(result.future().get(1).forecast()).isCloseTo(expectedH2, offset(1e-6));
    }

    @Test
    void twoPointSeries_minimalData_stillComputes() {
        var result = calculator.compute(monthlyPoints(new long[]{10, 20}), 0.3, 0.1, 0.2, 12, 1, false);
        assertThat(result.history()).hasSize(2);
        assertThat(result.future()).hasSize(1);
    }

    @Test
    void alphaOutOfRange_throws() {
        assertThatThrownBy(() -> calculator.compute(List.of(), 0.0, 0.5, 0.5, 12, 0, false))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("alpha");
        assertThatThrownBy(() -> calculator.compute(List.of(), 1.0, 0.5, 0.5, 12, 0, false))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("alpha");
    }

    @Test
    void betaOutOfRange_throws() {
        assertThatThrownBy(() -> calculator.compute(List.of(), 0.5, 0.0, 0.5, 12, 0, false))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("beta");
    }

    @Test
    void gammaOutOfRange_throws() {
        assertThatThrownBy(() -> calculator.compute(List.of(), 0.5, 0.5, 1.0, 12, 0, false))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("gamma");
    }

    @Test
    void seasonLessThanTwo_throws() {
        assertThatThrownBy(() -> calculator.compute(List.of(), 0.3, 0.1, 0.2, 1, 0, false))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("season");
    }

    @Test
    void horizonNegative_throws() {
        assertThatThrownBy(() -> calculator.compute(List.of(), 0.3, 0.1, 0.2, 12, -1, false))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("horizon");
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
