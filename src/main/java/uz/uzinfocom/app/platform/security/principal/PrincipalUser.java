package uz.uzinfocom.app.platform.security.principal;

import java.util.UUID;

public record PrincipalUser(
        Long id,
        UUID uuid,
        String username,
        String nnuzb,
        Boolean active,
        UUID selectedOrganizationUuid
) {

    public PrincipalUser withSelectedOrganizationUuid(UUID selectedOrganizationUuid) {
        return new PrincipalUser(
                id,
                uuid,
                username,
                nnuzb,
                active,
                selectedOrganizationUuid
        );
    }

    public boolean isActive() {
        return Boolean.TRUE.equals(active);
    }
}