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
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.iam.repository.OrganizationRepository;
import uz.uzinfocom.app.platform.security.auth.FederatedAuthenticationToken;
import uz.uzinfocom.app.platform.security.auth.CachedSecurityOrganization;
import uz.uzinfocom.app.platform.security.auth.IntegrationClientAuthenticationToken;
import uz.uzinfocom.app.platform.security.auth.SelectedOrganizationSecurityCacheService;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;
import uz.uzinfocom.app.platform.security.context.SecurityHeaders;
import uz.uzinfocom.app.platform.security.route.RequestPolicy;
import uz.uzinfocom.app.platform.security.route.RequestPolicyResolver;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrganizationContextFilter extends OncePerRequestFilter {

    private final RequestPolicyResolver requestPolicyResolver;
    private final SelectedOrganizationSecurityCacheService selectedOrganizationSecurityCacheService;
    private final OrganizationRepository organizationRepository;

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

        if (authentication instanceof IntegrationClientAuthenticationToken integrationToken) {
            // X-Organization-Id is required here too, exactly like the human path below -
            // not because the client could be bound to more than one organization (it can't,
            // see IntegrationClient), but so a client can never silently submit for whatever
            // organization happens to be baked into its token without saying so explicitly.
            // It must still match that bound organization: a client can state its own
            // organization, never claim a different one.
            String requestedOrganizationHeader = resolveOrganizationHeader(request);
            if (!StringUtils.hasText(requestedOrganizationHeader)) {
                throw new AccessDeniedException("organization.required");
            }

            UUID requestedOrganizationUuid = parseUuid(requestedOrganizationHeader);
            if (!requestedOrganizationUuid.equals(integrationToken.getPrincipal().organizationUuid())) {
                throw new AccessDeniedException("organization.not_allowed");
            }

            // findById, not getReferenceById: this filter runs with no active transaction
            // (open-in-view=false), so a lazy proxy from getReferenceById would be bound to
            // no session and blow up the moment anything downstream - even inside a later,
            // unrelated @Transactional method - touches a field beyond the ID. findById
            // executes its query eagerly (Spring Data wraps each repository call in its own
            // short transaction) and returns a fully hydrated, genuinely detached entity.
            Organization organization = organizationRepository.findById(
                            integrationToken.getPrincipal().organizationId())
                    .orElseThrow(() -> new AccessDeniedException("organization.not_allowed"));

            try {
                CurrentOrganizationContext.set(organization);
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
