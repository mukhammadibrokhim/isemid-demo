package uz.uzinfocom.isemid.features.report.dtos;

public record CaseCountDynamicsData(
    String period,
    String locationCode,
    int confirmed,
    int unconfirmed,
    int male,
    int female
    ) {

}
