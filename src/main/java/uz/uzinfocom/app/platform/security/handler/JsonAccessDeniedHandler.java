package uz.uzinfocom.app.platform.security.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.platform.exception.ErrorCode;
import uz.uzinfocom.app.platform.observability.TraceIdProvider;
import uz.uzinfocom.app.platform.web.response.ErrorResponseWriter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JsonAccessDeniedHandler implements AccessDeniedHandler {

    private final TraceIdProvider traceIdProvider;
    private final ErrorResponseWriter errorResponseWriter;

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException {

        String traceId = traceIdProvider.getTraceId(request);

        log.warn(
                "Access denied. traceId={}, method={}, path={}, remoteAddr={}, reason={}",
                traceId,
                request.getMethod(),
                request.getRequestURI(),
                request.getRemoteAddr(),
                accessDeniedException.getMessage()
        );

        errorResponseWriter.write(request, response, HttpStatus.FORBIDDEN, ErrorCode.FORBIDDEN);
    }
}
