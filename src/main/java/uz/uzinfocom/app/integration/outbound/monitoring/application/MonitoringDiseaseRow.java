package uz.uzinfocom.app.integration.outbound.monitoring.application;

/** Cases of one ICD-10 block ({@code UNCLASSIFIED} when none resolves) within one location. */
public record MonitoringDiseaseRow(String locationCode, String diseaseCode, long casesCount) {
}
