package uz.uzinfocom.app.platform.iam.application.sync.dto;

import uz.uzinfocom.app.platform.iam.domain.User;

import java.util.Set;
import java.util.UUID;

public record UserSyncResult(
        User user,
        boolean created,
        boolean userDataChanged,
        boolean organizationsChanged,
        boolean rolesChanged,
        Set<UUID> affectedOrganizationUuids
) {
    public UserSyncResult {
        affectedOrganizationUuids = affectedOrganizationUuids == null
                ? Set.of()
                : Set.copyOf(affectedOrganizationUuids);
    }

    public boolean changed() {
        return created || userDataChanged || organizationsChanged || rolesChanged;
    }

    public static UserSyncResult unchanged(User user) {
        return new UserSyncResult(user, false, false, false, false, Set.of());
    }

    public static UserSyncResult created(User user) {
        return new UserSyncResult(user, true, false, false, false, Set.of());
    }
}
