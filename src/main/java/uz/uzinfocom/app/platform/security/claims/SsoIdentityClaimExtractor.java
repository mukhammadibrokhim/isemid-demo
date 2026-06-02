package uz.uzinfocom.app.platform.security.claims;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
public class SsoIdentityClaimExtractor implements IdentityClaimExtractor {

    private static final String PROVIDER_KEY = "sso";

    @Override
    public String providerKey() {
        return PROVIDER_KEY;
    }

    @Override
    public ExternalIdentityPayload extract(Jwt jwt) {
        Map<String, Object> userInfo = JwtClaimUtils.requiredMap(
                jwt.getClaims(),
                "user_info"
        );

        String subject = jwt.getSubject();

        UUID practitionerUuid = JwtClaimUtils.requiredUuid(
                userInfo.get("practitioner"),
                "SSO token user_info.practitioner is required"
        );

        String username = JwtClaimUtils.stringFromMap(userInfo, "name");

        if (!StringUtils.hasText(username)) {
            username = subject;
        }

        Set<ExternalOrganizationContext> organizationContexts = extractOrganizationContexts(userInfo);
        Set<String> roleNames = extractRoleNames(userInfo);

        return new ExternalIdentityPayload(
                PROVIDER_KEY,
                subject,
                practitionerUuid,
                username,
                null,
                organizationContexts,
                roleNames
        );
    }

    private Set<ExternalOrganizationContext> extractOrganizationContexts(Map<String, Object> userInfo) {
        Object rawOrganizations = userInfo.get("organization");

        if (!(rawOrganizations instanceof List<?> organizations)) {
            return Set.of();
        }

        Set<ExternalOrganizationContext> result = new LinkedHashSet<>();

        for (Object rawOrganization : organizations) {
            if (!(rawOrganization instanceof Map<?, ?> organization)) {
                continue;
            }

            UUID organizationUuid = JwtClaimUtils.uuidFromAny(organization.get("uuid"))
                    .orElse(null);

            if (organizationUuid == null) {
                continue;
            }

            Object rawPractitionerRoles = organization.get("practitioner_role");

            if (!(rawPractitionerRoles instanceof List<?> practitionerRoles)
                    || practitionerRoles.isEmpty()) {
                result.add(ExternalOrganizationContext.ofOrganization(organizationUuid));
                continue;
            }

            for (Object rawPractitionerRole : practitionerRoles) {
                if (!(rawPractitionerRole instanceof Map<?, ?> practitionerRole)) {
                    continue;
                }

                UUID practitionerRoleUuid = JwtClaimUtils.uuidFromAny(practitionerRole.get("uuid"))
                        .orElse(null);

                String practitionerRoleCode = JwtClaimUtils.stringValue(practitionerRole.get("code"));

                result.add(ExternalOrganizationContext.of(
                        organizationUuid,
                        practitionerRoleUuid,
                        practitionerRoleCode
                ));
            }
        }

        return Set.copyOf(result);
    }

    private Set<String> extractRoleNames(Map<String, Object> userInfo) {
        Object rawRoles = userInfo.get("roles");

        if (!(rawRoles instanceof List<?> roles)) {
            return Set.of();
        }

        Set<String> result = new LinkedHashSet<>();

        for (Object rawRole : roles) {
            String role = JwtClaimUtils.normalizeRoleName(
                    JwtClaimUtils.stringValue(rawRole)
            );

            if (StringUtils.hasText(role)) {
                result.add(role);
            }
        }

        return Set.copyOf(result);
    }
}