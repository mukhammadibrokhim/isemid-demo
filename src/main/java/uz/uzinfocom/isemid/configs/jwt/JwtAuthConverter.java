package uz.uzinfocom.isemid.configs.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uz.uzinfocom.isemid.exceptioins.ForbiddenException;
import uz.uzinfocom.isemid.models.entities.Organization;
import uz.uzinfocom.isemid.models.entities.Role;
import uz.uzinfocom.isemid.models.entities.User;
import uz.uzinfocom.isemid.models.entities.dtos.UserInfo;
import uz.uzinfocom.isemid.services.OrganizationService;
import uz.uzinfocom.isemid.services.RoleService;
import uz.uzinfocom.isemid.services.UserService;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    final OrganizationService organizationService;
    final UserService userService;
    final RoleService roleService;
    final ObjectMapper objectMapper;

    public JwtAuthConverter(OrganizationService organizationService, UserService userService, RoleService roleService, ObjectMapper objectMapper) {
        this.organizationService = organizationService;
        this.userService = userService;
        this.roleService = roleService;
        this.objectMapper = objectMapper;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        HttpServletRequest request = getCurrentHttpRequest();
        String selectedOrgId = request.getHeader("Organization-id");
        boolean isReceiveFromLisRequest = request.getRequestURI().contains("receive-from-lis");

//        validateOrganizationHeader(selectedOrgId, isReceiveFromLisRequest,request);

        UserInfo userInfo = parseUserInfo(jwt);

        if (!isReceiveFromLisRequest) {
            validateUserRoles(userInfo,request);
        }

        List<Organization> organizations = resolveOrganizations(userInfo, jwt);
        Set<Role> roles = resolveRoles(userInfo);
        User user = resolveUser(userInfo, jwt, organizations, roles);

        validateUserStatus(user,request);
        setUserCurrentOrganization(user, organizations, selectedOrgId, request);

        return new AuthToken(jwt, user, user.getRoles());
    }

    private HttpServletRequest getCurrentHttpRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new IllegalStateException("No current HTTP request context available");
        }
        return attributes.getRequest();
    }

    private void validateOrganizationHeader(String selectedOrgId, boolean isReceiveFromLisRequest, HttpServletRequest request) {
        if (!isReceiveFromLisRequest && (selectedOrgId == null || selectedOrgId.isEmpty())) {
            request.setAttribute("FORBIDDEN", true);
            request.setAttribute("ERROR","Organization-id is required in the header." );
            throw new ForbiddenException("Organization-id is required in the header.");
        }
    }

    private UserInfo parseUserInfo(Jwt jwt) {
        return objectMapper.convertValue(jwt.getClaim("user_info"), UserInfo.class);
    }

    private void validateUserRoles(UserInfo userInfo, HttpServletRequest request) {
        List<Role> availableRoles = roleService.getAllRoles();
        boolean hasAuthorizedRole = userInfo.getRoles().stream()
                .anyMatch(userRole -> availableRoles.stream()
                        .map(Role::getName)
                        .anyMatch(userRole::equals));

        if (!hasAuthorizedRole) {
            request.setAttribute("FORBIDDEN", true);
            request.setAttribute("ERROR","No authorized role found for user." );
            throw new ForbiddenException("No authorized role found for user.");
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

    private void validateUserStatus(User user, HttpServletRequest request) {
        if (!user.isActive()) {
            request.setAttribute("FORBIDDEN", true);
            request.setAttribute("ERROR","User is not active." );
            throw new ForbiddenException("User is not active.");
        }
    }

    private void setUserCurrentOrganization(User user, List<Organization> organizations, String selectedOrgId, HttpServletRequest request) {
        if (selectedOrgId != null && !selectedOrgId.isEmpty()) {
            UUID selectedUuid = UUID.fromString(selectedOrgId);
            Organization matchingOrg = organizations.stream()
                    .filter(org -> org.getUuid().equals(selectedUuid))
                    .findFirst().orElse(null);
//                    .orElseThrow(() -> {
//                        request.setAttribute("FORBIDDEN", true);
//                        request.setAttribute("ERROR", "Organization does not exist.");
////                        return new ForbiddenException("Organization not found for user or access denied");
//                    });
            user.setCurrentOrganization(matchingOrg);

        }
    }
}
