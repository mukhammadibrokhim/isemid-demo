package uz.uzinfocom.isemid.commons.utils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;
import org.springframework.util.StringUtils;

import java.util.Enumeration;

@UtilityClass
public class HttpUtil {
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
