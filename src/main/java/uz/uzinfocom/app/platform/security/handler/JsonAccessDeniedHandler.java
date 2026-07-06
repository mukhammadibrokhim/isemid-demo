package uz.uzinfocom.app.platform.security.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.platform.observability.RequestLogErrorContext;
import uz.uzinfocom.app.platform.web.response.ErrorResponseWriter;
import uz.uzinfocom.app.shared.exception.ErrorCode;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JsonAccessDeniedHandler implements AccessDeniedHandler {

    private final ErrorResponseWriter errorResponseWriter;

    @Override
    public void handle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException {

        RequestLogErrorContext.attach(
                request,
                ErrorCode.FORBIDDEN.getCode(),
                "Access denied",
                accessDeniedException
        );

        if (isMessageCode(accessDeniedException.getMessage())) {
            errorResponseWriter.write(
                    request,
                    response,
                    HttpStatus.FORBIDDEN,
                    ErrorCode.FORBIDDEN,
                    accessDeniedException.getMessage()
            );
            return;
        }

        errorResponseWriter.write(request, response, HttpStatus.FORBIDDEN, ErrorCode.FORBIDDEN);
    }

    private boolean isMessageCode(String message) {
        return message != null && message.matches("[a-z][a-z0-9_]*(\\.[a-z0-9_]+)+");
    }
}
