package uz.uzinfocom.app.platform.cache;

public final class SecurityCacheNames {

    public static final String JWKS_BY_URI = "security-jwks-by-uri";
    public static final String PUBLIC_KEY_DECODER_BY_PROVIDER = "security-public-key-decoder-by-provider";
    public static final String ORGANIZATION_SYNC_BY_PROVIDER_AND_UUID = "iam-organization-sync-by-provider-and-uuid";
    public static final String ROLE_BY_NAME = "iam-role-by-name";
    public static final String SECURITY_USER_BY_ID = "iam-security-user-by-id";
    public static final String SELECTED_ORGANIZATION_BY_USER_ID_AND_UUID = "iam-selected-organization-by-user-id-and-uuid";
    public static final String USER_AUTHORITIES_BY_USER_ID = "iam-user-authorities-by-user-id";
    public static final String ROLE_PERMISSIONS_BY_ROLE_IDS = "iam-role-permissions-by-role-ids";

    public static final String IAM_ROLE_BY_NAME = ROLE_BY_NAME;

    private SecurityCacheNames() {
    }
}
