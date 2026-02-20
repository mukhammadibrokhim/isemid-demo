package uz.uzinfocom.isemid.features.db.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class DbUtil {

    public static boolean validateCredentials(String username, String password) {
        return "admin".equals(username) && "password123".equals(password);
    }
}
