package uz.uzinfocom.app.shared.constants.api;

public final class ApiPaths {

    private ApiPaths() {
    }

    public static final String API_V1 = "/v1";

    /**
     * Root for the entire admin-only API surface — settings, elevated-access
     * management, and admin-facing (unscoped, cross-organization) statistics.
     * Kept as one prefix so it can be gated as a single Spring Security
     * policy rule (see SecurityRouteCatalog) and served as its own Swagger
     * group (see OpenApiGroups.ADMIN).
     */
    public static final class Admin {
        private Admin() {
        }

        public static final String ROOT = API_V1 + "/admin";
    }

    public static final class Dashboard {
        private Dashboard() {
        }

        public static final String ROOT = API_V1 + "/dashboard";
        public static final String HOME = "/home";
        public static final String HOME_MODULE = "/home/{module}";
    }

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
        public static final String MKB10 = ROOT + "/mkb10";

        public static final String BY_ID = "/{id}";
        public static final String BY_CODE = "/code/{code}";
        public static final String BY_PARENT_CODE = "/by-parent-code/{parentCode}";
        public static final String BY_TYPE = "/types/{type}";
        public static final String BY_TYPE_AND_CODE = "/types/{type}/codes/{code}";
        public static final String BY_TYPE_AND_PARENT_CODE = "/types/{type}/parents/{parentCode}";
        public static final String BY_MKB10_CODE = "/mkb10/{code}";
        public static final String ROOTS = "/roots";
        public static final String CHILDREN = "/{id}/children";
    }

    public static final class SystemSetting {
        private SystemSetting() {
        }

        public static final String ROOT = Admin.ROOT + "/settings";
        public static final String BY_ID = "/{id}";
        public static final String BY_KEY = "/by-key/{key}";
        public static final String RESTORE = "/{id}/restore";
    }

    /**
     * Human-facing admin tooling for provisioning inbound-integration clients
     * (see {@link Integration}) — lives under the existing {@link Admin} root,
     * not under {@code Integration.ROOT}, since it's authenticated the same
     * way as every other admin endpoint (human SSO JWT + adminAccessGuard),
     * not by the machine clients it manages.
     */
    public static final class IntegrationClient {
        private IntegrationClient() {
        }

        public static final String ROOT = Admin.ROOT + "/integration-clients";
        public static final String BY_ID = "/{id}";
        public static final String REVOKE = "/{id}/revoke";
    }

    public static final class Form058 {
        private Form058() {
        }

        public static final String ROOT = API_V1 + "/form-058";
        public static final String BY_ID = "/{id}";
        public static final String APPROVE = "/{id}/approve";
        public static final String NOT_APPROVE = "/{id}/not-approve";
        public static final String CANCEL = "/{id}/cancel";
        public static final String CARDS = "/{id}/cards";
        public static final String ASSIGN_CARDS = "/{id}/cards/assign";
        public static final String PDF = "/{id}/pdf";

    }

    public static final class Form058Stats {
        private Form058Stats() {
        }

        public static final String ROOT = Form058.ROOT + "/stats";
        public static final String BY_STATUS = "/by-status";
        public static final String BY_DATE = "/by-date";
        public static final String TOP_MKB10 = "/top-mkb10";
    }

    public static final class Form058AdminStats {
        private Form058AdminStats() {
        }

        public static final String ROOT = Admin.ROOT + "/form-058/stats";
        public static final String BY_STATUS = "/by-status";
        public static final String BY_SENDER_ORGANIZATION = "/by-sender-organization";
        public static final String BY_RECEIVER_ORGANIZATION = "/by-receiver-organization";
        public static final String BY_DATE = "/by-date";
        public static final String TOP_MKB10 = "/top-mkb10";
    }

    public static final class Form0581 {
        private Form0581() {
        }

        public static final String ROOT = API_V1 + "/form-058-1";
        public static final String BY_ID = "/{id}";
        public static final String BY_DOCUMENT_VALUE = "/by-document";
        public static final String APPROVE = "/{id}/approve";
        public static final String NOT_APPROVE = "/{id}/not-approve";
        public static final String CANCEL = "/{id}/cancel";
    }

    public static final class Form0581Stats {
        private Form0581Stats() {
        }

        public static final String ROOT = Form0581.ROOT + "/stats";
        public static final String BY_STATUS = "/by-status";
        public static final String BY_DATE = "/by-date";
        public static final String TOP_MKB10 = "/top-mkb10";
    }

    public static final class Form0581AdminStats {
        private Form0581AdminStats() {
        }

        public static final String ROOT = Admin.ROOT + "/form-058-1/stats";
        public static final String BY_STATUS = "/by-status";
        public static final String BY_SENDER_ORGANIZATION = "/by-sender-organization";
        public static final String BY_RECEIVER_ORGANIZATION = "/by-receiver-organization";
        public static final String BY_DATE = "/by-date";
        public static final String TOP_MKB10 = "/top-mkb10";
    }

    public static final class Card {
        private Card() {
        }

        public static final String ROOT = API_V1 + "/cards";
        public static final String BY_ID = "/{id}";

        // Personal, server-scoped list view — never trust a client-supplied
        // user id for this, always resolve from the authenticated principal.
        // For a broader-scope organization (region/republic level SANEPID),
        // this widens from "assigned to me" to the whole organization scope
        // — see CardQueryService.findMine.
        public static final String MINE = "/mine";

        // Attached-employee actions (the user working the card).
        public static final String ACCEPT = "/{id}/accept";
        public static final String REJECT = "/{id}/reject";
        public static final String COMPLETE = "/{id}/complete";

        // Supervisor actions (the user the card is assigned to via
        // assignedById) — nested under /supervisor/ so the two reviewer
        // roles can never be confused with each other at the URL level.
        public static final String SUPERVISOR_APPROVE = "/{id}/supervisor/approve";
        public static final String SUPERVISOR_REJECT = "/{id}/supervisor/reject";
        public static final String SUPERVISOR_REASSIGN = "/{id}/supervisor/reassign";

        public static final String ACTS = "/{id}/acts";
    }

    public static final class Act {
        private Act() {
        }

        public static final String ROOT = API_V1 + "/acts";
        public static final String BY_ID = "/{id}";

        // Personal, server-scoped list view — same rule as Card.MINE.
        public static final String MINE = "/mine";

        // Attached-employee actions (the user working the act).
        public static final String ACCEPT = "/{id}/accept";
        public static final String REJECT = "/{id}/reject";
        public static final String COMPLETE = "/{id}/complete";

        // Supervisor actions (the user the act is assigned to via
        // assignedById) — nested under /supervisor/ just like Card's.
        public static final String SUPERVISOR_APPROVE = "/{id}/supervisor/approve";
        public static final String SUPERVISOR_REJECT = "/{id}/supervisor/reject";
        public static final String SUPERVISOR_REASSIGN = "/{id}/supervisor/reassign";
    }

    public static final class ExternalApi {
        private ExternalApi() {
        }

        public static final String ISEMID_CALLBACK = API_V1 + "/acts/lis/callback/";
        public static final String LIS_ACT_API = "/api/lis/labs/%s/acts/%s?allowedDuplicate=%s";
        public static final String LIS_RESEARCH_TYPE_API = "/api/lis/research-types/%s/template-id";
    }

    /**
     * Root for the inbound-integration API surface: external systems
     * (labs, hospital information systems, other registries) submitting case
     * data directly, authenticated as a registered {@link IntegrationClient}
     * rather than a human SSO/DHP user. Deliberately kept OUTSIDE
     * {@link #API_V1} — a fully separate namespace from the frontend-facing
     * API, so the two surfaces can never be confused in routing, logging, or
     * security policy.
     */
    public static final class Integration {
        private Integration() {
        }

        public static final String ROOT = "/integration/v1";
        public static final String OAUTH_TOKEN = ROOT + "/oauth/token";

        /**
         * {source} identifies which system is calling — e.g.
         * /integration/v1/lab-x/form-058. For an integration-client caller it
         * must match the client's own registered sourceKey (see
         * InboundCallerContext); it is not a free-form value a caller can
         * pick per request.
         */
        public static final String FORM058 = ROOT + "/{source}/form-058";
        public static final String FORM0581 = ROOT + "/{source}/form-058-1";

        /**
         * DMED-specific form058 endpoint, kept separate from {@link #FORM058}
         * because DMED already integrates against a fixed, flat request
         * shape ({@code DmedCreateForm058Request}) that must not shift to the
         * generic endpoint's entity-mirroring nested structure. A literal
         * path, not a {@code {source}} pattern — this is the one form058
         * contract that isn't meant to generalize.
         */
        public static final String DMED_FORM058 = ROOT + "/DMED/form-058";
    }

}
