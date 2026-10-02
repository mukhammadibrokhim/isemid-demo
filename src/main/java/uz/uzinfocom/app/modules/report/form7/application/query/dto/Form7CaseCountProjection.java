package uz.uzinfocom.app.modules.report.form7.application.query.dto;

/**
 * Raw auto-computed count block produced by {@code Form7CaseCountRepository}
 * for a single organization and reporting period — the "Hisobot davrida
 * ro'yxatga olingan bemorlar" cuts plus "Birlamchi tashxis tasdiqlandi".
 * Internal to the report's query service; the web layer never sees this
 * shape directly (see {@link Form7EntryPrefillResponse} /
 * {@link Form7EntryTableResponse}).
 * <p>
 * {@code under14}/{@code under18}/{@code adult}/{@code female} are
 * independent, overlapping cuts of {@code total} (not a mutually exclusive
 * partition), matching {@code Form1ReportRepository}'s definitions.
 */
public record Form7CaseCountProjection(
        long total,
        long under14,
        long under18,
        long adult,
        long female,
        long primaryDiagnosisConfirmed
) {
    public static final Form7CaseCountProjection EMPTY = new Form7CaseCountProjection(0, 0, 0, 0, 0, 0);
}
