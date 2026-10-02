package uz.uzinfocom.app.platform.observability;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Optional;

/**
 * Bridges the authenticated principal's name from deep inside the Spring
 * Security filter chain out to {@link RequestLoggingFilter}, which wraps
 * *outside* that chain (see its {@code @Order}). By the time control returns
 * to that outer filter, the request wrapper Spring Security installs for
 * {@code HttpServletRequest#getUserPrincipal()} is gone (it only existed on
 * the wrapped instance passed further down the chain, not the outer
 * reference) and {@code SecurityContextHolder} has already been cleared by
 * {@code SecurityContextHolderFilter}'s own {@code finally} block - so
 * neither can be read reliably from the outside. Request attributes are the
 * one thing that survives that boundary, since every wrapper delegates
 * get/setAttribute back to the original request. Same pattern as
 * {@link RequestLogErrorContext}, just for the principal instead of the
 * error details.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RequestPrincipalContext {

    private static final String ATTRIBUTE_NAME =
            RequestPrincipalContext.class.getName() + ".PRINCIPAL";

    public static void attach(HttpServletRequest request, String principalName) {
        if (request == null || principalName == null || principalName.isBlank()) {
            return;
        }
        request.setAttribute(ATTRIBUTE_NAME, principalName);
    }

    public static Optional<String> get(HttpServletRequest request) {
        if (request == null) {
            return Optional.empty();
        }
        Object attribute = request.getAttribute(ATTRIBUTE_NAME);
        return attribute instanceof String principalName ? Optional.of(principalName) : Optional.empty();
    }
}
