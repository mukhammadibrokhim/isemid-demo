package uz.uzinfocom.isemid.features.security.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

public final class BearerTokenExtractor {

    private BearerTokenExtractor() {}

    public static String extract(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (!StringUtils.hasText(header)) return null;

        if (header.startsWith("Bearer ")) {
            String token = header.substring(7).trim();
            return token.isEmpty() ? null : token;
        }
        return null;
    }

    public static String stripBearer(String authorizationHeader) {
        if (!StringUtils.hasText(authorizationHeader)) return null;
        if (authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7).trim();
            return token.isEmpty() ? null : token;
        }
        return authorizationHeader.trim();
    }
}
