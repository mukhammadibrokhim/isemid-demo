package uz.uzinfocom.app.shared.constants.api;

public final class ApiPaths {

    private ApiPaths() {
    }

    public static final String API_V1 = "/v1";

    public static final class Organization {

        private Organization() {
        }

        public static final String BASE = API_V1 + "/organizations";

        public static final String LOOKUP = "/lookup";
        public static final String BY_ID = "/{id}";
        public static final String USERS_BY_ORGANIZATION_ID = "/{id}/users";

        public static final String ID = "id";

    }

    public static final class User {

        private User() {
        }

        public static final String BASE = API_V1 + "/users";

        public static final String ME = "/me";
        public static final String BY_ID = "/{id}";
        public static final String BY_UUID = "/by-uuid/{uuid}";
        public static final String ORGANIZATIONS = "/{id}/organizations";

        public static final String ID = "id";
    }

    public static final class Role {

        private Role() {
        }

        public static final String BASE = API_V1 + "/roles";

        public static final String BY_ID = "/{id}";

        public static final String PERMISSIONS = "/{id}/permissions";

        public static final String REMOVE_PERMISSIONS = "/{id}/permissions/remove";

        public static final String RESTORE = "/{id}/restore";
    }

    public static final class Permission {
        public static final String BASE = API_V1 + "/permissions";

        public static final String BY_ID = "/{id}";
        public static final String RESTORE = "/{id}/restore";
    }

    public static final class Scope {
        private Scope() {
        }

        public static final String BASE = API_V1 + "/scopes";
        public static final String CURRENT = "/current";
    }

    public static final class Reference {
        private Reference() {
        }

        public static final String BASE = "/api/v1/references";

        public static final String COUNTRIES = BASE + "/countries";
        public static final String REGIONS = BASE + "/regions";
        public static final String DISTRICTS = BASE + "/districts";
        public static final String NEIGHBORHOODS = BASE + "/mahallas";

        public static final String BY_ID = "/{id}";
        public static final String BY_CODE = "/code/{code}";
        public static final String BY_PARENT_CODE = "/by-parent-code/{parentCode}";
    }

}
