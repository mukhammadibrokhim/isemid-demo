package uz.uzinfocom.app.shared.constants.api;

public final class ApiPaths {

    private ApiPaths() {
    }

    public static final String API_V1 = "/v1";

    /**
     * Root for the entire admin-only API surface — settings, elevated-access
     * management, and admin-facing (unscoped, cross-organization) statistics.
     * Kept as one prefix so it can be gated as a single Spring Security
     * policy rule (see RouteAccessPolicy/RouteAccessPolicyResolver) and
     * served as its own Swagger group (see OpenApiGroups.ADMIN).
     */
    public static final class Admin {
        private Admin() {
        }

        public static final String ROOT = API_V1 + "/admin";
    }

    /**
     * Login-proxy surface: exchanges an end user's credentials for a token
     * by calling an external authentication provider's token endpoint on the
     * caller's behalf (see platform.ssoproxy) - one endpoint regardless of which
     * OAuth2 grant the resolved provider speaks. Public - see
     * the {@code /v1/auth/**} row in {@code RouteAccessPolicy}.
     */
    public static final class Auth {
        private Auth() {
        }

        public static final String ROOT = API_V1 + "/auth";
        public static final String LOGIN = "/login/{provider}";
        public static final String REFRESH = "/refresh/{provider}";
        public static final String LOGOUT = "/logout/{provider}";
        public static final String PROVIDER = "provider";
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

    public static final class Action {
        private Action() {
        }

        public static final String ROOT = API_V1 + "/actions";

        public static final String BY_ID = "/{id}";
        public static final String RESTORE = "/{id}/restore";
    }

    public static final class Scope {
        private Scope() {
        }

        public static final String ROOT = API_V1 + "/scopes";
        public static final String CURRENT = "/current";
    }

    /**
     * Outbound API2 lookup: we call out to the external API2 system to fetch
     * citizen data on a caller's behalf. More lookup types (beyond
     * NNUZB/PPN/CZ) are expected to land under this same root later - see
     * {@code CitizenLookupType}.
     */
    public static final class Citizen {
        private Citizen() {
        }

        public static final String ROOT = API_V1 + "/citizen";
    }

    /**
     * Outbound API2 lookup: we call out to the external API2 system to fetch
     * legal-entity (TIN) data on a caller's behalf.
     */
    public static final class LegalEntity {
        private LegalEntity() {
        }

        public static final String ROOT = API_V1 + "/legal-entity";
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
        public static final String ICD10 = ROOT + "/icd10";

        public static final String BY_ID = "/{id}";
        public static final String BY_CODE = "/code/{code}";
        public static final String BY_PARENT_CODE = "/by-parent-code/{parentCode}";
        public static final String BY_TYPE = "/types/{type}";
        public static final String BY_TYPE_AND_CODE = "/types/{type}/codes/{code}";
        public static final String BY_TYPE_AND_PARENT_CODE = "/types/{type}/parents/{parentCode}";
        public static final String BY_ICD10_CODE = "/icd10/{code}";
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
     * Runtime-editable route-access rules — replaces the old, restart-only
     * {@code SecurityRouteCatalog}. Lives under {@link Admin} (SSO-admin
     * authenticated), mirrored read/write under {@link Dev} for the
     * dev panel (see {@code DevRoutePolicyController}).
     */
    public static final class RouteAccessPolicy {
        private RouteAccessPolicy() {
        }

        public static final String ROOT = Admin.ROOT + "/route-policies";
        public static final String BY_ID = "/{id}";
    }

    /**
     * Human-facing admin tooling for provisioning inbound-integration clients
     * (see {@link Integration}) — lives under the existing {@link Admin} root,
     * not under {@code Integration.ROOT}, since it's authenticated the same
     * way as every other admin endpoint (human SSO JWT + adminAccessGuard),
     * not by the machine clients it manages. Mirrored read/write under
     * {@link Dev} for the dev panel (see
     * {@code DevIntegrationClientController}), same as {@link RouteAccessPolicy}.
     */
    public static final class IntegrationClient {
        private IntegrationClient() {
        }

        public static final String ROOT = Admin.ROOT + "/integration-clients";
        public static final String BY_ID = "/{id}";
        public static final String REVOKE = "/{id}/revoke";
        public static final String ALLOWED_IPS = "/{id}/allowed-ips";
        public static final String WEBHOOK = "/{id}/webhook";

