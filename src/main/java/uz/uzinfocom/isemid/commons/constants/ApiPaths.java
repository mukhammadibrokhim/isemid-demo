package uz.uzinfocom.isemid.commons.constants;

public final class ApiPaths {
    public static final String BASE_PATH = "/v1";

    public static final class Act {
        public static final String ROOT = BASE_PATH + "/act";
    }

    public static final class Permission {
        public static final String ROOT = BASE_PATH + "/permission";
    }

    public static final class Catalog {
        public static final String ROOT = BASE_PATH + "/catalog";
    }

    public static final class User {
        public static final String ROOT = BASE_PATH + "/user";
    }

    public static final class Organization {
        public static final String ROOT = BASE_PATH + "/organization";
    }

    public static final class Role {
        public static final String ROOT = BASE_PATH + "/role";
    }

    public static final class ManualReport {
        public static final String ROOT = BASE_PATH + "/manual-report";
    }

    public static final class Mkb10 {
        public static final String ROOT = BASE_PATH + "/mkb10";
    }

    public static final class Auth {
        public static final String ROOT = BASE_PATH + "/auth";
    }

    public static class Form058 {
        public static final String ROOT = BASE_PATH + "/form058";
    }

    public static class Card {
        public static final String ROOT = BASE_PATH + "/card";
    }

    public static final class Citizen {
        public static final String ROOT = BASE_PATH + "/citizen";
    }

    public static final class LegalEntity {
        public static final String ROOT = BASE_PATH + "/legal-entity";
    }

    public static final class Vaccination {
        public static final String ROOT = BASE_PATH + "/vaccination";
    }

    public final static class Db {
        public static final String ROOT = BASE_PATH + "/db";
    }
    public final static class Log {
        public static final String ROOT = BASE_PATH + "/log";
    }

    public static final class Report {
        public static final String ROOT = BASE_PATH  + "/report";
        public static final String FORM1 = ROOT + "/form1";
        public static final String FORM_2 = ROOT + "/form-2";
        public static final String FORM_3_1 = ROOT + "/form-3-1";
        public static final String FORM_3_2 = ROOT + "/form-3-2";
        public static final String FORM_4 = ROOT + "/form-4";
        public static final String FORM6= ROOT + "/form-6";
        public static final String FORM7= ROOT + "/form-7";
        public static final String FORM9 = ROOT + "/form-9";
        public static final String FORM10 = ROOT + "/form-10";
        public static final String FORM11 = ROOT + "/form-11";
        public static final String FORM12 = ROOT + "/form12";
        public static final String FORM13 = ROOT + "/form-13";
        public static final String FORM28_1 = ROOT  + "/form-28-1";
        public static final String FORM28_2 = ROOT + "/form-28-2";
        public static final String FORM12_7 = ROOT + "/form-12-7";
        public static final String LOCATION = ROOT + "/by-location";
        public static final String MONITORING = ROOT + "/monitoring";
    }

    public static final class AnalyticReport {
        public static final String ROOT = BASE_PATH + "/analytic-reports";
    }

    public static final class AnalyticTemplate {
        public static final String ROOT = BASE_PATH + "/analytic-templates";
    }

    public static final class BaseDomain {
        public static final String ROOT = "https://ykem.sanepid.uz/api";
        public static final String ROOT_DEV = "https://test-ykem.sanepid.uz/api";
        public static final String ROOT_LOCAL = "http://localhost:8081";
        private BaseDomain() {}

    }

    public static final class Info {
        public static final String ROOT = BASE_PATH + "/info";
    }

    public static final class ExternalApi {
        public static final String BASE_API_URL = "https://test-api2.ssv.uz";
        public static final String SSO_TOKEN = "https://test-sso.ssv.uz/oauth/token";
        public static final String SSO_LOGOUT = "https://test-sso.ssv.uz/api/oauth/logout";
        public static final String LIS_URL = "https://test-lis.sanepid.uz/api/v1/act/integration/";
        public static final String ISEMID_CALLBACK = "https://test-ykem.sanepid.uz/api/v1/act/lis/callback/";

//        public static final String BASE_API_URL = "https://api2.ssv.uz";
//        public static final String SSO_TOKEN = "https://sso.ssv.uz/oauth/token";
//        public static final String SSO_LOGOUT = "https://sso.ssv.uz/api/oauth/logout";
//        public static final String LIS_URL = "https://lis.sanepid.uz/api/v1/act/integration/";
//        public static final String ISEMID_CALLBACK = "https://ykem.sanepid.uz/api/v1/act/lis/callback/";

        public static final String PRACTITIONER = BASE_API_URL + "/v2/Practitioner/%s";
        public static final String ORGANIZATION = BASE_API_URL + "/v2/Organization/%s";
        public static final String GCP_CITIZEN = BASE_API_URL + "/v3/Citizen?nnuzb=%s&birth_date=%s&photo=%s";
        public static final String GCP_CHILD = BASE_API_URL + "/v3/Child?series=%s&number=%s";
        public static final String LEGAL_ENTITY_API = BASE_API_URL + "/v3/LegalEntity?tin=%s";
        public static final String VACCINATION_NNUZB = "https://api2.ssv.uz/Vaccination/%s";

        private ExternalApi() {
        }
    }
}
