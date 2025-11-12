package uz.uzinfocom.isemid.commons.utils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.util.Map;
import java.util.stream.IntStream;

@UtilityClass
public class CommonUtil {


    /**
     * NNUZB tekshiruvchi static metod
     *
     * @param nnuzb - 14 ta raqamli NNUZB qiymati
     * @return boolean - NNUZB to'g'ri bo'lsa true, aks holda false
     */
    public static boolean checkNnuzb(String nnuzb) {
        if (nnuzb.length() != 14 || !nnuzb.chars().allMatch(Character::isDigit)) {
            return false; // NNUZB faqat 14 ta raqamdan iborat bo'lishi kerak
        }

        int genderCenturyIndex = Integer.parseInt(nnuzb.substring(0, 1));
        if (genderCenturyIndex < 1 || genderCenturyIndex > 6) {
            return false; // Gender va asr kodi 1 dan 6 gacha bo'lishi kerak
        }

        int day = Integer.parseInt(nnuzb.substring(1, 3));
        if (day < 1 || day > 31) {
            return false; // Kunda 1-31 oralig'ida bo'lishi kerak
        }

        int month = Integer.parseInt(nnuzb.substring(3, 5));
        if (month < 1 || month > 12) {
            return false; // Oy 1-12 oralig'ida bo'lishi kerak
        }

        // Modulo 10 bilan nazorat raqamini hisoblash
        String[] verifiable = nnuzb.substring(0, 13).split("");
        int checkDigit = Integer.parseInt(nnuzb.substring(13));
        int[] weightFunction = {7, 3, 1, 7, 3, 1, 7, 3, 1, 7, 3, 1, 7};

        int calculatedCheck = IntStream.range(0, verifiable.length)
                .map(i -> Integer.parseInt(verifiable[i]) * weightFunction[i])
                .sum() % 10;

        return calculatedCheck == checkDigit;
    }

    public static boolean checkReqUri(HttpServletRequest request, String... patterns) {
        if (request == null || patterns == null || patterns.length == 0) {
            return false;
        }

        String reqUri = request.getRequestURI();

        for (String pattern : patterns) {
            if (reqUri.contains(pattern)) {
                return true;
            }
        }

        return false;
    }

    public static String maskTin(String tin) {
        int visibleCount = 3;
        if (tin == null || tin.length() <= visibleCount) return "***";
        return "*".repeat(tin.length() - visibleCount) + tin.substring(tin.length() - visibleCount);
    }

    public static String maskNnuzb(String nnuzb) {
        if (nnuzb == null || nnuzb.length() <= 3) return "***";
        return "*".repeat(nnuzb.length() - 3) + nnuzb.substring(nnuzb.length() - 3);
    }


    public static final String[] WHITELIST_HEADER_CHECK = {
            "receive-from-lis",
            "/catalog",
            "/user/me",
            "/db",
            "/department",
            "/v1/auth",
            "/qr",
            "/legal-entity",
            "/info",
            "/monitoring"
    };

    private static final Map<Integer, String> uzMonthsCyrl = Map.ofEntries(
            Map.entry(1, "январь"),
            Map.entry(2, "февраль"),
            Map.entry(3, "март"),
            Map.entry(4, "апрель"),
            Map.entry(5, "май"),
            Map.entry(6, "июнь"),
            Map.entry(7, "июль"),
            Map.entry(8, "август"),
            Map.entry(9, "сентябрь"),
            Map.entry(10, "октябрь"),
            Map.entry(11, "ноябрь"),
            Map.entry(12, "декабрь")
    );

    private String getUzMonthCyril(LocalDate date) {
        return (date != null) ? uzMonthsCyrl.get(date.getMonthValue()) : "________";
    }

}
