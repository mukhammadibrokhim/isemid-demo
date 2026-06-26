package uz.uzinfocom.isemid.features.forecast.service;

import org.junit.jupiter.api.Test;
import uz.uzinfocom.isemid.features.forecast.service.Form058MonthlyAggregationService.MonthSeriesPoint;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class HoltWeeklyCalculatorTest {

    private final HoltWeeklyCalculator calculator = new HoltWeeklyCalculator();

    // 24 months of data (2 full seasons) — integer counts matching Excel shape
    private static final long[] DATA_24 = {
        90, 94, 96, 103, 110, 115, 120, 117, 112, 108, 105, 97,   // 2024
        100, 104, 106, 112, 119, 125, 130, 127, 121, 117, 115, 107 // 2025
    };

    // ── Initialization ────────────────────────────────────────────────────────

    @Test
    void firstSeasonPointsHaveNullForecastAndNullError() {
        var result = calculator.compute(monthlyPoints(DATA_24), 0.3, 0.1, 0.2, 12, 0);

        for (int i = 0; i < 12; i++) {
            var p = result.history().get(i);
            assertThat(p.forecast()).as("history[%d].forecast", i).isNull();
            assertThat(p.error()).as("history[%d].error", i).isNull();
        }
    }

    @Test
    void firstSeasonPointsHaveInitialSeasonalIndex() {
        var result = calculator.compute(monthlyPoints(DATA_24), 0.3, 0.1, 0.2, 12, 0);

        // S[0] = y[0] - avg(first 12)
        double L = initialLevel(DATA_24, 12);
        assertThat(result.history().get(0).seasonal())
                .isCloseTo(DATA_24[0] - L, offset(1e-6));
        assertThat(result.history().get(11).seasonal())
                .isCloseTo(DATA_24[11] - L, offset(1e-6));
    }

    @Test
    void initialLevelAndTrendAreShownAtEndOfFirstSeason() {
        var result = calculator.compute(monthlyPoints(DATA_24), 0.3, 0.1, 0.2, 12, 0);

        double expectedL = initialLevel(DATA_24, 12);
        double expectedT = initialTrend(DATA_24, 12);

        // Shown only at history[11] (last period of first season)
        assertThat(result.history().get(11).level()).isCloseTo(expectedL, offset(1e-6));
        assertThat(result.history().get(11).trend()).isCloseTo(expectedT, offset(1e-6));

        // All other first-season points have null level/trend
        for (int i = 0; i < 11; i++) {
            assertThat(result.history().get(i).level()).as("history[%d].level", i).isNull();
            assertThat(result.history().get(i).trend()).as("history[%d].trend", i).isNull();
        }
    }

    // ── Recursive formulas ────────────────────────────────────────────────────

    @Test
    void firstForecastEqualsLevelPlusTrendPlusSeasonalOffset() {
        var result = calculator.compute(monthlyPoints(DATA_24), 0.3, 0.1, 0.2, 12, 0);

        double L  = result.history().get(11).level();
        double T  = result.history().get(11).trend();
        double S1 = result.history().get(0).seasonal();   // seasonal index for slot 0

        double expected = L + T + S1;
        assertThat(result.history().get(12).forecast()).isCloseTo(expected, offset(1e-6));
    }

    @Test
    void errorEqualsActualMinusForecast() {
        var result = calculator.compute(monthlyPoints(DATA_24), 0.3, 0.1, 0.2, 12, 0);

        for (int i = 12; i < 24; i++) {
            var p = result.history().get(i);
            double expected = p.y() - p.forecast();
            assertThat(p.error()).as("error at history[%d]", i).isCloseTo(expected, offset(1e-9));
        }
    }

    @Test
    void pointsFromSecondSeasonOnwardHaveNonNullFields() {
        var result = calculator.compute(monthlyPoints(DATA_24), 0.3, 0.1, 0.2, 12, 0);

        for (int i = 12; i < 24; i++) {
            var p = result.history().get(i);
            assertThat(p.forecast()).as("forecast at [%d]", i).isNotNull();
            assertThat(p.error()).as("error at [%d]", i).isNotNull();
            assertThat(p.level()).as("level at [%d]", i).isNotNull();
            assertThat(p.trend()).as("trend at [%d]", i).isNotNull();
            assertThat(p.seasonal()).as("seasonal at [%d]", i).isNotNull();
        }
    }

    // ── MAPE & quality ────────────────────────────────────────────────────────

    @Test
    void constantSeries_mapeIsZero_qualityExcellent() {
        long[] constant = new long[24];
        java.util.Arrays.fill(constant, 100L);

        var result = calculator.compute(monthlyPoints(constant), 0.3, 0.1, 0.2, 12, 0);

        assertThat(result.metrics().mape()).isZero();
        assertThat(result.metrics().quality()).isEqualTo("Аъло");
    }

    @Test
    void qualityThresholds_poor_whenMapeAbove50() {
        // Drive MAPE high by using wildly variable data in the second season
        long[] wild = new long[24];
        java.util.Arrays.fill(wild, 0, 12, 100L);
        java.util.Arrays.fill(wild, 12, 24, 1L);   // second season values near 0 vs. large forecast
        // MAPE will be very high (forecast ≈ 100, actual ≈ 1 → APE ≈ 9900%)
        var result = calculator.compute(monthlyPoints(wild), 0.3, 0.1, 0.2, 12, 0);
        assertThat(result.metrics().quality()).isEqualTo("Ёмон");
    }

    // ── Future forecasts ──────────────────────────────────────────────────────

    @Test
    void futureSize_equalsHorizonParam() {
        var result = calculator.compute(monthlyPoints(DATA_24), 0.3, 0.1, 0.2, 12, 7);
        assertThat(result.future()).hasSize(7);
    }

    @Test
    void futureMonths_areConsecutiveCalendarMonths() {
        var result = calculator.compute(monthlyPoints(DATA_24), 0.3, 0.1, 0.2, 12, 3);
        var f = result.future();

        assertThat(f.get(0).from()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(f.get(0).to()).isEqualTo(LocalDate.of(2026, 1, 31));
        assertThat(f.get(1).from()).isEqualTo(LocalDate.of(2026, 2, 1));
        assertThat(f.get(1).to()).isEqualTo(LocalDate.of(2026, 2, 28));
        assertThat(f.get(2).from()).isEqualTo(LocalDate.of(2026, 3, 1));
        assertThat(f.get(2).to()).isEqualTo(LocalDate.of(2026, 3, 31));
    }

    @Test
    void futureHorizonZero_returnEmptyFutureList() {
        var result = calculator.compute(monthlyPoints(DATA_24), 0.3, 0.1, 0.2, 12, 0);
        assertThat(result.future()).isEmpty();
    }

    @Test
    void futureHorizonAboveSeason_cyclesSeasonalIndices() {
        // horizon=24 (2 seasons) — index cycling should not throw AIOOB
        assertThatNoException().isThrownBy(() ->
                calculator.compute(monthlyPoints(DATA_24), 0.3, 0.1, 0.2, 12, 24));
    }

    // ── Empty repository (all y = 0) ─────────────────────────────────────────

    @Test
    void emptyRepo_fullRange_noDivisionByZero() {
        // repo returns nothing → aggregation fills every month with y=0
        long[] zeros = new long[24];
        assertThatNoException().isThrownBy(() ->
                calculator.compute(monthlyPoints(zeros), 0.3, 0.1, 0.2, 12, 12));
    }

    @Test
    void emptyRepo_fullRange_allForecastsAreZero() {
        long[] zeros = new long[24];
        var result = calculator.compute(monthlyPoints(zeros), 0.3, 0.1, 0.2, 12, 12);

        // L=0, T=0, S=[0,...] → every forecast = 0
        for (int i = 12; i < 24; i++) {
            assertThat(result.history().get(i).forecast())
                    .as("history[%d].forecast", i)
                    .isZero();
        }
        result.future().forEach(fp ->
                assertThat(fp.forecast()).as("future h=%d", fp.h()).isZero());
    }

    @Test
    void emptyRepo_fullRange_mapeIsZeroAndQualityExcellent() {
        // y=0 every period: division guard (yi != 0) keeps ape=0 → MAPE=0
        long[] zeros = new long[24];
        var result = calculator.compute(monthlyPoints(zeros), 0.3, 0.1, 0.2, 12, 12);

        assertThat(result.metrics().mape()).isZero();
        assertThat(result.metrics().quality()).isEqualTo("Аъло");
    }

    @Test
    void emptyRepo_fullRange_historyAndFutureSizesAreCorrect() {
        long[] zeros = new long[24];
        var result = calculator.compute(monthlyPoints(zeros), 0.3, 0.1, 0.2, 12, 12);

        assertThat(result.history()).hasSize(24);
        assertThat(result.future()).hasSize(12);
    }

    @Test
    void emptyRepo_fullRange_errorsAreZero() {
        long[] zeros = new long[24];
        var result = calculator.compute(monthlyPoints(zeros), 0.3, 0.1, 0.2, 12, 12);

        for (int i = 12; i < 24; i++) {
            assertThat(result.history().get(i).error())
                    .as("history[%d].error", i)
                    .isZero();
        }
    }

    @Test
    void emptyRepo_exactlyOneSeason_noRecursiveForecasts() {
        // Only 12 months — initialization only, recursive loop never runs
        long[] zeros = new long[12];
        var result = calculator.compute(monthlyPoints(zeros), 0.3, 0.1, 0.2, 12, 0);

        assertThat(result.history()).hasSize(12);
        result.history().forEach(p ->
                assertThat(p.forecast()).as("period %d should have null forecast", p.periodNo()).isNull());
    }

    @Test
    void emptyRepo_exactlyOneSeason_mapeIsZeroNoMeasurements() {
        long[] zeros = new long[12];
        var result = calculator.compute(monthlyPoints(zeros), 0.3, 0.1, 0.2, 12, 0);

        // mCount = 0 → MAPE defaults to 0
        assertThat(result.metrics().mape()).isZero();
        assertThat(result.metrics().quality()).isEqualTo("Аъло");
    }

    @Test
    void emptyRepo_exactlyOneSeason_futureIsZeroWithHorizon() {
        long[] zeros = new long[12];
        var result = calculator.compute(monthlyPoints(zeros), 0.3, 0.1, 0.2, 12, 3);

        assertThat(result.future()).hasSize(3);
        result.future().forEach(fp ->
                assertThat(fp.forecast()).as("future h=%d", fp.h()).isZero());
    }

    @Test
    void emptyRepo_lessThanOneSeason_returnsEmpty() {
        // 11 months: < season=12 → cannot initialize, guard returns empty
        long[] zeros = new long[11];
        var result = calculator.compute(monthlyPoints(zeros), 0.3, 0.1, 0.2, 12, 12);

        assertThat(result.history()).isEmpty();
        assertThat(result.future()).isEmpty();
    }

    @Test
    void emptyRepo_partiallyZero_nonZeroMonthsInfluenceForecast() {
        // First season all zeros, second season has real data → forecast shifts toward actual
        long[] mixed = new long[24];
        java.util.Arrays.fill(mixed, 12, 24, 100L);   // second season = 100

        var result = calculator.compute(monthlyPoints(mixed), 0.3, 0.1, 0.2, 12, 0);

        // L_init = 0, so first forecast = 0; but level/trend update after seeing 100s
        // → later forecasts should be positive (pulled toward 100)
        var lastHistorical = result.history().get(23);
        assertThat(lastHistorical.level()).isGreaterThan(0);
    }

    // ── Edge cases & validation ───────────────────────────────────────────────

    @Test
    void insufficientData_returnsEmptyHistoryAndFuture() {
        var tooFew = monthlyPoints(new long[]{1, 2, 3});   // < season=12
        var result = calculator.compute(tooFew, 0.3, 0.1, 0.2, 12, 12);
        assertThat(result.history()).isEmpty();
        assertThat(result.future()).isEmpty();
    }

    @Test
    void nullList_returnsEmptyResult() {
        var result = calculator.compute(null, 0.3, 0.1, 0.2, 12, 0);
        assertThat(result.history()).isEmpty();
    }

    @Test
    void alphaOutOfRange_throwsIllegalArgument() {
        assertThatThrownBy(() -> calculator.compute(List.of(), 0.0, 0.5, 0.5, 12, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("alpha");

        assertThatThrownBy(() -> calculator.compute(List.of(), 1.0, 0.5, 0.5, 12, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("alpha");
    }

    @Test
    void betaOutOfRange_throwsIllegalArgument() {
        assertThatThrownBy(() -> calculator.compute(List.of(), 0.5, 0.0, 0.5, 12, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("beta");
    }

    @Test
    void gammaOutOfRange_throwsIllegalArgument() {
        assertThatThrownBy(() -> calculator.compute(List.of(), 0.5, 0.5, 1.0, 12, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("gamma");
    }

    @Test
    void seasonLessThanTwo_throwsIllegalArgument() {
        assertThatThrownBy(() -> calculator.compute(List.of(), 0.3, 0.1, 0.2, 1, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("season");
    }

    @Test
    void horizonNegative_throwsIllegalArgument() {
        assertThatThrownBy(() -> calculator.compute(List.of(), 0.3, 0.1, 0.2, 12, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("horizon");
    }

    @Test
    void responseContainsCorrectAlphaBetaGammaSeason() {
        var result = calculator.compute(monthlyPoints(DATA_24), 0.3, 0.1, 0.2, 12, 0);
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
