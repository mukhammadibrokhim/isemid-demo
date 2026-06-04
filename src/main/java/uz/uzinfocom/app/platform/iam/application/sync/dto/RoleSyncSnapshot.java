package uz.uzinfocom.app.platform.iam.application.sync.dto;

import uz.uzinfocom.app.platform.iam.domain.Role;

public record RoleSyncSnapshot(
        Long id,
        String name,
        boolean active,
        boolean deleted
) {

    public static RoleSyncSnapshot from(Role role) {
        return new RoleSyncSnapshot(
                role.getId(),
                role.getName(),
                Boolean.TRUE.equals(role.getActive()),
                Boolean.TRUE.equals(role.getDeleted())
        );
    }

    public boolean availableForAuthorization() {
        return id != null && active && !deleted;
    }
}
