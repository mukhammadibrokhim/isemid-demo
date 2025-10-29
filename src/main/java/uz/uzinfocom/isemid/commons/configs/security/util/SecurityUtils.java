package uz.uzinfocom.isemid.commons.configs.security.util;

import uz.uzinfocom.isemid.features.user.models.User;

public class SecurityUtils {

    public static boolean isSuperAdmin(User user) {
        if (user == null || user.getRoles() == null) return false;
        return user.getRoles().stream()
                .anyMatch(role -> "isemid_super_admin".equals(role.getName()));
    }
    public static boolean isHead(User user) {
        if (user == null || user.getRoles() == null) return false;
        return user.getRoles().stream()
                .anyMatch(role -> "isemid_epidim_head".equals(role.getName()));
    }
}
