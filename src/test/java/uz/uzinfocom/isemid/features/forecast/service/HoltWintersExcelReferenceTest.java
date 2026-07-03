package uz.uzinfocom.isemid.features.forecast.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.forecast.service.Form058MonthlyAggregationService.MonthSeriesPoint;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;
import static org.mockito.Mockito.mock;

/**
 * Regression fixture for additive Holt-Winters (alpha=0.3, beta=0.1,
 * gamma=0.2, season=12), validated by epidemiologists against an
 * independently built Excel model. Expected values are the Excel model's
 * outputs; tolerance is +/-0.01 as specified by the reference, except MAPE
 * which the reference states rounded to 1 decimal.
 *
 * <p>The 24-point series is fractional (e.g. 90.8). Production data can
 * never be fractional — {@link MonthSeriesPoint#y()} is a {@code long}
 * because it's always a SQL {@code COUNT()} of case rows — so this suite
 * exercises {@link HoltWintersCalculator#fitSeasonal} directly (exact
 * doubles, matches the Excel figures to +/-0.01) and separately confirms
 * the public {@link HoltWintersCalculator#compute} wraps that same result
 * correctly using a rounded version of the series (what real DB-sourced
 * data would look like).
 */
class HoltWintersExcelReferenceTest {

    private static final double[] SERIES = {
        90.8, 94.6, 96.4, 103.2, 110.0, 115.8, 120.6, 117.4, 112.2, 108.0, 105.8, 97.6,
        100.4, 104.2, 106.0, 112.8, 119.6, 125.4, 130.2, 127.0, 121.8, 117.6, 115.4, 107.2,
    };
    private static final double ALPHA = 0.3;
    private static final double BETA = 0.1;
    private static final double GAMMA = 0.2;
    private static final int SEASON = 12;
    private static final double TOLERANCE = 0.01;

    // F_13..F_24
    private static final double[] EXPECTED_FITTED = {
        91.60, 99.10, 103.65, 112.44, 120.65, 127.40,
        132.81, 129.95, 124.91, 120.72, 118.44, 110.09,
    };
    // h = 1..12 (2026)
    private static final double[] EXPECTED_FORECAST = {
        104.33, 108.29, 110.38, 117.57, 124.85, 131.19,
        136.58, 134.01, 129.46, 125.94, 124.42, 116.92,
    };
    private static final double EXPECTED_MAPE = 2.8;

    private final HoltWintersCalculator calculator = new HoltWintersCalculator(mock(MessageUtil.class));

    @ParameterizedTest(name = "offsetFromT13={0}, expected={1}")
    @MethodSource("fittedCases")
    void fittedValuesMatchExcelReference(int offsetFromT13, double expected) {
        var fit = calculator.fitSeasonal(SERIES, ALPHA, BETA, GAMMA, SEASON);
        assertThat(fit.fitted()[SEASON + offsetFromT13]).isCloseTo(expected, offset(TOLERANCE));
    }

    private static Stream<Arguments> fittedCases() {
        return IntStream.range(0, EXPECTED_FITTED.length)
                .mapToObj(i -> Arguments.of(i, EXPECTED_FITTED[i]));
    }

    @ParameterizedTest(name = "h={0} forecast ≈ {1}")
    @MethodSource("forecastCases")
    void forecastValuesMatchExcelReference(int h, double expected) {
        var fit = calculator.fitSeasonal(SERIES, ALPHA, BETA, GAMMA, SEASON);
        double[] forecast = fit.forecast(EXPECTED_FORECAST.length, SEASON);
        assertThat(forecast[h - 1]).isCloseTo(expected, offset(TOLERANCE));
    }

    private static Stream<Arguments> forecastCases() {
        return IntStream.rangeClosed(1, EXPECTED_FORECAST.length)
                .mapToObj(h -> Arguments.of(h, EXPECTED_FORECAST[h - 1]));
    }

    @Test
    void mapeMatchesExcelReference() {
        var fit = calculator.fitSeasonal(SERIES, ALPHA, BETA, GAMMA, SEASON);
        // reference states MAPE rounded to 1 decimal; half a rounding step is the tightest honest bound
        assertThat(fit.mape()).isCloseTo(EXPECTED_MAPE, offset(0.05));
    }

