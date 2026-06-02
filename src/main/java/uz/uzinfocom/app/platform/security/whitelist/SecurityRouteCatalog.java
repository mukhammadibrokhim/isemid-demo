package uz.uzinfocom.app.platform.security.whitelist;

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
     * </p>
     */
    public static final List<String> OPEN_PATTERNS = List.of(
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/api-docs/**",
            "/v1/auth/**",
            "/v1/log/**",
            "/v1/actuator/**"
    );

    /**
     * Authenticated routes that do not require current organization selection.
     */
    public static final List<String> NO_ORG_HEADER_PATTERNS = List.of(
            "/v1/user/me",
            "/v1/user/me/**",
            "/v1/users/me",
            "/v1/users/me/**"
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

        POLICY_RULES = Collections.unmodifiableMap(rules);
    }

    public record RoutePolicyRule(
            boolean organizationHeaderRequired,
            boolean roleValidationRequired
    ) {
    }
}
