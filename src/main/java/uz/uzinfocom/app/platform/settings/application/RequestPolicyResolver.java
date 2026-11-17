package uz.uzinfocom.app.platform.settings.application;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.platform.settings.application.RouteAccessPolicyResolver;

@Component
@RequiredArgsConstructor
public class RequestPolicyResolver {

    private final RouteAccessPolicyResolver routeAccessPolicyResolver;

    public RequestPolicy resolve(HttpServletRequest request) {
        return routeAccessPolicyResolver.resolve(normalize(request));
    }

    private String normalize(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (StringUtils.hasText(contextPath) && requestUri.startsWith(contextPath)) {
            return requestUri.substring(contextPath.length());
        }
        return StringUtils.hasText(requestUri) ? requestUri : "/";
    }
}
