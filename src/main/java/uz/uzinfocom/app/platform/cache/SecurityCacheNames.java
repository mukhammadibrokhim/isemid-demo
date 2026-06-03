package uz.uzinfocom.app.platform.cache;

public final class SecurityCacheNames {

    public static final String JWKS_BY_URI = "security-jwks-by-uri";
    public static final String PUBLIC_KEY_DECODER_BY_PROVIDER = "security-public-key-decoder-by-provider";
    public static final String ORGANIZATION_BY_UUID = "iam-organization-by-uuid";
    public static final String ROLE_BY_NAME = "iam-role-by-name";
    public static final String USER_AUTHORITIES_BY_USER_ID = "iam-user-authorities-by-user-id";
    public static final String ROLE_PERMISSIONS_BY_ROLE_IDS = "iam-role-permissions-by-role-ids";
    public static final String USER_ORGANIZATION_IDS_BY_USER_ID = "iam-user-organization-ids-by-user-id";

    public static final String IAM_ORGANIZATION_BY_UUID = ORGANIZATION_BY_UUID;
    public static final String IAM_ROLE_BY_NAME = ROLE_BY_NAME;

    private SecurityCacheNames() {
    }
}
