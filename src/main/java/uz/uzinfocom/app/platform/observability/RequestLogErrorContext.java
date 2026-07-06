package uz.uzinfocom.app.platform.observability;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RequestLogErrorContext {

    private static final String ATTRIBUTE_NAME =
            RequestLogErrorContext.class.getName() + ".ERROR_DETAILS";

    public static void attach(HttpServletRequest request, String errorCode, String technicalMessage,
                              Throwable throwable) {
        if (request == null) {
            return;
        }
        request.setAttribute(
                ATTRIBUTE_NAME,
                new ErrorDetails(
                        normalize(errorCode),
                        normalize(technicalMessage),
                        throwable
                )
        );
    }

    public static void attach(
            HttpServletRequest request,
            String errorCode,
            Throwable throwable
    ) {
        attach(
                request,
                errorCode,
                throwable == null ? null : throwable.getMessage(),
                throwable
        );
    }

    public static Optional<ErrorDetails> get(HttpServletRequest request) {
        if (request == null) {
            return Optional.empty();
        }
        Object attribute = request.getAttribute(ATTRIBUTE_NAME);
        return attribute instanceof ErrorDetails errorDetails
                ? Optional.of(errorDetails)
                : Optional.empty();
    }

    private static String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    public record ErrorDetails(
            String errorCode,
            String technicalMessage,
            Throwable throwable
    ) {
    }
}
