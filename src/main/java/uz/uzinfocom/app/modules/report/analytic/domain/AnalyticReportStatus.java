package uz.uzinfocom.app.modules.report.analytic.domain;

/** Which button the caller saved an {@link AnalyticReport} with. */
public enum AnalyticReportStatus {
    /** "Shablon sifatida saqlash" — a reusable filter+content skeleton the caller reopens and re-saves later. */
    TEMPLATE,
    /** "Saqlash" — a finished report. */
    FINAL
}
