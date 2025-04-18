package uz.uzinfocom.isemid.configs.logging;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static uz.uzinfocom.isemid.commons.utils.HttpUtil.getRequestHeaders;
import static uz.uzinfocom.isemid.commons.utils.HttpUtil.modifyRequestBody;


@Slf4j
@Component
@RequiredArgsConstructor
public class AdvancedLoggingFilter implements Filter {
    private final AuditService auditService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        RequestWrapper requestWrapper = new RequestWrapper(httpRequest);

        filterChain.doFilter(requestWrapper, response);

        auditService.logRequest(
                httpRequest.getUserPrincipal() != null ? httpRequest.getUserPrincipal().getName() : "anonymous",
                httpRequest.getRemoteAddr(),
                httpRequest.getMethod(),
                httpRequest.getRequestURI(),
                httpRequest.getQueryString(),
                getRequestHeaders(httpRequest),
                modifyRequestBody(requestWrapper.getBody().replaceAll("\\s+", "")),
                httpResponse.getStatus()
        );
    }
}
