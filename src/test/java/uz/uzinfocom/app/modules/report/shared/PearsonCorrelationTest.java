package uz.uzinfocom.app.modules.report.shared;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PearsonCorrelationTest {

    @Test
    void perfectPositiveAndNegativeLinesGiveOneAndMinusOne() {
        double[] x = {1, 2, 3, 4, 5};

        assertThat(PearsonCorrelation.compute(x, new double[]{2, 4, 6, 8, 10}).coefficient())
                .isCloseTo(1.0, org.assertj.core.data.Offset.offset(1e-12));
        assertThat(PearsonCorrelation.compute(x, new double[]{10, 8, 6, 4, 2}).coefficient())
                .isCloseTo(-1.0, org.assertj.core.data.Offset.offset(1e-12));
    }

    @Test
    void matchesAHandComputedValue() {
        // x = 1..5, y = 2,1,4,3,5: cov = 8, var(x) = 10, var(y) = 10 -> r = 0.8
        PearsonCorrelation.Result result =
                PearsonCorrelation.compute(new double[]{1, 2, 3, 4, 5}, new double[]{2, 1, 4, 3, 5});

        assertThat(result.coefficient()).isCloseTo(0.8, org.assertj.core.data.Offset.offset(1e-12));
        assertThat(result.pairs()).isEqualTo(5);
        assertThat(result.strength()).isEqualTo(PearsonCorrelation.Strength.STRONG);
    }

    @Test
    void isUndefinedWithTooFewPairsOrAFlatSeries() {
        assertThat(PearsonCorrelation.compute(new double[]{1, 2}, new double[]{3, 4}).coefficient()).isNull();
        assertThat(PearsonCorrelation.compute(new double[]{1, 2, 3}, new double[]{5, 5, 5}).coefficient()).isNull();
        assertThat(PearsonCorrelation.compute(new double[]{}, new double[]{}).pairs()).isZero();
    }

    @Test
    void isOnlyReliableFromFourteenPairs() {
        double[] x = new double[14];
        double[] y = new double[14];
        for (int i = 0; i < 14; i++) {
            x[i] = i;
            y[i] = i * 2.0 + (i % 2);
        }

        assertThat(PearsonCorrelation.compute(x, y).reliable()).isTrue();
        assertThat(PearsonCorrelation.compute(java.util.Arrays.copyOf(x, 13), java.util.Arrays.copyOf(y, 13)).reliable())
                .isFalse();
    }

    @Test
    void readsTheStrengthFromTheAbsoluteValue() {
        assertThat(PearsonCorrelation.Strength.of(0.05)).isEqualTo(PearsonCorrelation.Strength.NONE);
        assertThat(PearsonCorrelation.Strength.of(-0.2)).isEqualTo(PearsonCorrelation.Strength.WEAK);
        assertThat(PearsonCorrelation.Strength.of(0.3)).isEqualTo(PearsonCorrelation.Strength.MODERATE);
        assertThat(PearsonCorrelation.Strength.of(-0.9)).isEqualTo(PearsonCorrelation.Strength.STRONG);
    }

    @Test
    void rejectsSeriesOfDifferentLength() {
        assertThatThrownBy(() -> PearsonCorrelation.compute(new double[]{1, 2, 3}, new double[]{1, 2}))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
