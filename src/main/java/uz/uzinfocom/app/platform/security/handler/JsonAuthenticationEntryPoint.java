package uz.uzinfocom.app.platform.security.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.platform.observability.RequestLogErrorContext;
import uz.uzinfocom.app.platform.web.response.ErrorResponseWriter;
import uz.uzinfocom.app.shared.exception.ErrorCode;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JsonAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ErrorResponseWriter errorResponseWriter;

    @Override
    public void commence(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {

        RequestLogErrorContext.attach(
                request,
                ErrorCode.UNAUTHORIZED.getCode(),
                "Authentication failed",
                authException
        );

        errorResponseWriter.write(request, response, HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED);
    }
}
