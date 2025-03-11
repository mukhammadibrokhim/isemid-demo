package uz.uzinfocom.isemid.utils;

public interface ApiUrlConstant {

    String PREFIX = "/v1";

    String USER = PREFIX + "/user";

    String ROLE = PREFIX + "/role";

    String PERMISSION = PREFIX + "/permission";

    String ORGANIZATION = PREFIX + "/organization";

    String CATALOG = PREFIX + "/catalog";

    String CITIZEN = PREFIX + "/citizen";

    String FORM089 = PREFIX + "/form089";

    String FORM0058 = PREFIX + "/form058";

    String CARD = PREFIX + "/card";

    String MKB10 = PREFIX + "/mkb10";

    String DB = PREFIX + "/db";

    String JOURNAL_060 = PREFIX + "/journal-060";

    String VACCINATION = PREFIX + "/vaccination";

    String MANUAL_REPORT = PREFIX + "/manual-report";

    String AUTH = PREFIX + "/auth";
    String LEGAL_ENTITY = PREFIX + "/legal-entity";

//    String BASE_DOMAIN = "http://localhost:8081";
//    String BASE_DOMAIN = "https://ykem.sanepid.uz";

    String BASE_DOMAIN = "https://test-ykem.sanepid.uz/api";
    String BASE_DOMAIN_LOCAL = "http://localhost:8081";

//    ____________________________________________________________

    String BASE_API_URL = "https://test-api2.ssv.uz";

    String PRACTITIONER = BASE_API_URL + "/v2/Practitioner/%s";
    String GCP_CITIZEN = BASE_API_URL + "/v3/Citizen?nnuzb=%s&photo=%s";
    String GCP_CHILD = BASE_API_URL + "/v3/Child?series=%s&number=%s";
    String LEGAL_ENTITY_API = BASE_API_URL + "/v3/LegalEntity?tin=%s";
    String VACCINATION_NNUZB = "https://api2.ssv.uz/Vaccination/%s";
}
