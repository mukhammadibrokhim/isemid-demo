package uz.uzinfocom.isemid.commons.configs.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.oauth2.jwt.Jwt;
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
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static uz.uzinfocom.isemid.commons.utils.CommonUtil.WHITELIST_HEADER_CHECK;
import static uz.uzinfocom.isemid.commons.utils.CommonUtil.checkReqUri;
import static uz.uzinfocom.isemid.commons.utils.MessageUtil.*;

@RequiredArgsConstructor
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final OrganizationService organizationService;
    private final UserService userService;
    private final RoleService roleService;
    private final ObjectMapper objectMapper;

    @Override
    public AbstractAuthenticationToken convert(@Nonnull Jwt jwt) {
        HttpServletRequest request = getCurrentHttpRequest();
        String selectedOrgId = request.getHeader("Organization-id");
        boolean isReceiveFromLisRequest = request.getRequestURI().contains("receive-from-lis");

        validateOrganizationHeader(selectedOrgId, request);

        UserInfo userInfo = parseUserInfo(jwt);

        if (!isReceiveFromLisRequest) {
            validateUserRoles(userInfo);
        }

        List<Organization> organizations = resolveOrganizations(userInfo, jwt);
        Set<Role> roles = resolveRoles(userInfo);
        User user = resolveUser(userInfo, jwt, organizations, roles);

        validateUserStatus(user);
        setUserCurrentOrganization(user, organizations, selectedOrgId, jwt);

        return new AuthToken(jwt, user, user.getRoles());
    }

    private HttpServletRequest getCurrentHttpRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new IllegalStateException(NO_HTTP_REQUEST_CONTEXT);
        }
        return attributes.getRequest();
    }

    private void validateOrganizationHeader(String selectedOrgId, HttpServletRequest request) {
        if (!checkReqUri(request, WHITELIST_HEADER_CHECK) && (selectedOrgId == null || selectedOrgId.isBlank())) {
            throw new InsufficientAuthenticationException(REQUIRED_ORGANIZATION_HEADER);
        }
    }

    private UserInfo parseUserInfo(Jwt jwt) {
        return objectMapper.convertValue(jwt.getClaim("user_info"), UserInfo.class);
    }

    private void validateUserRoles(UserInfo userInfo) {
        List<Role> availableRoles = roleService.getAllRoles();
        boolean hasAuthorizedRole = userInfo.getRoles().stream()
                .anyMatch(userRole -> availableRoles.stream()
                        .map(Role::getName)
                        .anyMatch(userRole::equals));

        if (!hasAuthorizedRole) {
            throw new InsufficientAuthenticationException(NO_AUTHORIZED_ROLE_FOUND);
        }
    }

    private List<Organization> resolveOrganizations(UserInfo userInfo, Jwt jwt) {
        return userInfo.getOrganization().stream()
                .map(orgDto -> organizationService.findById(orgDto.getUuid())
                        .orElseGet(() -> organizationService.fetchOrganizationFromMisAndSave(orgDto.getUuid(), "Bearer " + jwt.getTokenValue())))
                .toList();
    }

    private Set<Role> resolveRoles(UserInfo userInfo) {
        return userInfo.getRoles().stream()
                .map(roleService::findOrCreateRoleByName)
                .collect(Collectors.toSet());
    }

    private User resolveUser(UserInfo userInfo, Jwt jwt, List<Organization> organizations, Set<Role> roles) {
        return userService.findByUuid(userInfo.getPractitioner())
                .orElseGet(() -> userService.fetchUserFromMisAndSave(userInfo.getPractitioner(),
                        "Bearer " + jwt.getTokenValue(), organizations, roles));
    }

    private void validateUserStatus(User user) {
        if (!user.isActive()) {
            throw new InsufficientAuthenticationException(USER_NOT_ACTIVE);
        }
    }

    private void setUserCurrentOrganization(User user, List<Organization> organizations, String selectedOrgId, Jwt jwt) {
        if (selectedOrgId != null && !selectedOrgId.isEmpty()) {
            try {
                UUID selectedUuid = UUID.fromString(selectedOrgId);
                Organization matchingOrg = organizations.stream()
                        .filter(org -> org.getUuid().equals(selectedUuid))
                        .findFirst()
                        .orElseGet(() -> organizationService.fetchOrganizationFromMisAndSave(selectedUuid, "Bearer " + jwt.getTokenValue()));
//                        .orElseThrow(() -> new InsufficientAuthenticationException(CURRENT_ORGANIZATION_NOT_FOUND_FOR_USER));
                user.setCurrentOrganization(matchingOrg);

            } catch (Exception e) {
                throw new InsufficientAuthenticationException(CURRENT_ORGANIZATION_NOT_FOUND);
            }

        }
    }
}
