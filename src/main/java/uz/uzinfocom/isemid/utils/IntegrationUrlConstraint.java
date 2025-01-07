package uz.uzinfocom.isemid.utils;

public interface IntegrationUrlConstraint {

    String BASE_API_URL = "https://test-api2.ssv.uz";

    String MIS_ORGANIZATION = BASE_API_URL + "/v2/Organization/%s";
    String MIS_PRACTITIONER = BASE_API_URL + "/v2/Practitioner/%s";
}
