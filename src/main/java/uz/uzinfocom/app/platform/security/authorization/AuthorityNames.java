package uz.uzinfocom.app.platform.security.authorization;

public final class AuthorityNames {

    private static final String ROLE_PREFIX = "ROLE_";
    private static final String PERMISSION_PREFIX = "PERMISSION_";

    private AuthorityNames() {
    }

    public static String role(String roleName) {
        return ROLE_PREFIX + normalize(roleName);
    }

    public static String permission(String subject, String action) {
        return PERMISSION_PREFIX + normalize(subject) + "_" + normalize(action);
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }

        return value.trim()
                .replace("-", "_")
                .replace(" ", "_")
                .toUpperCase();
    }
}