        /**
         * Distinct {@code sourceKey} values of active clients — lets a caller
         * populate the {@code {source}} path segment of {@link Integration}'s
         * endpoints from a live list instead of a hand-typed string.
         */
        public static final String SOURCE_KEYS = "/source-keys";
    }

    /**
     * Developer monitoring panel - login/error history and system metrics for
     * ops/dev accounts. Deliberately NOT under {@link Admin}: these accounts
     * authenticate via their own local {@code DevUser} credentials (HTTP Basic
     * on a dedicated {@code SecurityFilterChain}, see
     * {@code DevPanelSecurityConfig}), not the external SSO/DHP bearer JWT
     * every other endpoint in this app requires - so this whole subtree is
     * intentionally outside the {@code isemid_admin}/{@code isemid_super_admin}
     * authorization model.
     */
    public static final class Dev {
        private Dev() {
        }

        public static final String ROOT = API_V1 + "/dev";
        public static final String ERRORS = "/errors";
        public static final String ERROR_BY_ID = "/errors/{id}";

        /**
         * Login-attempt (success and failure) history against
         * {@code /v1/auth/login/{provider}}. {@link #LOGINS} returns the
         * table/list view (summary columns); {@link #LOGIN_BY_ID} returns
         * the full detail (failure reason, user agent, trace id) for one row.
         */
        public static final String LOGINS = "/logins";
        public static final String LOGIN_BY_ID = "/logins/{id}";

        /**
         * Full per-request resource-usage log (every request, not just
         * failures - see {@link #ERRORS}) captured by {@code RequestLoggingFilter}
         * into {@code dev_request_log}. {@link #REQUESTS} returns the table/list
         * view (summary columns); {@link #REQUEST_BY_ID} returns the full
         * detail (query string, content types, exception/root-cause, message)
         * for one row.
         */
        public static final String REQUESTS = "/requests";
        public static final String REQUEST_BY_ID = "/requests/{id}";

        /**
         * Single multiplexed SSE connection for the dev panel - pushes
         * {@code system}, {@code http} (periodic metrics snapshots, replacing
         * what used to be plain {@code GET /metrics/system} / {@code /metrics/http}
         * polling endpoints) and {@code error} (new {@link #ERRORS} row) named
         * events over one stream instead of one connection per data source, to
         * stay well under the browser's per-origin connection limit.
         */
        public static final String METRICS_STREAM = "/metrics/stream";
        public static final String SETTINGS = "/settings";
        public static final String SETTINGS_BY_ID = "/settings/{id}";
        public static final String SETTINGS_BY_KEY = "/settings/by-key/{key}";
        public static final String SETTINGS_RESTORE = "/settings/{id}/restore";
        public static final String ROUTE_POLICIES = "/settings/route-policies";
        public static final String ROUTE_POLICY_BY_ID = "/settings/route-policies/{id}";
        public static final String FILES = "/files";
        public static final String FILES_DOWNLOAD = "/files/download";
        public static final String AUDIT = "/audit";
        public static final String AUDIT_BY_ID = "/audit/{id}";
        public static final String INTEGRATION_CLIENTS = "/integration-clients";
        public static final String INTEGRATION_CLIENT_BY_ID = "/integration-clients/{id}";
        public static final String INTEGRATION_CLIENT_REVOKE = "/integration-clients/{id}/revoke";
        public static final String INTEGRATION_CLIENT_ALLOWED_IPS = "/integration-clients/{id}/allowed-ips";
        public static final String INTEGRATION_CLIENT_WEBHOOK = "/integration-clients/{id}/webhook";
        public static final String INTEGRATION_CLIENT_SOURCE_KEYS = "/integration-clients/source-keys";

        /**
         * Operational visibility for the outbound status-change webhook queue
         * (see {@code orchestration.webhook}) - dev-panel only, no admin-facing
         * equivalent: this is purely operational (dispatch status, manual
         * retry), not something an org admin configures.
         */
        public static final String WEBHOOK_DISPATCHES = "/webhook-dispatches";
        public static final String WEBHOOK_DISPATCH_BY_ID = "/webhook-dispatches/{id}";
        public static final String WEBHOOK_DISPATCH_RETRY = "/webhook-dispatches/{id}/retry";

