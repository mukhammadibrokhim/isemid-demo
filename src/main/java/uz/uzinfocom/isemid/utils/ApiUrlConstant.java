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

    String DB = PREFIX + "/db";

//    String BASE_DOMAIN = "http://localhost:8081";
    String BASE_DOMAIN = "https://ykem.sanepid.uz";

//    ____________________________________________________________

    String BASE_API_URL = "https://test-api2.ssv.uz";

    String PRACTITIONER = BASE_API_URL + "/v2/Practitioner/%s";
    String GCP_CITIZEN = BASE_API_URL + "/v3/Citizen?nnuzb=%s&photo=%s";
    String GCP_CHILD = BASE_API_URL + "/v3/Child?series=%s&number=%s";

}
