package uz.uzinfocom.app.platform.security.auth;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public record CachedSecurityUser(
        Long userId,
        UUID uuid,
        String username,
        String nnuzb,
        Boolean active,
        Set<CachedSecurityOrganization> organizations
) {
    public CachedSecurityUser {
        organizations = organizations == null ? Set.of() : Set.copyOf(organizations);
    }

    public boolean isActive() {
        return Boolean.TRUE.equals(active);
    }

    public Optional<CachedSecurityOrganization> findOrganizationByUuid(UUID organizationUuid) {
        if (organizationUuid == null || organizations.isEmpty()) {
            return Optional.empty();
        }

        return organizations.stream()
                .filter(organization -> organizationUuid.equals(organization.uuid()))
                .findFirst();
    }
}
