package uz.uzinfocom.app.platform.iam.application.sync.dto;

import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.iam.domain.Role;
import uz.uzinfocom.app.platform.iam.domain.User;

import java.util.List;
import java.util.Set;

public record IamSyncResult(
        User user,
        Set<Role> roles,
        List<Organization> organizations
) {
    public IamSyncResult {
        roles = roles == null ? Set.of() : Set.copyOf(roles);
        organizations = organizations == null ? List.of() : List.copyOf(organizations);
    }
}
