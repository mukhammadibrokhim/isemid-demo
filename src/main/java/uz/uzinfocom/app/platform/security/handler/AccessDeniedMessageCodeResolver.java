package uz.uzinfocom.app.platform.security.handler;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

/**
 * Validates that an {@link org.springframework.security.access.AccessDeniedException}
 * message is a genuine localization key - not an arbitrary, potentially sensitive
 * free-text message - before it is safe to forward to the client.
 * <p>
 * Shared by {@link JsonAccessDeniedHandler} (exceptions caught by the Spring
 * Security filter chain, e.g. thrown from a {@code Filter} or {@code @PreAuthorize})
 * and {@code GlobalExceptionHandler} (the same exception type, but thrown from
 * application code inside a controller method body, which is instead caught by
 * MVC's {@code ExceptionHandlerExceptionResolver}). Both call sites must apply the
 * exact same rule - otherwise a validated message code added on one path silently
 * degrades to a generic "forbidden" message on the other, depending on nothing
 * more meaningful than where in the request lifecycle the exception happened to
 * be thrown from.
 */
@Component
public class AccessDeniedMessageCodeResolver {

    /**
     * Accepts localization keys such as:
     * organization.required
     * organization.not_allowed
     * integration.scope.missing
     */
    private static final Pattern MESSAGE_CODE_PATTERN = Pattern.compile(
            "^[a-z][a-z0-9_]*(?:\\.[a-z0-9_]+)+$"
    );

    /**
     * Prevents unexpectedly large exception messages from being treated
     * as localization keys.
     */
    private static final int MAX_MESSAGE_CODE_LENGTH = 200;

    public String resolve(String message) {
        if (!StringUtils.hasText(message)) {
            return null;
        }

        String normalized = message.trim();

        if (normalized.length() > MAX_MESSAGE_CODE_LENGTH) {
            return null;
        }

        return MESSAGE_CODE_PATTERN.matcher(normalized).matches() ? normalized : null;
    }
}
