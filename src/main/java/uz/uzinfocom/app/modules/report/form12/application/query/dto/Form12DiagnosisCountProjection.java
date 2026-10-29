package uz.uzinfocom.app.modules.report.form12.application.query.dto;

/**
 * One row of the "Form 12" root-level aggregation: confirmed case counts for a
 * single confirmed final-diagnosis code ({@code final_icd10_code}, no fallback
 * to the initial {@code icd10_code}), across the caller's whole access scope. {@code
 * Form12ReportQueryService} rolls these per-code rows up to one row per {@code
 * FORM_12}-tagged manual-report catalog entry by summing the codes in each
 * entry's ICD-10 set.
 */
public record Form12DiagnosisCountProjection(String code, long total, long under14, long under18) {
}
