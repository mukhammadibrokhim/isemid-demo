package uz.uzinfocom.isemid.commons.configs.security.jwt;

import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import uz.uzinfocom.isemid.commons.dtos.UserInfo;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.user.models.Role;
import uz.uzinfocom.isemid.features.user.models.User;

import java.util.List;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtAuthHelper jwtAuthHelper;

    @Override
    public AbstractAuthenticationToken convert(@Nonnull Jwt jwt) {

        HttpServletRequest request = jwtAuthHelper.getCurrentHttpRequest();
        String selectedOrgId = request.getHeader("Organization-id");

        boolean isReceiveFromLisRequest = request.getRequestURI().contains("receive-from-lis");
        boolean isMonitoring = request.getRequestURI().contains("monitoring");

        if (isMonitoring) {
            return new AuthToken(jwt, null, Set.of());
        }

        jwtAuthHelper.validateOrganizationHeader(selectedOrgId, request);

        UserInfo userInfo = jwtAuthHelper.parseUserInfo(jwt);

        if (!isReceiveFromLisRequest) {
            jwtAuthHelper.validateUserRoles(userInfo);
        }

        List<Organization> organizations = jwtAuthHelper.resolveOrganizations(userInfo, jwt);
        Set<Role> roles = jwtAuthHelper.resolveRoles(userInfo);

        User user = jwtAuthHelper.resolveUser(userInfo, jwt, organizations, roles);

        jwtAuthHelper.validateUserStatus(user);
        jwtAuthHelper.setUserCurrentOrganization(user, selectedOrgId, jwt);

        return new AuthToken(jwt, user, user.getRoles());
    }
}
