package uz.uzinfocom.app.platform.observability;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RequestLogErrorContext {

    private static final String ATTRIBUTE_NAME =
            RequestLogErrorContext.class.getName() + ".ERROR_DETAILS";
    private static final int MAX_ERROR_CODE_LENGTH = 100;
    private static final int MAX_TECHNICAL_MESSAGE_LENGTH = 500;

    public static void attach(
            HttpServletRequest request,
            String errorCode,
            String technicalMessage,
            Throwable throwable
    ) {
        if (request == null) {
            return;
        }

        ErrorDetails existing = get(request).orElse(null);
        String normalizedCode = normalize(errorCode, MAX_ERROR_CODE_LENGTH);
        String normalizedMessage = normalize(technicalMessage, MAX_TECHNICAL_MESSAGE_LENGTH);
        ErrorDetails merged = new ErrorDetails(
                selectText(existing == null ? null : existing.errorCode(), normalizedCode),
                selectText(existing == null ? null : existing.technicalMessage(), normalizedMessage),
                selectThrowable(existing == null ? null : existing.throwable(), throwable)
        );
        request.setAttribute(ATTRIBUTE_NAME, merged);
    }

    public static void attach(
            HttpServletRequest request,
            String errorCode,
            Throwable throwable
    ) {
        String message = throwable == null
                ? null
                : throwable.getClass().getSimpleName() + ": " + throwable.getMessage();
        attach(request, errorCode, message, throwable);
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

    private static String selectText(String existing, String incoming) {
        if (!StringUtils.hasText(existing)) {
            return incoming;
        }
        if (!StringUtils.hasText(incoming)) {
            return existing;
        }
        return isGeneric(existing) && !isGeneric(incoming) ? incoming : existing;
    }

    private static Throwable selectThrowable(Throwable existing, Throwable incoming) {
        if (existing == null) {
            return incoming;
        }
        if (incoming == null || existing == incoming) {
            return existing;
        }
        if (containsCause(existing, incoming)) {
            return incoming;
        }
        if (containsCause(incoming, existing)) {
            return existing;
        }
        if (existing instanceof ServletException && !(incoming instanceof ServletException)) {
            return incoming;
        }
        return existing;
    }

    private static boolean containsCause(Throwable container, Throwable candidate) {
        Throwable current = container;
        while (current != null && current != current.getCause()) {
            if (current == candidate) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private static boolean isGeneric(String value) {
        return "INTERNAL_ERROR".equals(value)
                || "BAD_REQUEST".equals(value)
                || "No message".equalsIgnoreCase(value)
                || value.startsWith("Exception:")
                || value.startsWith("RuntimeException:")
                || value.startsWith("ServletException:")
                || value.startsWith("Unhandled filter-chain exception");
    }

    private static String normalize(String value, int maxLength) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        StringBuilder normalized = new StringBuilder(Math.min(value.length(), maxLength));
        for (int index = 0; index < value.length() && normalized.length() < maxLength; index++) {
            char character = value.charAt(index);
            normalized.append(Character.isISOControl(character) || character == '\r'
                    || character == '\n' || character == '\t' ? ' ' : character);
        }
        return normalized.toString().trim();
    }

    public record ErrorDetails(
            String errorCode,
            String technicalMessage,
            Throwable throwable
    ) {
    }
}
