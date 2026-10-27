package uz.uzinfocom.app.platform.integrationclient.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uz.uzinfocom.app.platform.security.auth.AuthorityNames;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;

import java.util.Arrays;
import java.util.Optional;

/**
 * Single source of truth for the scope literals granted to an
 * {@link IntegrationClient} and checked by every inbound-integration
 * controller's {@code @PreAuthorize}. Adding a new inbound integration form
 * type later is one new constant here.
 */
@Getter
@RequiredArgsConstructor
public enum IntegrationScope {
    FORM058_SUBMIT("form058:submit", new Endpoint("POST", ApiPaths.Integration.FORM058)),
    FORM0581_SUBMIT("form0581:submit", new Endpoint("POST", ApiPaths.Integration.FORM0581)),
    FORM058_CONFIRM("form058:confirm", new Endpoint("POST", ApiPaths.Integration.FORM058 + "/{idOrUuid}/confirm")),
    FORM0581_CONFIRM("form0581:confirm", new Endpoint("POST", ApiPaths.Integration.FORM0581 + "/{idOrUuid}/confirm")),
    FORM129_SUBMIT("form129:submit", new Endpoint("POST", ApiPaths.Integration.FORM129)),
    PATIENT_CASE_READ("patient-case:read", new Endpoint("GET", ApiPaths.Integration.PATIENT_CASE));

    /** HTTP method + path template (with a {@code {source}} segment) that a scope unlocks. */
    public record Endpoint(String method, String pathTemplate) {
        public String resolve(String sourceKey) {
            return pathTemplate.replace("{source}", sourceKey);
        }
    }

    private final String claim;
    private final Endpoint endpoint;

    public static Optional<IntegrationScope> fromClaim(String claim) {
        return Arrays.stream(values())
                .filter(scope -> scope.claim.equals(claim))
                .findFirst();
    }

    /**
     * The RBAC equivalent of this scope, e.g. {@code "form058:submit"} →
     * {@code "PERMISSION_FORM058_SUBMIT"} — lets an SSO/DHP-authenticated
     * caller satisfy the same {@link IntegrationScope} check an integration
     * client satisfies via its JWT's {@code SCOPE_*} authority, by holding
     * the matching {@code Permission}+{@code Action} through the existing
     * Role/Permission/Action admin CRUD instead.
     */
    public String permissionAuthority() {
        String[] parts = claim.split(":", 2);
        return AuthorityNames.permission(parts[0], parts[1]);
    }
}
