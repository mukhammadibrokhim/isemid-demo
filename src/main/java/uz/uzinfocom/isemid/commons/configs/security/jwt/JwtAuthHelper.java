package uz.uzinfocom.isemid.commons.configs.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uz.uzinfocom.isemid.commons.dtos.UserInfo;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.organization.services.OrganizationService;
import uz.uzinfocom.isemid.features.user.models.Role;
import uz.uzinfocom.isemid.features.user.models.User;
import uz.uzinfocom.isemid.features.user.services.RoleService;
import uz.uzinfocom.isemid.features.user.services.UserService;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static uz.uzinfocom.isemid.commons.utils.CommonUtil.WHITELIST_HEADER_CHECK;
import static uz.uzinfocom.isemid.commons.utils.CommonUtil.checkReqUri;
import static uz.uzinfocom.isemid.commons.utils.MessageUtil.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtAuthHelper {
    private final OrganizationService organizationService;
    private final UserService userService;
    private final RoleService roleService;
    private final ObjectMapper objectMapper;


    protected HttpServletRequest getCurrentHttpRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new IllegalStateException(NO_HTTP_REQUEST_CONTEXT);
        }
        return attributes.getRequest();
    }

    protected void validateOrganizationHeader(String selectedOrgId, HttpServletRequest request) {
        if (!checkReqUri(request, WHITELIST_HEADER_CHECK) && (selectedOrgId == null || selectedOrgId.isBlank())) {
            throw new InsufficientAuthenticationException(REQUIRED_ORGANIZATION_HEADER);
        }
    }

    protected UserInfo parseUserInfo(Jwt jwt) {
        return objectMapper.convertValue(jwt.getClaim("user_info"), UserInfo.class);
    }

    protected void validateUserRoles(UserInfo userInfo) {
        List<Role> availableRoles = roleService.getAllRoles();
        boolean hasAuthorizedRole = userInfo.getRoles().stream()
                .anyMatch(userRole -> availableRoles.stream()
                        .map(Role::getName)
                        .anyMatch(userRole::equals));

        if (!hasAuthorizedRole) {
            log.warn("Roles not authorize: {}", userInfo.getRoles());
//            throw new InsufficientAuthenticationException(NO_AUTHORIZED_ROLE_FOUND);
        }
    }

    protected List<Organization> resolveOrganizations(UserInfo userInfo, Jwt jwt) {
        return userInfo.getOrganization().stream()
                .map(orgDto -> organizationService.findById(orgDto.getUuid())
                        .orElseGet(() -> organizationService.fetchOrganizationFromMisAndSave(orgDto.getUuid(), "Bearer " + jwt.getTokenValue())))
                .toList();
    }

    protected Set<Role> resolveRoles(UserInfo userInfo) {
        return userInfo.getRoles().stream()
                .map(roleService::findOrCreateRoleByName)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    protected User resolveUser(UserInfo userInfo, Jwt jwt, List<Organization> organizationsFromToken, Set<Role> rolesFromToken) {
        return userService.findByUuid(userInfo.getPractitioner())
                .map(existingUser -> {
                    // === 1. ROLES UPDATE ===
                    Set<String> existingRoleNames = existingUser.getRoles().stream()
                            .map(Role::getName)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet());

                    Set<String> tokenRoleNames = rolesFromToken.stream()
                            .filter(Objects::nonNull)
                            .map(Role::getName)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet());

                    if (!existingRoleNames.equals(tokenRoleNames)) {
                        existingUser.setRoles(rolesFromToken);
                        log.info("Updated roles for user [{}]: {} -> {}", existingUser.getUsername(), existingRoleNames, tokenRoleNames);
                    }

                    // === 2. ORGANIZATIONS UPDATE ===
                    Set<UUID> existingOrgIds = existingUser.getOrganizations().stream()
                            .map(Organization::getUuid)
                            .collect(Collectors.toSet());

                    Set<UUID> tokenOrgIds = organizationsFromToken.stream()
                            .map(Organization::getUuid)
                            .collect(Collectors.toSet());

                    if (!existingOrgIds.equals(tokenOrgIds)) {
                        existingUser.setOrganizations(organizationsFromToken);
                        log.info("Updated organizations for user [{}]: {} -> {}", existingUser.getUsername(), existingOrgIds, tokenOrgIds);
                    }

                    return userService.save(existingUser);
                })
                .orElseGet(() -> {
                    log.info("User not found in DB. Fetching from MIS and creating...");
                    return userService.fetchUserFromMisAndSave(
                            userInfo.getPractitioner(),
                            jwt.getTokenValue(),
                            organizationsFromToken,
                            rolesFromToken
                    );
                });
    }


    protected void validateUserStatus(User user) {
        if (!user.isActive()) {
            throw new InsufficientAuthenticationException(USER_NOT_ACTIVE);
        }
    }

    protected void setUserCurrentOrganization(User user, String selectedOrgId, Jwt jwt) {
        if (selectedOrgId != null && !selectedOrgId.isEmpty()) {
            try {
                UUID selectedUuid = UUID.fromString(selectedOrgId);
                Organization matchingOrg = organizationService.getAllOrganizations().stream()
                        .filter(org -> org.getUuid().equals(selectedUuid))
                        .findFirst()
                        .orElseGet(() -> organizationService.fetchOrganizationFromMisAndSave(selectedUuid, jwt.getTokenValue()));
//                        .orElseThrow(() -> new InsufficientAuthenticationException(CURRENT_ORGANIZATION_NOT_FOUND_FOR_USER));
                user.setCurrentOrganization(matchingOrg);

            } catch (Exception e) {
                throw new InsufficientAuthenticationException(CURRENT_ORGANIZATION_NOT_FOUND);
            }

        }
    }
}
