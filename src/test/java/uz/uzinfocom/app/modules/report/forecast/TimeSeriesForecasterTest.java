package uz.uzinfocom.app.modules.report.forecast;

import org.junit.jupiter.api.Test;
import uz.uzinfocom.app.modules.report.forecast.application.query.dto.ForecastMethod;
import uz.uzinfocom.app.modules.report.forecast.application.query.forecasting.TimeSeriesForecaster;

import static org.assertj.core.api.Assertions.assertThat;

class TimeSeriesForecasterTest {

    @Test
    void autoPicksNaiveForVeryShortSeries() {
        TimeSeriesForecaster.Result result =
                TimeSeriesForecaster.forecast(new double[] {2, 4}, 3, 52, ForecastMethod.AUTO);

        assertThat(result.method()).isEqualTo(ForecastMethod.NAIVE_MEAN);
        assertThat(result.point()).hasSize(3);
    }

    @Test
    void autoPicksHoltWintersWhenTwoFullSeasonsAvailable() {
        double[] history = new double[24];
        for (int i = 0; i < 24; i++) {
            history[i] = 10 + (i % 12); // clear month-of-year seasonality
        }

        TimeSeriesForecaster.Result result =
                TimeSeriesForecaster.forecast(history, 6, 12, ForecastMethod.AUTO);

        assertThat(result.method()).isEqualTo(ForecastMethod.HOLT_WINTERS_ADDITIVE);
    }

    @Test
    void holtProjectsALinearTrendForward() {
        double[] history = {1, 2, 3, 4, 5, 6, 7, 8};

        TimeSeriesForecaster.Result result =
                TimeSeriesForecaster.forecast(history, 3, 7, ForecastMethod.HOLT);

        // next values should keep climbing, roughly 9, 10, 11
        assertThat(result.point()[0]).isBetween(8.0, 10.5);
        assertThat(result.point()[2]).isGreaterThan(result.point()[0]);
    }

    @Test
    void everyBoundIsNonNegativeAndOrdered() {
        double[] history = {5, 0, 3, 0, 8, 1, 0, 4, 2, 0};

        TimeSeriesForecaster.Result result =
                TimeSeriesForecaster.forecast(history, 5, 7, ForecastMethod.AUTO);

        for (int h = 0; h < 5; h++) {
            assertThat(result.lower()[h]).isGreaterThanOrEqualTo(0.0);
            assertThat(result.point()[h]).isGreaterThanOrEqualTo(result.lower()[h]);
            assertThat(result.upper()[h]).isGreaterThanOrEqualTo(result.point()[h]);
        }
    }

    @Test
    void seasonalModelReproducesSeasonalShape() {
        double[] history = new double[36];
        for (int i = 0; i < 36; i++) {
            history[i] = (i % 12 == 0) ? 100 : 10; // annual spike every 12th bucket
        }

        TimeSeriesForecaster.Result result =
                TimeSeriesForecaster.forecast(history, 12, 12, ForecastMethod.HOLT_WINTERS_ADDITIVE);

        // history length 36 → the first forecast step (index 36, phase 0) lands on the spike
        assertThat(result.point()[0]).isGreaterThan(50.0);
        // every other near-term step is on the low baseline phase
        assertThat(result.point()[0]).isGreaterThan(result.point()[5] + 40.0);
    }

    @Test
    void flatHistoryStillYieldsAVisibleBand() {
        double[] history = {4, 4, 4, 4, 4, 4, 4, 4};

        TimeSeriesForecaster.Result result =
                TimeSeriesForecaster.forecast(history, 4, 7, ForecastMethod.SES);

        assertThat(result.point()[0]).isCloseTo(4.0, org.assertj.core.data.Offset.offset(0.5));
        assertThat(result.upper()[0]).isGreaterThan(result.point()[0]);
    }
}
