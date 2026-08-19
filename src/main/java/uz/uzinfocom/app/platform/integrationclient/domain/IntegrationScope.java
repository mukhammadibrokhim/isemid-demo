package uz.uzinfocom.app.platform.integrationclient.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uz.uzinfocom.app.platform.security.auth.AuthorityNames;

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
    FORM058_SUBMIT("form058:submit"),
    FORM0581_SUBMIT("form0581:submit"),
    PATIENT_CASE_READ("patient-case:read");

    private final String claim;

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
