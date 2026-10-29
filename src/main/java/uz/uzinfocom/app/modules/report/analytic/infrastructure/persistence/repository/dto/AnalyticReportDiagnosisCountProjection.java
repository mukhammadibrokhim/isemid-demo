package uz.uzinfocom.app.modules.report.analytic.infrastructure.persistence.repository.dto;

/** One confirmed-case count for one ICD-10 code, within one region's organization-id set. */
public record AnalyticReportDiagnosisCountProjection(String diagnosisCode, long count) {
}
