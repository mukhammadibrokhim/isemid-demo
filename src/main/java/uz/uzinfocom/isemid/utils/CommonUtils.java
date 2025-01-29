package uz.uzinfocom.isemid.utils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;
import uz.uzinfocom.isemid.exceptioins.ApiException;
import uz.uzinfocom.isemid.models.payload.responses.RespPractitioner;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponsePage;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static uz.uzinfocom.isemid.utils.ApiUrlConstant.BASE_DOMAIN;
import static uz.uzinfocom.isemid.utils.AppConstant.*;

@UtilityClass
public class CommonUtils {

    public static String[] WHITE_LIST = {
            "/swagger-ui/**",
            "/api-docs/**",
            "/actuator/**"
    };

    public static String getIdentifierValueByCode(List<RespPractitioner.Identifier> identifiers, String targetCode) {
        return identifiers.stream()
                .filter(identifier -> identifier.getType() != null && identifier.getType().getCoding() != null)
                .flatMap(identifier -> identifier.getType().getCoding().stream()
                        .filter(coding -> targetCode.equals(coding.getCode()))
                        .map(coding -> identifier.getValue()))
                .findFirst().orElse(null);
    }

    /**
     * Tilga asoslangan matnni tanlash.
     *
     * @param nameRu      Rus tilidagi matn
     * @param nameUz      Lotin yozuvidagi o‘zbekcha matn
     * @param nameUzCyril Kirill yozuvidagi o‘zbekcha matn
     * @return Tanlangan til uchun mos matn
     */
    public static String resolveLang(String nameRu, String nameUz, String nameUzCyril, String kaa) {
        Locale locale = LocaleContextHolder.getLocale();
        String language = locale.getLanguage();
        return switch (language) {
            case UZ -> nameUz;
            case KAA -> kaa;
            case UZ_CYRIL -> nameUzCyril;
            default -> nameRu;
        };
    }

    public static void validateByNnuzb(String nnuzb) {
        if (!checkNnuzb(nnuzb)) {
            throw new ApiException(BAD_REQUEST, "Invalid NNUZB or photo query param invalid");
        }
    }

    public static void validateChildCertificate(String serial, String number) {
        if (!isValidCertificateSeries(serial)) {
            throw new ApiException(BAD_REQUEST, "Invalid certificate series.");
        }
        if (!isValidNumber(number)) {
            throw new ApiException(BAD_REQUEST, "Invalid certificate number.");
        }
    }

    public static boolean isValidCertificateSeries(String series) {
        String validSeriesPattern = "[A-Z-]+";
        return series != null && series.matches(validSeriesPattern);
    }

    public static boolean isValidNumber(String number) {
        return number != null && number.matches("\\d+");
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

    /*
     *  Getting header
     * */
    public static String getRequestHeaders(HttpServletRequest request) {
        StringBuilder headers = new StringBuilder();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();

            if ("authorization".equalsIgnoreCase(headerName)) {
                headers.append(headerName).append(": ").append("Bearer [REDACTED]").append(", ");
            } else {
                headers.append(headerName).append(": ").append(request.getHeader(headerName)).append(", ");
            }
        }
        return headers.toString();
    }

    public static boolean validateCredentials(String username, String password) {
        return "admin".equals(username) && "password123".equals(password);
    }

    public static String modifyRequestBody(String request) {
        if (StringUtils.hasText(request) && request.contains("\"photo\"")) {
            return request.replaceAll("\"photo\":\"[^\"]*\"", "\"photo\":\"photo\"");
        }
        return request;
    }

    private static void validatePageAndSize(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("Page number cannot be less than 0.");
        }
        if (size <= 0 || size > 100) {
            throw new IllegalArgumentException("Size must be greater than 0 and less than or equal to 100.");
        }
    }

    public static Pageable pageableSortByIdDesc(int page, int size) {
        validatePageAndSize(page, size);
        return PageRequest.of(page - 1, size, Sort.by("id").descending());
    }

    public static ApiResponsePage createApiResponsePage(int page, Page<?> objectPage, String requestUrl) {

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(requestUrl);

        String selfUrl = uriBuilder.replaceQueryParam("page", page).toUriString();
        String nextUrl = uriBuilder.replaceQueryParam("page", page + 1).toUriString();

        ApiResponsePage apiResponsePage = new ApiResponsePage(true, "Objects retrieved successfully", objectPage.getTotalElements(), new ArrayList<>(), new ArrayList<>(objectPage.getContent()));

        apiResponsePage.addLink("self", selfUrl);
        apiResponsePage.addLink("next", nextUrl);

        if (page > 1) {
            String prevUrl = uriBuilder.replaceQueryParam("page", page - 1).toUriString();
            apiResponsePage.addLink("prev", prevUrl);
        }

        return apiResponsePage;
    }
    public static String getFullURL(HttpServletRequest request) {
        String fullURL = request.getRequestURI();
        String queryString = request.getQueryString();

        if (queryString != null) {
            fullURL += "?" + queryString;
        }

        return BASE_DOMAIN + fullURL;
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
            "/department"
    };

}
