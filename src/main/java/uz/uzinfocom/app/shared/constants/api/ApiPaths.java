package uz.uzinfocom.app.shared.constants.api;

public final class ApiPaths {

    private ApiPaths() {
    }

    public static final String API_V1 = "/v1";

    public static final class Organization {

        private Organization() {
        }

        public static final String ROOT = API_V1 + "/organizations";

        public static final String LOOKUP = "/lookup";
        public static final String BY_ID = "/{id}";
        public static final String USERS_BY_ORGANIZATION_ID = "/{id}/users";

        public static final String ID = "id";

    }

    public static final class User {

        private User() {
        }

        public static final String ROOT = API_V1 + "/users";

        public static final String ME = "/me";
        public static final String BY_ID = "/{id}";
        public static final String BY_UUID = "/by-uuid/{uuid}";
        public static final String ORGANIZATIONS = "/{id}/organizations";

        public static final String ID = "id";
    }

    public static final class Role {

        private Role() {
        }

        public static final String ROOT = API_V1 + "/roles";

        public static final String BY_ID = "/{id}";

        public static final String PERMISSIONS = "/{id}/permissions";

        public static final String REMOVE_PERMISSIONS = "/{id}/permissions/remove";

        public static final String RESTORE = "/{id}/restore";
    }

    public static final class Permission {
        public static final String ROOT = API_V1 + "/permissions";

        public static final String BY_ID = "/{id}";
        public static final String RESTORE = "/{id}/restore";
    }

    public static final class Scope {
        private Scope() {
        }

        public static final String ROOT = API_V1 + "/scopes";
        public static final String CURRENT = "/current";
    }

    public static final class Reference {
        private Reference() {
        }

        public static final String ROOT = API_V1 + "/references";

        public static final String COUNTRIES = ROOT + "/countries";
        public static final String REGIONS = ROOT + "/regions";
        public static final String DISTRICTS = ROOT + "/districts";
        public static final String NEIGHBORHOODS = ROOT + "/neighborhood";
        public static final String CATALOGS = ROOT + "/catalogs";
        public static final String MANUAL_REPORTS = ROOT + "/manual-reports";

        public static final String BY_ID = "/{id}";
        public static final String BY_CODE = "/code/{code}";
        public static final String BY_PARENT_CODE = "/by-parent-code/{parentCode}";
        public static final String BY_TYPE = "/types/{type}";
        public static final String BY_TYPE_AND_CODE = "/types/{type}/codes/{code}";
        public static final String BY_TYPE_AND_PARENT_CODE = "/types/{type}/parents/{parentCode}";
        public static final String BY_MKB10_CODE = "/mkb10/{code}";
    }

    public static final class Form058 {
        private Form058() {
        }

        public static final String ROOT = API_V1 + "/form-058";
        public static final String BY_ID = "/{id}";
        public static final String APPROVE = "/{id}/approve";
        public static final String NOT_APPROVE = "/{id}/not-approve";
        public static final String CANCEL = "/{id}/cancel";

    }

    public static final class Card {
        private Card() {
        }

        public static final String ROOT = API_V1 + "/cards";
    }

    public static final class Act {
        private Act() {
        }

        public static final String ROOT = API_V1 + "/acts";
    }

    public static final class ExternalApi {
        private ExternalApi() {
        }

        public static final String ISEMID_CALLBACK = API_V1 + "/acts/lis/callback/";
        public static final String LIS_ACT_API = "/api/lis/labs/%s/acts/%s?allowedDuplicate=%s";
        public static final String LIS_RESEARCH_TYPE_API = "/api/lis/research-types/%s/template-id";
    }

}
