package uz.uzinfocom.app.platform.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import uz.uzinfocom.app.modules.iam.domain.Organization;
import uz.uzinfocom.app.modules.iam.repository.OrganizationRepository;
import uz.uzinfocom.app.platform.integrationclient.domain.IntegrationAuthType;
import uz.uzinfocom.app.platform.integrationclient.domain.IntegrationClient;
import uz.uzinfocom.app.platform.integrationclient.repository.IntegrationClientRepository;
import uz.uzinfocom.app.platform.security.auth.FederatedAuthenticationToken;
import uz.uzinfocom.app.platform.security.auth.CachedSecurityOrganization;
import uz.uzinfocom.app.platform.security.auth.IntegrationClientAuthentication;
import uz.uzinfocom.app.platform.security.auth.SelectedOrganizationSecurityCacheService;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;
import uz.uzinfocom.app.platform.security.context.SecurityHeaders;
import uz.uzinfocom.app.platform.security.ip.IpAllowlistMatcher;
import uz.uzinfocom.app.platform.settings.application.RequestPolicy;
import uz.uzinfocom.app.platform.settings.application.RequestPolicyResolver;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrganizationContextFilter extends OncePerRequestFilter {

    private final RequestPolicyResolver requestPolicyResolver;
    private final SelectedOrganizationSecurityCacheService selectedOrganizationSecurityCacheService;
    private final OrganizationRepository organizationRepository;
    private final IntegrationClientRepository integrationClientRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        CurrentOrganizationContext.clear();

        RequestPolicy policy = requestPolicyResolver.resolve(request);
        if (policy.publicRoute()) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof IntegrationClientAuthentication integrationAuthentication) {
            // The sending organization of an integration client is named in the request body and
            // validated against the client's default organization / allow-list by the controller
            // (see InboundSenderResolver), so the header is only required for a client that still
            // has a default organization - and one, if sent, must name an organization the client
            // may use (EXTERNAL_TOKEN clients with no list: any organization of their provider).
            //
            // The client row is re-read per request (not baked into a JWT at issuance, for
            // credential kinds that even involve one) so revoking, deactivating, or
            // narrowing the allow-list takes effect immediately on the very next request -
            // including one carrying an already-issued, not-yet-expired Bearer JWT - rather
            // than only blocking new token issuance.
            Optional<IntegrationClient> foundClient = integrationClientRepository
                    .findByClientId(integrationAuthentication.getPrincipal().clientId());
            boolean externalToken = foundClient
                    .map(client -> client.getAuthType() == IntegrationAuthType.EXTERNAL_TOKEN)
                    .orElse(false);

            String requestedOrganizationHeader = resolveOrganizationHeader(request);
            if (!externalToken && integrationAuthentication.getPrincipal().organizationId() != null
                    && !StringUtils.hasText(requestedOrganizationHeader)) {
                throw new AccessDeniedException("organization.required");
            }

            Organization headerOrganization = null;
            if (StringUtils.hasText(requestedOrganizationHeader)) {
                UUID requestedOrganizationUuid = parseUuid(requestedOrganizationHeader);
                boolean isDefault = requestedOrganizationUuid.equals(integrationAuthentication.getPrincipal().organizationUuid());
                if (!isDefault) {
                    IntegrationClient headerClient = foundClient
                            .orElseThrow(() -> new AccessDeniedException("organization.not_allowed"));
                    headerOrganization = organizationRepository.findByUuid(requestedOrganizationUuid)
                            .filter(organization -> headerClient.getProviderKey() == null
                                    || Objects.equals(organization.getProviderKey(), headerClient.getProviderKey()))
                            .orElse(null);
                    if (headerOrganization == null) {
                        throw new AccessDeniedException("organization.not_allowed");
                    }
                }
            }

            IntegrationClient integrationClient = foundClient
                    .orElseThrow(() -> new AccessDeniedException("integration.ip.not_allowed"));

            if (!integrationClient.isActive()) {
                throw new AccessDeniedException("integration.client.inactive");
            }

            if (!IpAllowlistMatcher.isAllowed(request.getRemoteAddr(), integrationClient.getAllowedIps())) {
                throw new AccessDeniedException("integration.ip.not_allowed");
            }

            // findById, not getReferenceById: this filter runs with no active transaction
            // (open-in-view=false), so a lazy proxy from getReferenceById would be bound to
            // no session and blow up the moment anything downstream - even inside a later,
            // unrelated @Transactional method - touches a field beyond the ID. findById
            // executes its query eagerly (Spring Data wraps each repository call in its own
            // short transaction) and returns a fully hydrated, genuinely detached entity.
            Long defaultOrganizationId = integrationAuthentication.getPrincipal().organizationId();
            Organization organization = defaultOrganizationId == null
                    ? null
                    : organizationRepository.findById(defaultOrganizationId)
                    .orElseThrow(() -> new AccessDeniedException("organization.not_allowed"));
            // An EXTERNAL_TOKEN client may have no default organization: the context then comes
            // from the header if one was sent, otherwise InboundSenderResolver sets it from the body.
            if (organization == null) {
                organization = headerOrganization;
            }

            try {
                if (organization != null) {
                    CurrentOrganizationContext.set(organization);
                }
                filterChain.doFilter(request, response);
            } finally {
                CurrentOrganizationContext.clear();
            }
            return;
        }

        if (!(authentication instanceof FederatedAuthenticationToken federatedToken)) {
            filterChain.doFilter(request, response);
            return;
        }

        String selectedOrganizationHeader = resolveOrganizationHeader(request);
        if (!StringUtils.hasText(selectedOrganizationHeader)) {
            if (policy.organizationHeaderRequired()) {
                throw new AccessDeniedException("organization.required");
            }

            filterChain.doFilter(request, response);
            return;
        }

        UUID selectedOrganizationUuid = parseUuid(selectedOrganizationHeader);

        CachedSecurityOrganization selectedOrganization = selectedOrganizationSecurityCacheService
                .resolveSelectedOrganization(federatedToken.getUserId(), selectedOrganizationUuid)
                .orElseThrow(() -> new AccessDeniedException("organization.not_allowed"));

        try {
            CurrentOrganizationContext.set(selectedOrganization.toDetachedOrganization());
            filterChain.doFilter(request, response);
        } finally {
            CurrentOrganizationContext.clear();
        }
    }

    private String resolveOrganizationHeader(HttpServletRequest request) {
        String canonical = request.getHeader(SecurityHeaders.ORGANIZATION_ID);

        if (StringUtils.hasText(canonical)) {
            return canonical;
        }

        String legacy = request.getHeader(SecurityHeaders.LEGACY_ORGANIZATION_ID);

        if (StringUtils.hasText(legacy)) {
            log.warn(
                    "Legacy organization header '{}' is used. Migrate clients to '{}'.",
                    SecurityHeaders.LEGACY_ORGANIZATION_ID,
                    SecurityHeaders.ORGANIZATION_ID
            );
        }

        return legacy;
    }

    private UUID parseUuid(String raw) {
        try {
            return UUID.fromString(raw.trim());
        } catch (IllegalArgumentException invalidUuid) {
            throw new AccessDeniedException("organization.invalid");
        }
    }
}
