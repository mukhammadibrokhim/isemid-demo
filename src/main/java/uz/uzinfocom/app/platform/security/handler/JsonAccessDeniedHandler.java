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

    private static final String TECHNICAL_MESSAGE =
            "The authenticated principal does not have permission to access the requested resource";

    private final ErrorResponseWriter errorResponseWriter;
    private final AccessDeniedMessageCodeResolver messageCodeResolver;

    @Override
    public void handle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull AccessDeniedException accessDeniedException
    ) throws IOException {

        String messageCode = messageCodeResolver.resolve(
                accessDeniedException.getMessage()
        );

        String logMessage = messageCode == null
                ? TECHNICAL_MESSAGE
                : TECHNICAL_MESSAGE + "; messageCode=" + messageCode;

        /*
         * AccessDeniedException is handled inside the Spring Security filter chain
         * and may not reach GlobalExceptionHandler.
         *
         * Attach it to the request so RequestLoggingFilter can write one final
         * HTTP request log without duplicating the stack trace.
         */
        RequestLogErrorContext.attach(
                request,
                ErrorCode.FORBIDDEN.getCode(),
                logMessage,
                accessDeniedException
        );

        /*
         * A committed response cannot be safely rewritten.
         * Error context is still preserved for request logging.
         */
        if (response.isCommitted()) {
            return;
        }

        /*
         * Only a strictly validated localization key may be forwarded
         * to ErrorResponseWriter.
         *
         * Arbitrary exception messages must never be exposed to the client.
         */
        if (messageCode != null) {
            errorResponseWriter.write(
                    request,
                    response,
                    HttpStatus.FORBIDDEN,
                    ErrorCode.FORBIDDEN,
                    messageCode
            );
            return;
        }

        errorResponseWriter.write(
                request,
                response,
                HttpStatus.FORBIDDEN,
                ErrorCode.FORBIDDEN
        );
    }
}