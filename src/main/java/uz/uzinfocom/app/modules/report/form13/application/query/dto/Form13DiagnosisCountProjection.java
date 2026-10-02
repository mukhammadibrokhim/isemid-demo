package uz.uzinfocom.app.modules.report.form13.application.query.dto;

/**
 * One row of the "Form 13" scope-wide aggregation: confirmed case counts for a
 * single confirmed final-diagnosis code ({@code final_icd10_code}), across the
 * caller's whole access scope (no organization attribution). {@code
 * Form13GeographyCountSource} rolls these per-code rows up
 * to one {@link Form13Metric} per {@code FORM_13}-tagged manual-report entry —
 * used for the drill-down root ("Jami") node.
 */
public record Form13DiagnosisCountProjection(String code, long total, long under14, long under18) {
}
