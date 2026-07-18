package uz.uzinfocom.app.platform.dashboard.application.query.dto;

/**
 * Only {@code MONTH} exists today (every dashboard trend uses the same
 * 6-month monthly window) — kept as its own enum rather than a bare string
 * so a future weekly/daily trend has somewhere well-typed to plug into,
 * both in code and in the generated OpenAPI schema.
 */
public enum TimeSeriesGranularity {
    MONTH
}
