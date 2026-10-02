package uz.uzinfocom.app.modules.report.form282.application.query.dto;

/**
 * One row of the "Form 28.2" root-level aggregation: confirmed ({@code status =
 * 'APPROVED'}) case counts for a single confirmed final-diagnosis code ({@code
 * final_icd10_code}, no fallback to the initial {@code icd10_code}), across the
 * caller's whole access scope. {@code Form282ReportQueryService} rolls these
 * per-code rows up to one row per {@code FORM_28_2}-tagged manual-report catalog
 * entry by summing the codes in each entry's ICD-10 set.
 */
public record Form282DiagnosisCountProjection(String code, Form282Counts counts) {
}
