package uz.uzinfocom.isemid.configs.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import uz.uzinfocom.isemid.models.entities.Organization;
import uz.uzinfocom.isemid.models.entities.Role;
import uz.uzinfocom.isemid.models.entities.User;
import uz.uzinfocom.isemid.models.entities.dtos.UserInfo;
import uz.uzinfocom.isemid.services.OrganizationService;
import uz.uzinfocom.isemid.services.RoleService;
import uz.uzinfocom.isemid.services.UserService;

import java.util.List;

public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    final OrganizationService organizationService;
    final UserService userService;
    final RoleService roleService;

    public JwtAuthConverter(OrganizationService organizationService, UserService userService, RoleService roleService) {
        this.organizationService = organizationService;
        this.userService = userService;
        this.roleService = roleService;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        ObjectMapper objectMapper = new ObjectMapper();
        UserInfo userInfo = objectMapper.convertValue(jwt.getClaim("user_info"), UserInfo.class);

        List<Organization> organizations = userInfo.getOrganization().stream()
                .map(orgDto -> organizationService.findByUuid(orgDto.getUuid())
                        .orElseGet(() -> organizationService.fetchOrganizationFromMisAndSave(orgDto.getUuid(), "Bearer " + jwt.getTokenValue()))).toList();
        List<Role> roles = userInfo.getRoles().stream()
                .map(roleService::findOrCreateRoleByName).toList();

        User user = userService.findByUuid(userInfo.getPractitioner())
                .orElseGet(() -> userService.fetchUserFromMisAndSave(userInfo.getPractitioner(), "Bearer " + jwt.getTokenValue(), organizations, roles));
        if (!user.isActive()) throw new RuntimeException();
        return new AuthToken(jwt, user, user.getRoles());
    }
}
