package uz.uzinfocom.isemid.features.report.dtos;

public record CaseCountDynamicsData(
    String period,
    String locationCode,
    int confirmed,
    int unconfirmed,
    int male,
    int female,
    int age_0_3,
    int age_3_6,
    int age_6_10,
    int age_10_17,
    int age_18_25,
    int age_25_45,
    int age_45_65,
    int age_65_plus
) {}