        /**
         * Dev-panel account management (list/create/revoke) - deliberately
         * NOT under {@link Admin}: not even an SSO {@code isemid_super_admin}
         * may manage these accounts. Gated to {@code ROLE_DEV_ROOT} (see
         * {@code DevUserController}), a privilege only another root
         * dev-user can grant.
         */
        public static final String DEV_USERS = "/dev-users";
        public static final String DEV_USER_BY_ID = "/dev-users/{id}";
        public static final String DEV_USER_REVOKE = "/dev-users/{id}/revoke";
    }

    /**
     * Audit trail for business events (creation, status change, receiving-organization
     * reassignment) on Form058/Form0581/Act — see {@code AuditEventListener}. Read-only
     * here: {@code isemid_super_admin} and {@code isemid_admin} may both list/inspect
     * (see {@code @adminAccessGuard.isAdmin()} on {@code AuditQueryController}); the
     * dev-panel gets the same data, unrestricted, under {@link Dev#AUDIT}.
     */
    public static final class Audit {
        private Audit() {
        }

        public static final String ROOT = Admin.ROOT + "/audit";
        public static final String BY_ID = "/{id}";
    }

    /**
     * Background Excel export jobs (see {@code platform.export}) - one generic surface
     * shared by every module wired up as an {@code ExcelExportSource}: submission stays
     * per-module (e.g. {@link Form058#EXPORT}), but "my files" listing, SSE progress and
     * download are the same three endpoints regardless of which module produced the job.
     */
    public static final class Export {
        private Export() {
        }

        public static final String ROOT = API_V1 + "/exports";
        public static final String BY_ID = "/{id}";
        public static final String PROGRESS = "/{id}/progress";
        public static final String DOWNLOAD = "/{id}/download";
    }

    /**
     * In-app notifications — no message broker in this deployment, so events
     * (Form058/Form058-1 received, Card/Act assigned, LIS response) are fanned
     * out to {@code notification} rows in-process (see {@code orchestration.notification})
     * and delivered here: paged list, unread badge, mark-read, and an SSE stream
     * for live updates. Per-type enablement lives in the existing
     * {@code system_settings} store, editable from {@link Dev#SETTINGS} — no
     * separate admin surface for that.
     */
    public static final class Notification {
        private Notification() {
        }

        public static final String ROOT = API_V1 + "/notifications";
        public static final String UNREAD_COUNT = "/unread-count";
        public static final String UNREAD_COUNT_BY_TYPE = "/unread-count/by-type";
        public static final String BY_ID_READ = "/{id}/read";
        public static final String READ_ALL = "/read-all";
        public static final String STREAM = "/stream";
    }

    public static final class Form058 {
        private Form058() {
        }

        public static final String ROOT = API_V1 + "/form-058";
        public static final String BY_ID = "/{id}";
        public static final String ACCEPT = "/{id}/accept";
        public static final String APPROVE = "/{id}/approve";
        public static final String CANCEL = "/{id}/cancel";
        public static final String REOPEN = "/{id}/reopen";
        public static final String CARDS = "/{id}/cards";
        public static final String ASSIGN_CARDS = "/{id}/cards/assign";
        public static final String PDF = "/{id}/pdf";
        public static final String EXPORT = "/export";

        // Separate from the root listing (direction-scoped: sender/receiver)
        // on purpose — an "affiliated" form is visible for a completely
        // different reason (the patient's workplace/place of study matches
        // the current organization, regardless of who sent/received it), so
        // it gets its own endpoint/filter instead of a hidden mode-switch
        // query flag. Mirrors how GET /cards/mine stays separate from the
        // root card listing.
        public static final String AFFILIATED = "/affiliated";

    }

    public static final class Form058Stats {
        private Form058Stats() {
        }

        public static final String ROOT = Form058.ROOT + "/stats";
        public static final String BY_STATUS = "/by-status";
        public static final String BY_DATE = "/by-date";
        public static final String TOP_ICD10 = "/top-icd10";
    }

    public static final class Form058AdminStats {
        private Form058AdminStats() {
        }

        public static final String ROOT = Admin.ROOT + "/form-058/stats";
        public static final String BY_STATUS = "/by-status";
        public static final String BY_SENDER_ORGANIZATION = "/by-sender-organization";
        public static final String BY_RECEIVER_ORGANIZATION = "/by-receiver-organization";
        public static final String BY_DATE = "/by-date";
        public static final String TOP_ICD10 = "/top-icd10";
    }

    public static final class Form0581 {
        private Form0581() {
        }

