package uz.uzinfocom.app.modules.report.forecast.application.query.dto;

/**
 * Which extrapolation model produces the forecast. {@link #AUTO} (the
 * default) lets {@code TimeSeriesForecaster} pick based on how much history
 * is available and whether a full seasonal cycle fits; the others force a
 * specific model. The value actually used is always echoed back in the
 * response ({@code ForecastSummaryResponse.method}).
 */
public enum ForecastMethod {

    /** Pick automatically from series length and seasonal coverage. */
    AUTO,

    /** Flat line at the running historical mean — the fallback for very short series. */
    NAIVE_MEAN,

    /** Simple exponential smoothing — level only, no trend, no seasonality. */
    SES,

    /** Holt's linear method — level + additive trend, no seasonality. */
    HOLT,

    /** Holt-Winters additive — level + trend + additive seasonal component. */
    HOLT_WINTERS_ADDITIVE
}
