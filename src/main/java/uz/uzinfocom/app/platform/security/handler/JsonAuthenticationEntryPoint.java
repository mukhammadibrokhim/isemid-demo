package uz.uzinfocom.app.platform.security.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.platform.observability.TraceIdProvider;
import uz.uzinfocom.app.platform.web.response.ErrorResponseWriter;
import uz.uzinfocom.app.shared.exception.ErrorCode;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JsonAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final TraceIdProvider traceIdProvider;
    private final ErrorResponseWriter errorResponseWriter;

    @Override
    public void commence(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {

        String traceId = traceIdProvider.getTraceId(request);

        log.warn(
                "Authentication failed. traceId={}, method={}, path={}, remoteAddr={}, reason={}",
                traceId,
                request.getMethod(),
                request.getRequestURI(),
                request.getRemoteAddr(),
                authException.getMessage()
        );

        errorResponseWriter.write(request, response, HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED);
    }
}
