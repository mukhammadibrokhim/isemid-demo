package uz.uzinfocom.app.platform.iam.application.sync;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.iam.domain.Role;
import uz.uzinfocom.app.platform.iam.domain.User;
import uz.uzinfocom.app.platform.iam.application.sync.dto.IamSyncResult;
import uz.uzinfocom.app.platform.security.claims.ExternalOrganizationContext;
import uz.uzinfocom.app.platform.security.claims.ExternalIdentityPayload;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
        Map<UUID, Set<Role>> organizationRoles = resolveOrganizationRoles(payload, organizations, roles);
        Set<Role> effectiveRoles = unionRoles(roles, organizationRoles);

        User user = userSyncService.resolve(payload, rawToken, organizations, effectiveRoles, organizationRoles);

        return new IamSyncResult(user, effectiveRoles, organizations);
    }

    private Map<UUID, Set<Role>> resolveOrganizationRoles(
            ExternalIdentityPayload payload,
            List<Organization> organizations,
            Set<Role> fallbackRoles
    ) {
        Map<UUID, Set<Role>> rolesByOrganization = new LinkedHashMap<>();

        for (ExternalOrganizationContext context : payload.organizationContexts()) {
            Set<Role> scopedRoles = new LinkedHashSet<>();

            roleSyncService.resolveOne(context.practitionerRoleCode())
                    .ifPresent(scopedRoles::add);

            rolesByOrganization
                    .computeIfAbsent(context.organizationUuid(), ignored -> new LinkedHashSet<>())
                    .addAll(scopedRoles);
        }

        boolean noScopedRolesFromToken = rolesByOrganization.values()
                .stream()
                .allMatch(Set::isEmpty);

        if (noScopedRolesFromToken && fallbackRoles != null && !fallbackRoles.isEmpty()) {
            organizations.stream()
                    .map(Organization::getUuid)
                    .forEach(uuid -> rolesByOrganization
                            .computeIfAbsent(uuid, ignored -> new LinkedHashSet<>())
                            .addAll(fallbackRoles));
        }

        return rolesByOrganization.entrySet()
                .stream()
                .collect(Collectors.toUnmodifiableMap(
                        Map.Entry::getKey,
                        entry -> Set.copyOf(entry.getValue())
                ));
    }

    private Set<Role> unionRoles(Set<Role> tokenRoles, Map<UUID, Set<Role>> organizationRoles) {
        LinkedHashSet<Role> roles = new LinkedHashSet<>();

        if (tokenRoles != null) {
            roles.addAll(tokenRoles);
        }

        organizationRoles.values().forEach(roles::addAll);

        return Set.copyOf(roles);
    }
}
