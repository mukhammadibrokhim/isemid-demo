package uz.uzinfocom.app.integration.inbound.common.web;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import uz.uzinfocom.app.platform.integrationclient.domain.IntegrationScope;
import uz.uzinfocom.app.platform.security.auth.IntegrationClientAuthenticationToken;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;

/**
 * This is a resource server, not a gate exclusive to one token type: the
 * inbound-integration endpoints accept a caller authenticated through ANY
 * registered provider — a self-issued integration-client token (external
 * systems provisioned via the admin API), or a human/service SSO or DHP
 * token (our own internal systems, already holding one of those). Both are
 * handled uniformly here.
 * <p>
 * Sender organization resolution is identical for both caller types by the
 * time a controller runs: {@code OrganizationContextFilter} already requires
 * and validates an {@code X-Organization-Id} header for either token type
 * (for an integration-client token, validated against the organization
 * baked into the token at issuance — a client can state its own
 * organization, never claim a different one), populating
 * {@link CurrentOrganizationContext} the same way for both.
 * <p>
 * Scope gating ({@code form058:submit}/{@code form0581:submit}) and source-key
 * matching only apply to integration-client tokens, since only those carry a
 * fixed, per-client scope/source identity at all — an SSO/DHP-authenticated
 * caller is gated the same way the frontend already is, by
 * {@code Form058CreateValidator}'s existing sender-organization-must-match-
 * current-organization check.
 */
public final class InboundCallerContext {

    private InboundCallerContext() {
    }

    public static Long resolveSenderOrganizationId() {
        return CurrentOrganizationContext.require().getId();
    }

    public static void requireScopeIfIntegrationClient(IntegrationScope scope) {
        Authentication authentication = currentAuthentication();

        if (!(authentication instanceof IntegrationClientAuthenticationToken)) {
            return;
        }

        String requiredAuthority = "SCOPE_" + scope.getClaim();

        boolean granted = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(requiredAuthority::equals);

        if (!granted) {
            // Message is a localization key, not free text - JsonAccessDeniedHandler only
            // forwards messages matching its message-code pattern to the client, exactly
            // like OrganizationContextFilter's "organization.required"/"organization.not_allowed".
            throw new AccessDeniedException("integration.scope.missing");
        }
    }

    /**
     * Confirms the {@code {source}} path segment matches the calling
     * integration client's own registered source key — e.g. a client
     * registered for "dmed" cannot submit through
     * {@code /integration/v1/lab-x/form-058} and have it attributed to
     * "lab-x". No-op for SSO/DHP callers, who have no registered source key
     * to check against.
     */
    public static void requireMatchingSourceKey(String pathSource) {
        Authentication authentication = currentAuthentication();

        if (!(authentication instanceof IntegrationClientAuthenticationToken integrationToken)) {
            return;
        }

        if (!integrationToken.getPrincipal().sourceKey().equalsIgnoreCase(pathSource)) {
            throw new AccessDeniedException("integration.source_key.mismatch");
        }
    }

    private static Authentication currentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}
