package uz.uzinfocom.isemid.commons.utils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;
import org.springframework.data.domain.Page;
import org.springframework.web.util.UriComponentsBuilder;
import uz.uzinfocom.isemid.commons.dtos.ApiResponsePage;

import java.util.ArrayList;
import java.util.stream.IntStream;

@UtilityClass
public class CommonUtil {

    public static <T> ApiResponsePage createApiResponsePage(int page, Page<T> objectPage, String requestUrl) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(requestUrl);

        String selfUrl = uriBuilder.replaceQueryParam("page", page).toUriString();
        String nextUrl = uriBuilder.replaceQueryParam("page", page + 1).toUriString();

        ApiResponsePage apiResponsePage = new ApiResponsePage(
                true, MessageUtil.OPERATION_SUCCESS,
                objectPage.getTotalElements(),
                new ArrayList<>(),
                new ArrayList<>(objectPage.getContent())
        );

        apiResponsePage.addLink("self", selfUrl);
        apiResponsePage.addLink("next", nextUrl);

        if (page > 1) {
            String prevUrl = uriBuilder.replaceQueryParam("page", page - 1).toUriString();
            apiResponsePage.addLink("prev", prevUrl);
        }

        return apiResponsePage;
    }

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

    public static final String[] WHITELIST_HEADER_CHECK = {
            "receive-from-lis",
            "/catalog",
            "/user/me",
            "/department",
            "/v1/auth",
            "/qr",
            "/legal-entity"
    };
}
