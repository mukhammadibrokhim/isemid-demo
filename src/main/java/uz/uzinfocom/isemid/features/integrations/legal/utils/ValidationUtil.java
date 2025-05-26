package uz.uzinfocom.isemid.features.integrations.legal.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ValidationUtil {

    public static boolean validationTin(String tin) {
        return tin != null && !tin.trim().isEmpty() && tin.matches("\\d+");
    }
}
