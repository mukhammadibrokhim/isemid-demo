package uz.uzinfocom.app.platform.security.claims;

import java.util.UUID;

public record ExternalOrganizationContext(
        UUID organizationUuid,
        UUID practitionerRoleUuid,
        String practitionerRoleCode
) {
    public ExternalOrganizationContext {
        if (organizationUuid == null) {
            throw new IllegalArgumentException("organizationUuid must not be null");
        }
    }

    public static ExternalOrganizationContext ofOrganization(UUID organizationUuid) {
        return new ExternalOrganizationContext(organizationUuid, null, null);
    }

    public static ExternalOrganizationContext of(
            UUID organizationUuid,
            UUID practitionerRoleUuid,
            String practitionerRoleCode
    ) {
        return new ExternalOrganizationContext(
                organizationUuid,
                practitionerRoleUuid,
                practitionerRoleCode
        );
    }
}