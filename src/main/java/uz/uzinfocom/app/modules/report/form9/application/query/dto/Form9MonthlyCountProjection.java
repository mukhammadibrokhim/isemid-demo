package uz.uzinfocom.app.modules.report.form9.application.query.dto;

/**
 * Raw per-calendar-month count produced by {@code
 * Form9ReportRepository#countMonthlyBreakdown} — one row per month that
 * actually has data ({@code month} is 1..12, in the application reporting
 * zone). Months with no cases are simply absent; the query service fills the
 * gaps with zeros when it emits the fixed 12-row response.
 */
public record Form9MonthlyCountProjection(int month, long registered, long hospitalized) {
}
