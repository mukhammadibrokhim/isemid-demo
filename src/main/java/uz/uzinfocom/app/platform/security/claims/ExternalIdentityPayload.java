package uz.uzinfocom.app.platform.security.claims;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record ExternalIdentityPayload(
        String providerKey,
        String subject,
        UUID practitionerUuid,
        String username,
        String nnuzb,
        Set<ExternalOrganizationContext> organizationContexts,
        Set<String> roleNames
) {
    public ExternalIdentityPayload {
        organizationContexts = organizationContexts == null
                ? Set.of()
                : Set.copyOf(organizationContexts);

        roleNames = roleNames == null
                ? Set.of()
                : roleNames.stream()
                .filter(role -> role != null && !role.isBlank())
                .map(String::trim)
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Backward-compatible constructor.
     * Old code:
     * new ExternalIdentityPayload(providerKey, practitionerUuid, username, nnuzb, organizationUuids, roleNames)
     */
    public ExternalIdentityPayload(
            String providerKey,
            UUID practitionerUuid,
            String username,
            String nnuzb,
            Set<UUID> organizationUuids,
            Set<String> roleNames
    ) {
        this(
                providerKey,
                null,
                practitionerUuid,
                username,
                nnuzb,
                toOrganizationContexts(organizationUuids),
                roleNames
        );
    }

    public Set<UUID> organizationUuids() {
        return organizationContexts.stream()
                .map(ExternalOrganizationContext::organizationUuid)
                .collect(Collectors.toUnmodifiableSet());
    }

    public Set<UUID> practitionerRoleUuids() {
        return organizationContexts.stream()
                .map(ExternalOrganizationContext::practitionerRoleUuid)
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableSet());
    }

    private static Set<ExternalOrganizationContext> toOrganizationContexts(Set<UUID> organizationUuids) {
        if (organizationUuids == null || organizationUuids.isEmpty()) {
            return Set.of();
        }

        Set<ExternalOrganizationContext> contexts = new LinkedHashSet<>();

        for (UUID organizationUuid : organizationUuids) {
            if (organizationUuid != null) {
                contexts.add(ExternalOrganizationContext.ofOrganization(organizationUuid));
            }
        }

        return Set.copyOf(contexts);
    }
}