        public static final String ROOT = API_V1 + "/form-058-1";
        public static final String BY_ID = "/{id}";
        public static final String BY_DOCUMENT_VALUE = "/by-document";
        public static final String ACCEPT = "/{id}/accept";
        public static final String APPROVE = "/{id}/approve";
        public static final String CANCEL = "/{id}/cancel";
        public static final String REOPEN = "/{id}/reopen";
        public static final String CARDS = "/{id}/cards";
        public static final String ASSIGN_CARDS = "/{id}/cards/assign";
        public static final String PDF = "/{id}/pdf";
        public static final String EXPORT = "/export";

        // Same rationale as Form058.AFFILIATED - a form058-1 visible because
        // the patient's workplace/place of study matches the current
        // organization gets its own endpoint/filter instead of a hidden
        // mode-switch query flag.
        public static final String AFFILIATED = "/affiliated";
    }

    public static final class Form0581Stats {
        private Form0581Stats() {
        }

        public static final String ROOT = Form0581.ROOT + "/stats";
        public static final String BY_STATUS = "/by-status";
        public static final String BY_DATE = "/by-date";
        public static final String TOP_ICD10 = "/top-icd10";
    }

    public static final class Form0581AdminStats {
        private Form0581AdminStats() {
        }

        public static final String ROOT = Admin.ROOT + "/form-058-1/stats";
        public static final String BY_STATUS = "/by-status";
        public static final String BY_SENDER_ORGANIZATION = "/by-sender-organization";
        public static final String BY_RECEIVER_ORGANIZATION = "/by-receiver-organization";
        public static final String BY_DATE = "/by-date";
        public static final String TOP_ICD10 = "/top-icd10";
    }

    /**
     * Root for the report module — dedicated home for cross-form,
     * organization-hierarchy reports (many of them built from the same
     * form058/form0581 data). Each report gets its own nested {@code
     * ApiPaths} class and its own {@code modules.report.formN} package so
     * adding another report never collides with an existing one. Every
     * report shares the same drill-down shape — root node (whole access
     * scope) + one hierarchy level per {@code children} call (republic→
     * region→district→organization) — via {@code
     * report.shared.ReportHierarchyService}; each report only supplies its
     * own counting SQL. Kept separate from {@link Dashboard}: dashboard
     * endpoints are single-organization summary widgets, report endpoints
     * are drill-down tables meant to be printed/exported.
     */
    public static final class Report {
        private Report() {
        }

        public static final String ROOT = API_V1 + "/reports";
    }

    /**
     * "Form 1" — «Мониторинг инфекционных и паразитарных заболеваний»
     * (confirmed/primary case counts, age/gender cut), form058 + form0581
     * combined, organization-hierarchy drill-down (republic→region→district
     * →organization), one level per call, over an arbitrary caller-supplied
     * date range — see {@code Form1ReportController} under {@code
     * modules.report.form1}.
     */
    public static final class Form1Report {
        private Form1Report() {
        }

        public static final String ROOT = Report.ROOT + "/form-1";
        public static final String ROOT_NODE = "/root";
        public static final String CHILDREN = "/children";
    }

    /**
     * "Form 4" — «Kasallanishning ijtimoiy tarkibi» (social/occupation
     * composition of illness), confirmed/primary case counts broken down by
     * {@code patient.category_code} (children, students, workers, medical
     * staff, pensioners, homeless, ...), form058 + form0581 combined,
     * organization-hierarchy drill-down (republic→region→district
     * →organization), one level per call, over an arbitrary caller-supplied
     * date range — see {@code Form4ReportController} under {@code
     * modules.report.form4}.
     */
    public static final class Form4Report {
        private Form4Report() {
        }

        public static final String ROOT = Report.ROOT + "/form-4";
        public static final String ROOT_NODE = "/root";
        public static final String CHILDREN = "/children";
    }

    /**
     * "Form 6" — infectious/parasitic disease age-structure comparison
     * (primary/not-yet-decided notifications only, form058 + form0581
     * combined), organization-hierarchy drill-down (republic→region→district
     * →organization) same as Form 1, each node showing "O'tgan yil"/"Joriy
     * yil"/"O'sish-Kamayish" (current period vs. the same calendar dates one
     * year earlier), plus a per-node age-group breakdown — see {@code
     * Form6ReportController} under {@code modules.report.form6}.
     */
    public static final class Form6Report {
        private Form6Report() {
        }

