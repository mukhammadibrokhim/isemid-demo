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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import uz.uzinfocom.app.platform.iam.application.user.query.UserOrganizationRoleQueryService;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.iam.domain.Role;
import uz.uzinfocom.app.platform.iam.repository.OrganizationRepository;
import uz.uzinfocom.app.platform.iam.repository.UserRepository;
import uz.uzinfocom.app.platform.security.auth.FederatedAuthenticationToken;
import uz.uzinfocom.app.platform.security.auth.SecurityAuthorityService;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;
import uz.uzinfocom.app.platform.security.context.SecurityHeaders;
import uz.uzinfocom.app.platform.security.route.RequestPolicy;
import uz.uzinfocom.app.platform.security.route.RequestPolicyResolver;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrganizationContextFilter extends OncePerRequestFilter {

    private final RequestPolicyResolver requestPolicyResolver;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final UserOrganizationRoleQueryService userOrganizationRoleQueryService;
    private final SecurityAuthorityService securityAuthorityService;

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
        if (!(authentication instanceof FederatedAuthenticationToken federatedToken)) {
            filterChain.doFilter(request, response);
            return;
        }

        String selectedOrganizationHeader = resolveOrganizationHeader(request);
        if (!StringUtils.hasText(selectedOrganizationHeader)) {
            if (policy.organizationHeaderRequired()) {
                throw new AccessDeniedException(SecurityHeaders.ORGANIZATION_ID + " header is required");
            }

            filterChain.doFilter(request, response);
            return;
        }

        UUID selectedOrganizationUuid = parseUuid(selectedOrganizationHeader);

        Organization selectedOrganization = organizationRepository.findByUuid(selectedOrganizationUuid)
                .orElseThrow(() -> new AccessDeniedException("Selected organization does not exist"));

        Long userId = federatedToken.getUserId();

        boolean userBelongsToOrganization = userRepository.existsByIdAndOrganizations_Id(
                userId,
                selectedOrganization.getId()
        );

        if (!userBelongsToOrganization) {
            throw new AccessDeniedException("Selected organization is not assigned to the authenticated user");
        }

        List<Role> scopedRoles = userOrganizationRoleQueryService.activeRoles(
                userId,
                selectedOrganization.getId()
        );

        if (policy.roleValidationRequired() && scopedRoles.isEmpty()) {
            throw new AccessDeniedException("No active role is assigned to the user in the selected organization");
        }

        Collection<? extends GrantedAuthority> scopedAuthorities =
                securityAuthorityService.loadAuthoritiesByRoles(scopedRoles);

        FederatedAuthenticationToken scopedToken = federatedToken.withScopedAuthorities(
                scopedAuthorities,
                selectedOrganizationUuid
        );

        try {
            CurrentOrganizationContext.set(selectedOrganization);
            SecurityContextHolder.getContext().setAuthentication(scopedToken);

            filterChain.doFilter(request, response);
        } finally {
            SecurityContextHolder.getContext().setAuthentication(authentication);
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
            throw new AccessDeniedException("Invalid organization UUID in request header");
        }
    }
}