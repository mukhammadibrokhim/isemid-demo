package uz.uzinfocom.app.integration.inbound.common.web;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import uz.uzinfocom.app.platform.integrationclient.domain.IntegrationScope;
import uz.uzinfocom.app.platform.security.auth.IntegrationClientAuthentication;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;

/**
 * This is a resource server, not a gate exclusive to one token type or one
 * data direction: both the inbound submission endpoints ({@code
 * InboundForm058Controller}/{@code InboundForm0581Controller}/{@code
 * DmedForm058Controller}) and the outbound lookup endpoints ({@code
 * PatientCaseController}) accept a caller authenticated through ANY
 * registered provider — a self-issued integration-client credential
 * (client-credentials JWT, API key, Basic Auth, or IP allow-list — see
 * {@link IntegrationClientAuthentication}), or a human/service SSO or DHP
 * token (our own internal systems, already holding one of those). Both are
 * handled uniformly here, for either direction.
 * <p>
 * Organization resolution is identical for both caller types by the time a
 * controller runs: {@code OrganizationContextFilter} already requires and
 * validates an {@code X-Organization-Id} header for either token type (for
 * an integration-client credential, validated against the organization
 * bound to the client — a client can state its own organization, never
 * claim a different one), populating {@link CurrentOrganizationContext}
 * the same way for both.
 * <p>
 * Scope gating ({@code form058:submit}/{@code form0581:submit}/
 * {@code patient-case:read}) applies to every caller: an integration-client
 * credential satisfies it via its {@code SCOPE_*} authority, an SSO/DHP
 * caller via the equivalent {@code PERMISSION_*} authority granted through
 * the ordinary Role/Permission/Action admin CRUD (see {@link
 * IntegrationScope#permissionAuthority()}). Source-key matching stays
 * integration-client-only, since only those callers carry a fixed,
 * per-client source identity at all.
 */
public final class InboundCallerContext {

    private InboundCallerContext() {
    }

    public static Long resolveSenderOrganizationId() {
        return CurrentOrganizationContext.require().getId();
    }

    public static void requireScope(IntegrationScope scope) {
        Authentication authentication = currentAuthentication();

        boolean granted = hasAuthority(authentication, "SCOPE_" + scope.getClaim())
                || hasAuthority(authentication, scope.permissionAuthority());

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

        if (!(authentication instanceof IntegrationClientAuthentication integrationAuthentication)) {
            return;
        }

        if (!integrationAuthentication.getPrincipal().sourceKey().equalsIgnoreCase(pathSource)) {
            throw new AccessDeniedException("integration.source_key.mismatch");
        }
    }

    private static boolean hasAuthority(Authentication authentication, String authority) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority::equals);
    }

    private static Authentication currentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}