    @Test
    void firstFittedValueIsNaN_warmUpSeasonHasNoOneStepForecast() {
        var fit = calculator.fitSeasonal(SERIES, ALPHA, BETA, GAMMA, SEASON);
        for (int i = 0; i < SEASON; i++) {
            assertThat(fit.fitted()[i]).isNaN();
        }
    }

    @Test
    void initializationMatchesExcelReference() {
        var fit = calculator.fitSeasonal(SERIES, ALPHA, BETA, GAMMA, SEASON);
        // L12, B12 are carried at the last index of the first season (index 11)
        assertThat(fit.level()[SEASON - 1]).isCloseTo(106.03, offset(TOLERANCE));
        assertThat(fit.trend()[SEASON - 1]).isCloseTo(0.80, offset(TOLERANCE));
        // S1 (index 0) and S12 (index 11), before any recursive update
        assertThat(fit.seasonal()[0]).isCloseTo(-15.23, offset(TOLERANCE));
        assertThat(fit.seasonal()[SEASON - 1]).isCloseTo(-8.43, offset(TOLERANCE));
    }

    @Test
    void finalStateMatchesExcelReference() {
        var fit = calculator.fitSeasonal(SERIES, ALPHA, BETA, GAMMA, SEASON);
        int last = SERIES.length - 1; // t = 24
        assertThat(fit.level()[last]).isCloseTo(117.66, offset(TOLERANCE));
        assertThat(fit.trend()[last]).isCloseTo(0.68, offset(TOLERANCE));
    }

    @Test
    void qualityLabel_isExcellentTier() {
        // The codebase labels quality tiers in Uzbek Cyrillic (Аъло/Яхши/
        // Қониқарли/Ёмон = excellent/good/acceptable/bad) rather than
        // English, at the same <10/10-20/20-50/>50 MAPE thresholds as the
        // reference spec. MAPE ≈ 2.8 falls in the "excellent" (<10) tier.
        long[] rounded = new long[SERIES.length];
        for (int i = 0; i < SERIES.length; i++) rounded[i] = Math.round(SERIES[i]);
        var response = calculator.compute(monthlyPoints(rounded), ALPHA, BETA, GAMMA, SEASON, 12, false);
        assertThat(response.metrics().quality()).isEqualTo("Аъло");
    }

    @Test
    void publicComputeApi_agreesWithFitSeasonal_onSameIntegerSeries() {
        // MonthSeriesPoint.y() is long, so the public entry point can't take
        // the exact fractional Excel series (see class javadoc). This
        // confirms compute() correctly plumbs fitSeasonal's numbers into
        // the response DTO, using a rounded version of the same series.
        long[] rounded = new long[SERIES.length];
        for (int i = 0; i < SERIES.length; i++) rounded[i] = Math.round(SERIES[i]);

        double[] roundedAsDouble = new double[rounded.length];
        for (int i = 0; i < rounded.length; i++) roundedAsDouble[i] = rounded[i];
        var directFit = calculator.fitSeasonal(roundedAsDouble, ALPHA, BETA, GAMMA, SEASON);

        var response = calculator.compute(monthlyPoints(rounded), ALPHA, BETA, GAMMA, SEASON, 12, false);

        for (int i = SEASON; i < rounded.length; i++) {
            assertThat(response.history().get(i).forecast())
                    .as("history[%d].forecast", i)
                    .isCloseTo(directFit.fitted()[i], offset(1e-9));
        }
        double[] directForecast = directFit.forecast(12, SEASON);
        for (int h = 1; h <= 12; h++) {
            assertThat(response.future().get(h - 1).forecast())
                    .as("future h=%d", h)
                    .isCloseTo(directForecast[h - 1], offset(1e-9));
        }
        assertThat(response.metrics().mape()).isCloseTo(directFit.mape(), offset(1e-9));
    }

    private static List<MonthSeriesPoint> monthlyPoints(long[] values) {
        var list = new ArrayList<MonthSeriesPoint>(values.length);
        LocalDate cur = LocalDate.of(2024, 1, 1);
        for (int i = 0; i < values.length; i++) {
            LocalDate from = cur.withDayOfMonth(1);
            LocalDate to = cur.withDayOfMonth(cur.lengthOfMonth());
            list.add(new MonthSeriesPoint(i + 1, from, to, values[i]));
            cur = cur.plusMonths(1);
        }
        return list;
    }
}
