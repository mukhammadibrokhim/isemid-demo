package uz.uzinfocom.isemid.logging;


import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static uz.uzinfocom.isemid.utils.CommonUtils.getRequestHeaders;
import static uz.uzinfocom.isemid.utils.CommonUtils.modifyRequestBody;


@Component
public class AuditFilter implements Filter {

    @Autowired
    AuditService auditService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        if (httpRequest.getContentType() != null && httpRequest.getContentType().startsWith("multipart/")) {
            auditService.logRequest(
                    httpRequest.getUserPrincipal() != null ? httpRequest.getUserPrincipal().getName() : "anonymous",
                    httpRequest.getRemoteAddr(),
                    httpRequest.getMethod(),
                    httpRequest.getRequestURI(),
                    httpRequest.getQueryString(),
                    getRequestHeaders(httpRequest),
                    "file",
                    httpResponse.getStatus()
            );
            filterChain.doFilter(request, response);
            return;
        }
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
