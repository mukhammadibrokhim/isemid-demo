package uz.uzinfocom.isemid.features.report.projection;

public record MonitoringDiseaseRow(
        String regionCode, String diseaseCode, long casesCount
) {
}