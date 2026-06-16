package uz.uzinfocom.app.platform.security.authorization;

import uz.uzinfocom.app.platform.iam.domain.User;

public final class SecurityUtils {

    private static final String SUPER_ADMIN = "isemid_super_admin";
    private static final String EPID_HEAD = "isemid_epidim_head";

    private SecurityUtils() {
    }

    public static boolean isSuperAdmin(User user) {
        return user != null && user.hasRole(SUPER_ADMIN);
    }

    public static boolean isHead(User user) {
        return user != null && user.hasRole(EPID_HEAD);
    }
}
