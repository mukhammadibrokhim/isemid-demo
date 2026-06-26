package uz.uzinfocom.isemid.features.forecast.service;

import org.junit.jupiter.api.Test;
import uz.uzinfocom.isemid.features.forecast.service.Form058WeeklyAggregationService.WeekSeriesPoint;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class HoltWeeklyCalculatorTest {

    private final HoltWeeklyCalculator calculator = new HoltWeeklyCalculator();

    // ── Initialization ────────────────────────────────────────────────────────

    @Test
    void firstPoint_hasNullForecastAndError() {
        var result = calculator.compute(weeklyPoints(new long[]{10, 20, 30}), 0.3, 0.1);
        var first = result.points().get(0);
        assertThat(first.yHat()).isNull();
        assertThat(first.error()).isNull();
    }

    @Test
    void firstPoint_levelEqualsY1_trendEqualsY2MinusY1() {
        var result = calculator.compute(weeklyPoints(new long[]{10, 20, 30}), 0.3, 0.1);
        var first = result.points().get(0);
        assertThat(first.level()).isEqualTo(10.0);
        assertThat(first.trend()).isEqualTo(10.0);  // y2 - y1 = 20 - 10
    }

    @Test
    void singlePoint_trendIsZero() {
        var result = calculator.compute(weeklyPoints(new long[]{50}), 0.3, 0.1);
        assertThat(result.points().get(0).trend()).isZero();
    }

    // ── Recursive formulas ────────────────────────────────────────────────────

    @Test
    void forecastEqualsLevelPlusTrendOfPreviousPoint() {
        var result = calculator.compute(weeklyPoints(new long[]{10, 20, 30}), 0.3, 0.1);
        var prev  = result.points().get(0);
        var point = result.points().get(1);
        assertThat(point.yHat()).isCloseTo(prev.level() + prev.trend(), offset(1e-9));
    }

    @Test
    void errorEqualsActualMinusForecast() {
        var result = calculator.compute(weeklyPoints(new long[]{10, 20, 30}), 0.5, 0.5);
        for (int i = 1; i < result.points().size(); i++) {
            var p = result.points().get(i);
            assertThat(p.error()).as("error at [%d]", i)
                    .isCloseTo(p.y() - p.yHat(), offset(1e-9));
        }
    }

    @Test
    void levelUpdateFormula() {
        double alpha = 0.3;
        var pts = weeklyPoints(new long[]{10, 20});
        var result = calculator.compute(pts, alpha, 0.1);
        double prevL = result.points().get(0).level();
        double prevT = result.points().get(0).trend();
        double y1    = result.points().get(1).y();
        double expectedL = alpha * y1 + (1.0 - alpha) * (prevL + prevT);
        assertThat(result.points().get(1).level()).isCloseTo(expectedL, offset(1e-9));
    }

    @Test
    void trendUpdateFormula() {
        double beta = 0.2;
        var result = calculator.compute(weeklyPoints(new long[]{10, 20}), 0.3, beta);
        double prevL = result.points().get(0).level();
        double prevT = result.points().get(0).trend();
        double newL  = result.points().get(1).level();
        double expectedT = beta * (newL - prevL) + (1.0 - beta) * prevT;
        assertThat(result.points().get(1).trend()).isCloseTo(expectedT, offset(1e-9));
    }

    // ── Response structure ────────────────────────────────────────────────────

    @Test
    void resultContainsAlphaAndBeta() {
        var result = calculator.compute(weeklyPoints(new long[]{5, 10}), 0.4, 0.2);
        assertThat(result.alpha()).isEqualTo(0.4);
        assertThat(result.beta()).isEqualTo(0.2);
    }

    @Test
    void pointCountMatchesInputSize() {
        var result = calculator.compute(weeklyPoints(new long[]{1, 2, 3, 4, 5}), 0.3, 0.1);
        assertThat(result.points()).hasSize(5);
    }

    // ── Empty / null input ────────────────────────────────────────────────────

    @Test
    void emptyList_returnsEmptyPoints() {
        var result = calculator.compute(List.of(), 0.3, 0.1);
        assertThat(result.points()).isEmpty();
    }

    @Test
    void nullList_returnsEmptyPoints() {
        var result = calculator.compute(null, 0.3, 0.1);
        assertThat(result.points()).isEmpty();
    }

    @Test
    void allZeroWeeks_noDivisionByZero() {
        var zeros = weeklyPoints(new long[]{0, 0, 0, 0});
        assertThatNoException().isThrownBy(() -> calculator.compute(zeros, 0.3, 0.1));
    }

    @Test
    void allZeroWeeks_forecastsAndErrorsAreZero() {
        var result = calculator.compute(weeklyPoints(new long[]{0, 0, 0, 0}), 0.3, 0.1);
        for (int i = 1; i < result.points().size(); i++) {
            assertThat(result.points().get(i).yHat()).isZero();
            assertThat(result.points().get(i).error()).isZero();
        }
    }

    // ── Validation ────────────────────────────────────────────────────────────

    @Test
    void alphaOutOfRange_throws() {
        assertThatThrownBy(() -> calculator.compute(List.of(), 0.0, 0.5))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("alpha");
        assertThatThrownBy(() -> calculator.compute(List.of(), 1.0, 0.5))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("alpha");
    }

    @Test
    void betaOutOfRange_throws() {
        assertThatThrownBy(() -> calculator.compute(List.of(), 0.5, 0.0))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("beta");
        assertThatThrownBy(() -> calculator.compute(List.of(), 0.5, 1.0))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("beta");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static List<WeekSeriesPoint> weeklyPoints(long[] values) {
        var list = new ArrayList<WeekSeriesPoint>(values.length);
        LocalDate cur = LocalDate.of(2024, 1, 1);
        for (int i = 0; i < values.length; i++) {
            list.add(new WeekSeriesPoint(i + 1, cur, cur.plusDays(6), values[i]));
            cur = cur.plusDays(7);
        }
        return list;
    }
}
