package uz.uzinfocom.app.modules.iam.application.sync.dto;

import uz.uzinfocom.app.modules.iam.domain.Role;

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
