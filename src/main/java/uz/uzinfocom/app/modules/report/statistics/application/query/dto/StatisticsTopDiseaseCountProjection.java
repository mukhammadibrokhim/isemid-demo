package uz.uzinfocom.app.modules.report.statistics.application.query.dto;

/**
 * One raw aggregate row for the "Statistika" disease ranking — an ICD-10
 * {@code final_icd10_code} value and the number of confirmed ({@code status =
 * 'APPROVED'}) {@code form058} + {@code form058_1} cases carrying it over the
 * requested period and organization scope. The query service resolves the
 * localized name and applies the {@code limit}.
 */
public record StatisticsTopDiseaseCountProjection(String diagnosisCode, long confirmedCount) {
}
