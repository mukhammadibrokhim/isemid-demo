package uz.uzinfocom.isemid.commons.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

public final class RequestUrlUtils {

    private static volatile String publicBaseUrl;

    private RequestUrlUtils() {
    }

    public static void setPublicBaseUrl(String baseUrl) {
        if (!StringUtils.hasText(baseUrl)) {
            throw new IllegalArgumentException("Public base URL must not be blank");
        }

        publicBaseUrl = removeTrailingSlash(baseUrl);
    }

    public static String getRequestUrl(HttpServletRequest request) {
        if (!StringUtils.hasText(publicBaseUrl)) {
            throw new IllegalStateException("Public base URL is not initialized");
        }

        return UriComponentsBuilder
                .fromUriString(publicBaseUrl)
                .path(request.getRequestURI())
                .query(request.getQueryString())
                .build(true)
                .toUriString();
    }

    private static String removeTrailingSlash(String value) {
        String result = value.trim();

        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }

        return result;
    }
}