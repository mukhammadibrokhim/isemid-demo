package uz.uzinfocom.isemid.commons.utils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;
import org.springframework.util.StringUtils;
import uz.uzinfocom.isemid.commons.ApiPaths;
import uz.uzinfocom.isemid.commons.exceptions.ApiException;

import java.util.Enumeration;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@UtilityClass
public class HttpUtil {

    public static String getRequestUrl(HttpServletRequest request) {
        String fullURL = request.getRequestURI();
        String queryString = request.getQueryString();

        if (queryString != null) {
            fullURL += "?" + queryString;
        }

        return ApiPaths.BaseDomain.ROOT + fullURL;
    }

    public static String extractToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        throw new ApiException(UNAUTHORIZED);
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

    public static String modifyRequestBody(String request) {
        if (StringUtils.hasText(request) && request.contains("\"photo\"")) {
            return request.replaceAll("\"photo\":\"[^\"]*\"", "\"photo\":\"photo\"");
        }
        return request;
    }
}
