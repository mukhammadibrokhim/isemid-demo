package uz.uzinfocom.app.platform.cache;

public final class SecurityCacheNames {

    public static final String JWKS_BY_URI = "security-jwks-by-uri";
    public static final String PUBLIC_KEY_DECODER_BY_PROVIDER = "security-public-key-decoder-by-provider";

    /**
     * Access/refresh tokens revoked via {@code /v1/auth/logout/{provider}}
     * (see TokenBlacklistService) - both SSO and DHP tokens are validated
     * locally by signature only (see ProviderAuthenticationManagerRegistry),
     * so this cache is what actually makes logout take effect immediately;
     * neither provider's own logout/revoke call has any effect on whether
     * this backend still accepts an already-issued token. Entries expire on
     * a fixed TTL (see ApplicationCacheConfig), not each token's own {@code
     * exp} - simpler, and safe as long as the TTL is at least as long as the
     * longest-lived token either provider issues.
     */
    public static final String REVOKED_TOKEN_BLACKLIST = "security-revoked-token-blacklist";
    public static final String ORGANIZATION_SYNC_BY_PROVIDER_AND_UUID = "iam-organization-sync-by-provider-and-uuid";
    public static final String ROLE_BY_NAME = "iam-role-by-name";
    public static final String SECURITY_USER_BY_ID = "iam-security-user-by-id";
    public static final String SELECTED_ORGANIZATION_BY_USER_ID_AND_UUID = "iam-selected-organization-by-user-id-and-uuid";
    public static final String USER_AUTHORITIES_BY_USER_ID = "iam-user-authorities-by-user-id";
    public static final String ROLE_PERMISSIONS_BY_ROLE_IDS = "iam-role-permissions-by-role-ids";

    public static final String IAM_ROLE_BY_NAME = ROLE_BY_NAME;

    public static final String SCOPE_ORGANIZATION_IDS = "scopeOrganizationIds";
    public static final String FILTER_ORGANIZATION_IDS_BY_REGION_DISTRICT = "filterOrganizationIdsByRegionDistrict";

    /**
     * {@code /v1/dev/**} authenticates via HTTP Basic on a stateless filter
     * chain (see DevPanelSecurityConfig), so every single request would
     * otherwise re-run a full BCrypt comparison plus a DevUser DB lookup
     * before the controller even runs. This cache stores the successful
     * {@code Authentication} result keyed by username+password so repeat
     * calls with the same credentials skip both. Short TTL bounds how long a
     * revoked/disabled dev account can still authenticate from cache.
     */
    public static final String DEV_PANEL_AUTHENTICATION = "devPanelAuthentication";

    private SecurityCacheNames() {
    }
}