        public static final String ROOT = Report.ROOT + "/form-6";
        public static final String ROOT_NODE = "/root";
        public static final String CHILDREN = "/children";
        public static final String AGE_BREAKDOWN = "/age-breakdown";
    }

    /**
     * "Shakl №2" manual statistics entry — counts that have no source of
     * truth anywhere else in the system (disinfection, inspections, fines,
     * prosecutor referrals, ...), entered by hand per creating organization
     * and period. See {@code Form2ManualEntryController} under {@code
     * modules.report.form2.manual}.
     */
    public static final class Form2ManualEntry {
        private Form2ManualEntry() {
        }

        public static final String ROOT = Report.ROOT + "/form-2/manual-entries";
        public static final String PREFILL = "/prefill";
        public static final String BY_ID = "/{id}";
    }

    /**
     * "Shakl №3-1" manual statistics entry — weekly ILI/SARI
     * (influenza-like illness / severe acute respiratory infection)
     * sentinel surveillance counts plus flu vaccination coverage, entered
     * by hand per creating organization and period. Unlike {@link
     * Form2ManualEntry}, none of these counts have any other source of
     * truth in the system — see {@code Form31EntryController} under
     * {@code modules.report.form31}.
     */
    public static final class Form31Entry {
        private Form31Entry() {
        }

        public static final String ROOT = Report.ROOT + "/form-3-1/entries";
        public static final String BY_ID = "/{id}";
    }

    /**
     * "Shakl №3-2" manual statistics entry — sanitary inspection activity
     * counts (inspected objects, identified deficiencies, officials held
     * liable, suspended/closed objects), each broken down by object type,
     * entered by hand per creating organization and period. Same as {@link
     * Form31Entry}, none of these counts have any other source of truth in
     * the system — see {@code Form32EntryController} under {@code
     * modules.report.form32}.
     */
    public static final class Form32Entry {
        private Form32Entry() {
        }

        public static final String ROOT = Report.ROOT + "/form-3-2/entries";
        public static final String BY_ID = "/{id}";
    }

    public static final class Card {
        private Card() {
        }

        public static final String ROOT = API_V1 + "/cards";
        public static final String BY_ID = "/{id}";
        public static final String PDF = "/{id}/pdf";

        // Personal, server-scoped list view — never trust a client-supplied
        // user id for this, always resolve from the authenticated principal.
        // Always stays personal (assigned-to-me), regardless of organization
        // scope — see CardQueryService.findMine. The organization-wide view
        // lives at the root listing (GET /v1/cards, no path suffix) instead.
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
        public static final String PDF = "/{id}/pdf";

        // Attached employee marks a filled-in act as finished / sends it to LIS.
        public static final String READY = "/{id}/ready";
        public static final String SEND_TO_LIS = "/{id}/send-to-lis";

        // LIS posts its result here once it has finished processing the act —
        // the URL we hand LIS as the payload's redirectUrl (see LisUrlFactory).
        // An ordinary authenticated /v1/acts/** endpoint like any other, not a
        // separate integration surface: see the user's explicit call — LIS
        // calls back with the same SSO-token-based auth as everything else.
        public static final String LIS_CALLBACK = "/{id}/lis/callback";

        // Personal, server-scoped list view — same rule as Card.MINE.
        public static final String MINE = "/mine";
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

        /**
         * DMED-specific form058-1 endpoint, kept separate from
         * {@link #FORM0581} for the same reason as {@link #DMED_FORM058}:
         * DMED integrates against a fixed, flat request shape
         * ({@code DmedCreateForm0581Request}) that must not shift to the
         * generic endpoint's entity-mirroring nested structure. A literal
         * path, not a {@code {source}} pattern.
         */
        public static final String DMED_FORM0581 = ROOT + "/DMED/form-058-1";

        /**
         * Outbound side of the same integration surface: instead of an
         * external system pushing case data to us ({@link #FORM058}/
         * {@link #FORM0581}), this is where a registered integration client
         * pulls data back out - a patient's basic info plus their most
         * recently submitted form058/form058-1 (see
         * {@code PatientCaseLookupService}). Requires the
         * {@code patient-case:read} scope (see {@code IntegrationScope}) and
         * is scoped to the client's own bound organization, exactly like the
         * inbound side.
         */
        public static final String PATIENT_CASE = ROOT + "/{source}/patients/case";
    }

}
