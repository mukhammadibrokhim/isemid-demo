package uz.uzinfocom.app.platform.security.whitelist;

import uz.uzinfocom.app.shared.constants.api.ApiPaths;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class SecurityRouteCatalog {

    private SecurityRouteCatalog() {
    }

    /**
     * Publicly accessible routes.
     *
     * <p>
     * Production-safe default: DB/debug endpoints are intentionally excluded.
     * Only read-only, non-sensitive actuator endpoints are listed explicitly —
     * a blanket "/v1/actuator/**" would also expose mutable/sensitive endpoints
     * (e.g. "loggers", which lets a caller change log levels at runtime) to
     * unauthenticated callers the moment they're added to management.endpoints
     * .web.exposure.include. Anything not listed here falls through to
     * anyRequest().authenticated() in SecurityConfig.
     * </p>
     */
    public static final List<String> OPEN_PATTERNS = List.of(
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/api-docs/**",
            "/v1/auth/**",
            "/v1/log/**",
            "/v1/actuator/health/**",
            "/v1/actuator/info",
            "/v1/actuator/prometheus",
            "/v1/actuator/metrics/**",
            // The one public endpoint on the inbound-integration surface: a
            // registered client exchanges its client_id/client_secret for a
            // short-lived JWT here, before it has any bearer token to
            // present. Every other /integration/v1/** path requires that
            // token and is NOT listed here.
            "/integration/v1/oauth/token"
    );

    /**
     * Authenticated routes that do not require current organization selection.
     */
    public static final List<String> NO_ORG_HEADER_PATTERNS = List.of(
            "/v1/user/me",
            "/v1/user/me/**",
            "/v1/users/me",
            "/v1/users/me/**",
            "/v1/references/**",
            ApiPaths.Citizen.ROOT + "/**",
            ApiPaths.LegalEntity.ROOT + "/**"
    );

    /**
     * Fine-grained route policy overrides.
     * Default for protected routes is: org header required + role validation required.
     */
    public static final Map<String, RoutePolicyRule> POLICY_RULES;

    static {
        LinkedHashMap<String, RoutePolicyRule> rules = new LinkedHashMap<>();

        rules.put("/v1/auth/**", new RoutePolicyRule(false, false));
        rules.put("/v1/user/me", new RoutePolicyRule(false, false));
        rules.put("/v1/user/me/**", new RoutePolicyRule(false, false));
        rules.put("/v1/users/me", new RoutePolicyRule(false, false));
        rules.put("/v1/users/me/**", new RoutePolicyRule(false, false));
        rules.put("/v1/references/**", new RoutePolicyRule(false, true));
        rules.put(ApiPaths.Citizen.ROOT + "/**", new RoutePolicyRule(false, true));
        rules.put(ApiPaths.LegalEntity.ROOT + "/**", new RoutePolicyRule(false, true));
        // Admin actions (settings, panel-admin grant/revoke, cross-organization
        // stats) are not tied to any one selected organization — requiring an
        // org header here would reject otherwise-valid admin requests with
        // AccessDeniedException("organization.required") before they even
        // reach the controller. Role validation still applies as normal.
        rules.put("/v1/admin/**", new RoutePolicyRule(false, true));

        POLICY_RULES = Collections.unmodifiableMap(rules);
    }

    public record RoutePolicyRule(
            boolean organizationHeaderRequired,
            boolean roleValidationRequired
    ) {
    }
}
