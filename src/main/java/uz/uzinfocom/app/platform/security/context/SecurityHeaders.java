package uz.uzinfocom.app.platform.security.context;

public final class SecurityHeaders {

    public static final String ORGANIZATION_ID = "X-Organization-Id";
    public static final String LEGACY_ORGANIZATION_ID = "Organization-id";

    /** Carries an integration client's {@code authType=API_KEY} credential. */
    public static final String INTEGRATION_API_KEY = "X-Api-Key";

    private SecurityHeaders() {
    }
}
