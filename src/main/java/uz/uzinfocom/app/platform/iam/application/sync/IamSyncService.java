package uz.uzinfocom.app.platform.iam.application.sync;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.iam.domain.Role;
import uz.uzinfocom.app.platform.iam.domain.User;
import uz.uzinfocom.app.platform.iam.application.sync.dto.IamSyncResult;
import uz.uzinfocom.app.platform.security.claims.ExternalIdentityPayload;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class IamSyncService {

    private final OrganizationSyncService organizationSyncService;
    private final RoleSyncService roleSyncService;
    private final UserSyncService userSyncService;

    public IamSyncResult synchronize(ExternalIdentityPayload payload, String rawToken) {
        List<Organization> organizations = payload.organizationUuids().stream()
                .map(uuid -> organizationSyncService.resolve(payload.providerKey(), uuid, rawToken))
                .toList();

        Set<Role> roles = roleSyncService.resolve(payload.roleNames());

        User user = userSyncService.resolve(payload, rawToken, organizations, roles);

        return new IamSyncResult(user, roles, organizations);
    }
}
