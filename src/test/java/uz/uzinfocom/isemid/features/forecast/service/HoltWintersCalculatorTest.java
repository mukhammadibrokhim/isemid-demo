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
        double L = initialLevel(DATA_24, 12);
        assertThat(result.history().get(0).seasonal())
                .isCloseTo(DATA_24[0] - L, offset(1e-6));
        assertThat(result.history().get(11).seasonal())
                .isCloseTo(DATA_24[11] - L, offset(1e-6));
    }

    @Test
    void initialLevelAndTrendShownAtEndOfFirstSeason() {
        var result = calculator.compute(monthlyPoints(DATA_24), 0.3, 0.1, 0.2, 12, 0);
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
        var result = calculator.compute(monthlyPoints(DATA_24), 0.3, 0.1, 0.2, 12, 0);
        double L  = result.history().get(11).level();
        double T  = result.history().get(11).trend();
        double S1 = result.history().get(0).seasonal();
        assertThat(result.history().get(12).forecast()).isCloseTo(L + T + S1, offset(1e-6));
    }

    @Test
    void errorEqualsActualMinusForecast() {
        var result = calculator.compute(monthlyPoints(DATA_24), 0.3, 0.1, 0.2, 12, 0);
        for (int i = 12; i < 24; i++) {
            var p = result.history().get(i);
            assertThat(p.error()).as("error at [%d]", i)
                    .isCloseTo(p.y() - p.forecast(), offset(1e-9));
        }
    }

    @Test
    void pointsFromSecondSeasonHaveAllFieldsFilled() {
        var result = calculator.compute(monthlyPoints(DATA_24), 0.3, 0.1, 0.2, 12, 0);
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
        var result = calculator.compute(monthlyPoints(constant), 0.3, 0.1, 0.2, 12, 0);
        assertThat(result.metrics().mape()).isZero();
        assertThat(result.metrics().quality()).isEqualTo("Аъло");
    }

    @Test
    void highlyVariableSecondSeason_qualityPoor() {
        long[] wild = new long[24];
        Arrays.fill(wild, 0, 12, 100L);
        Arrays.fill(wild, 12, 24, 1L);   // forecast ≈ 100, actual = 1 → huge MAPE
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
    void horizonZero_returnEmptyFutureList() {
        var result = calculator.compute(monthlyPoints(DATA_24), 0.3, 0.1, 0.2, 12, 0);
        assertThat(result.future()).isEmpty();
    }

    @Test
    void horizonAboveSeason_cyclesSeasonalIndicesWithoutError() {
        assertThatNoException().isThrownBy(() ->
                calculator.compute(monthlyPoints(DATA_24), 0.3, 0.1, 0.2, 12, 24));
    }

    // ── Empty repository (all y = 0) ──────────────────────────────────────────

    @Test
    void emptyRepo_fullRange_noDivisionByZero() {
        long[] zeros = new long[24];
        assertThatNoException().isThrownBy(() ->
                calculator.compute(monthlyPoints(zeros), 0.3, 0.1, 0.2, 12, 12));
    }

    @Test
    void emptyRepo_fullRange_allForecastsAreZero() {
        long[] zeros = new long[24];
        var result = calculator.compute(monthlyPoints(zeros), 0.3, 0.1, 0.2, 12, 12);
        for (int i = 12; i < 24; i++) {
            assertThat(result.history().get(i).forecast()).as("history[%d]", i).isZero();
        }
        result.future().forEach(fp ->
                assertThat(fp.forecast()).as("future h=%d", fp.h()).isZero());
    }

    @Test
    void emptyRepo_fullRange_mapeIsZeroAndQualityExcellent() {
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
            assertThat(result.history().get(i).error()).as("history[%d].error", i).isZero();
        }
    }

    @Test
    void emptyRepo_exactlyOneSeason_noRecursiveForecasts() {
        long[] zeros = new long[12];
        var result = calculator.compute(monthlyPoints(zeros), 0.3, 0.1, 0.2, 12, 0);
        assertThat(result.history()).hasSize(12);
        result.history().forEach(p ->
                assertThat(p.forecast()).as("period %d", p.periodNo()).isNull());
    }

    @Test
    void emptyRepo_exactlyOneSeason_mapeDefaultsToZero() {
        long[] zeros = new long[12];
        var result = calculator.compute(monthlyPoints(zeros), 0.3, 0.1, 0.2, 12, 0);
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
        long[] zeros = new long[11];
        var result = calculator.compute(monthlyPoints(zeros), 0.3, 0.1, 0.2, 12, 12);
        assertThat(result.history()).isEmpty();
        assertThat(result.future()).isEmpty();
    }

    @Test
    void emptyRepo_partiallyZero_nonZeroSecondSeasonUpdatesLevel() {
        long[] mixed = new long[24];
        Arrays.fill(mixed, 12, 24, 100L);  // first season = 0, second = 100
        var result = calculator.compute(monthlyPoints(mixed), 0.3, 0.1, 0.2, 12, 0);
        assertThat(result.history().get(23).level()).isGreaterThan(0);
    }

    // ── Edge cases & validation ───────────────────────────────────────────────

    @Test
    void insufficientData_returnsEmptyHistoryAndFuture() {
        var result = calculator.compute(monthlyPoints(new long[]{1, 2, 3}), 0.3, 0.1, 0.2, 12, 12);
        assertThat(result.history()).isEmpty();
        assertThat(result.future()).isEmpty();
    }

    @Test
    void nullList_returnsEmptyResult() {
        var result = calculator.compute(null, 0.3, 0.1, 0.2, 12, 0);
        assertThat(result.history()).isEmpty();
    }

    @Test
    void alphaOutOfRange_throws() {
        assertThatThrownBy(() -> calculator.compute(List.of(), 0.0, 0.5, 0.5, 12, 0))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("alpha");
        assertThatThrownBy(() -> calculator.compute(List.of(), 1.0, 0.5, 0.5, 12, 0))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("alpha");
    }

    @Test
    void betaOutOfRange_throws() {
        assertThatThrownBy(() -> calculator.compute(List.of(), 0.5, 0.0, 0.5, 12, 0))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("beta");
    }

    @Test
    void gammaOutOfRange_throws() {
        assertThatThrownBy(() -> calculator.compute(List.of(), 0.5, 0.5, 1.0, 12, 0))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("gamma");
    }

    @Test
    void seasonLessThanTwo_throws() {
        assertThatThrownBy(() -> calculator.compute(List.of(), 0.3, 0.1, 0.2, 1, 0))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("season");
    }

    @Test
    void horizonNegative_throws() {
        assertThatThrownBy(() -> calculator.compute(List.of(), 0.3, 0.1, 0.2, 12, -1))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("horizon");
    }

    @Test
    void responseCarriesCorrectParameters() {
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
