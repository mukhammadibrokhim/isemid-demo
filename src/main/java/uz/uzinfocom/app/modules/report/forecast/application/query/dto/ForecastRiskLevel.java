package uz.uzinfocom.app.modules.report.forecast.application.query.dto;

/**
 * Coarse risk label for one disease in the "top diseases" ranking, derived
 * from its own forecast: {@link #HIGH} — at least one future bucket is
 * predicted above the endemic threshold; {@link #MEDIUM} — no threshold
 * breach, but the trend is still rising; {@link #LOW} — neither.
 */
public enum ForecastRiskLevel {
    HIGH,
    MEDIUM,
    LOW
}
