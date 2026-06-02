package uz.uzinfocom.app.platform.security.claims;

import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
public class DhpIdentityClaimExtractor implements IdentityClaimExtractor {

    private static final String PROVIDER_KEY = "dhp";

    @Override
    public String providerKey() {
        return PROVIDER_KEY;
    }

    @Override
    public ExternalIdentityPayload extract(Jwt jwt) {
        String subject = jwt.getSubject();

        List<?> ctxList = JwtClaimUtils.listValue(jwt.getClaim("ctx"));

        UUID practitionerUuid = extractFirstPractitionerUuid(ctxList);

        if (practitionerUuid == null) {
            throw new InsufficientAuthenticationException(
                    "DHP token ctx.practitioner_ref is required"
            );
        }

        Set<ExternalOrganizationContext> organizationContexts = extractOrganizationContexts(ctxList);
        Set<String> roleNames = extractRoleNames(jwt);

        String username = StringUtils.hasText(subject) ? subject : practitionerUuid.toString();

        String nnuzb = firstNonBlank(
                JwtClaimUtils.stringValue(jwt.getClaim("nnuzb")),
                JwtClaimUtils.stringValue(jwt.getClaim("pinfl"))
        );

        return new ExternalIdentityPayload(
                PROVIDER_KEY,
                subject,
                practitionerUuid,
                username,
                nnuzb,
                organizationContexts,
                roleNames
        );
    }

    private UUID extractFirstPractitionerUuid(List<?> ctxList) {
        for (Object rawCtx : ctxList) {
            if (!(rawCtx instanceof Map<?, ?> ctx)) {
                continue;
            }

            UUID practitionerUuid = JwtClaimUtils.uuidFromAny(
                    JwtClaimUtils.firstMapValue(
                            ctx,
                            "practitioner_ref",
                            "practitioner-ref",
                            "practitionerRef",
                            "practitioner"
                    )
            ).orElse(null);

            if (practitionerUuid != null) {
                return practitionerUuid;
            }
        }

        return null;
    }

    private Set<ExternalOrganizationContext> extractOrganizationContexts(List<?> ctxList) {
        Set<ExternalOrganizationContext> result = new LinkedHashSet<>();

        for (Object rawCtx : ctxList) {
            if (!(rawCtx instanceof Map<?, ?> ctx)) {
                continue;
            }

            UUID organizationUuid = JwtClaimUtils.uuidFromAny(
                    JwtClaimUtils.firstMapValue(
                            ctx,
                            "organization_ref",
                            "organization-ref",
                            "organizationRef",
                            "organization"
                    )
            ).orElse(null);

            if (organizationUuid == null) {
                continue;
            }

            UUID practitionerRoleUuid = JwtClaimUtils.uuidFromAny(
                    JwtClaimUtils.firstMapValue(
                            ctx,
                            "role_ref",
                            "role-ref",
                            "roleRef",
                            "practitioner_role_ref",
                            "practitionerRoleRef"
                    )
            ).orElse(null);

            result.add(ExternalOrganizationContext.of(
                    organizationUuid,
                    practitionerRoleUuid,
                    null
            ));
        }

        return Set.copyOf(result);
    }

    private Set<String> extractRoleNames(Jwt jwt) {
        Set<String> result = new LinkedHashSet<>();

        addRoles(result, jwt.getClaim("roles"));
        addRoles(result, jwt.getClaim("role"));
        addRoles(result, jwt.getClaim("authorities"));

        return Set.copyOf(result);
    }

    private void addRoles(Set<String> result, Object rawRoles) {
        if (rawRoles == null) {
            return;
        }

        if (rawRoles instanceof String text) {
            if (!StringUtils.hasText(text)) {
                return;
            }

            for (String item : text.split("\\s+")) {
                String role = JwtClaimUtils.normalizeRoleName(item);

                if (StringUtils.hasText(role)) {
                    result.add(role);
                }
            }

            return;
        }

        for (Object rawRole : JwtClaimUtils.listValue(rawRoles)) {
            String role = JwtClaimUtils.normalizeRoleName(
                    JwtClaimUtils.stringValue(rawRole)
            );

            if (StringUtils.hasText(role)) {
                result.add(role);
            }
        }
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }

        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value.trim();
            }
        }

        return null;
    }
}