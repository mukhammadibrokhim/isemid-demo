package uz.uzinfocom.app.platform.iam.application.sync;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.iam.domain.Role;
import uz.uzinfocom.app.platform.iam.domain.User;
import uz.uzinfocom.app.platform.iam.domain.UserOrganizationRole;
import uz.uzinfocom.app.platform.iam.application.sync.mapper.UserRemoteMapper;
import uz.uzinfocom.app.platform.iam.infrastructure.remote.ProviderIamRemoteClient;
import uz.uzinfocom.app.platform.iam.repository.UserRepository;
import uz.uzinfocom.app.platform.iam.repository.UserOrganizationRoleRepository;
import uz.uzinfocom.app.platform.security.claims.ExternalIdentityPayload;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserSyncService {

    private final UserRepository userRepository;
    private final UserOrganizationRoleRepository userOrganizationRoleRepository;
    private final ProviderIamRemoteClient remoteClient;
    private final UserRemoteMapper mapper;

    @Transactional
    public User resolve(
            ExternalIdentityPayload payload,
            String rawToken,
            List<Organization> organizations,
            Set<Role> roles,
            Map<UUID, Set<Role>> organizationRoles
    ) {
        User user = userRepository.findByUuid(payload.practitionerUuid())
                .map(existing -> syncExisting(existing, organizations, roles))
                .orElseGet(() -> provision(payload, rawToken, organizations, roles));

        syncScopedRoles(user, organizationRoles);

        return user;
    }

    private User syncExisting(User user, List<Organization> organizations, Set<Role> roles) {
        boolean changed = false;

        Set<UUID> currentOrganizationUuids = user.getOrganizations() == null
                ? Set.of()
                : user.getOrganizations().stream()
                .filter(Objects::nonNull)
                .map(Organization::getUuid)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<UUID> tokenOrganizationUuids = organizations.stream()
                .filter(Objects::nonNull)
                .map(Organization::getUuid)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (!currentOrganizationUuids.equals(tokenOrganizationUuids)) {
            user.setOrganizations(new LinkedHashSet<>(organizations));
            changed = true;
        }

        Set<String> currentRoleNames = user.getRoles() == null
                ? Set.of()
                : user.getRoles().stream()
                .filter(Objects::nonNull)
                .map(Role::getName)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<String> tokenRoleNames = roles.stream()
                .filter(Objects::nonNull)
                .map(Role::getName)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (!currentRoleNames.equals(tokenRoleNames)) {
            user.setRoles(new LinkedHashSet<>(roles));
            changed = true;
        }

        return changed ? userRepository.save(user) : user;
    }

    private User provision(
            ExternalIdentityPayload payload,
            String rawToken,
            List<Organization> organizations,
            Set<Role> roles
    ) {
        User remoteUser = mapper.toEntity(
                remoteClient.fetchPractitioner(payload.providerKey(), payload.practitionerUuid(), rawToken),
                new LinkedHashSet<>(organizations),
                roles
        );

        try {
            return userRepository.saveAndFlush(remoteUser);
        } catch (DataIntegrityViolationException concurrentInsert) {
            return userRepository.findByUuid(payload.practitionerUuid())
                    .orElseThrow(() -> concurrentInsert);
        }
    }

    private void syncScopedRoles(User user, Map<UUID, Set<Role>> organizationRoles) {
        if (organizationRoles == null || organizationRoles.isEmpty()) {
            return;
        }

        if (user.getOrganizations() == null || user.getOrganizations().isEmpty()) {
            return;
        }

        Map<UUID, Organization> organizationsByUuid = user.getOrganizations().stream()
                .filter(Objects::nonNull)
                .filter(organization -> organization.getUuid() != null)
                .collect(Collectors.toMap(
                        Organization::getUuid,
                        organization -> organization
                ));

        for (Map.Entry<UUID, Set<Role>> entry : organizationRoles.entrySet()) {
            Organization organization = organizationsByUuid.get(entry.getKey());

            if (organization == null || entry.getValue() == null || entry.getValue().isEmpty()) {
                continue;
            }

            for (Role role : entry.getValue()) {
                if (role == null || !role.isAvailableForAuthorization()) {
                    continue;
                }

                userOrganizationRoleRepository
                        .findTopByUserIdAndOrganizationIdAndRoleIdOrderByIdDesc(
                                user.getId(),
                                organization.getId(),
                                role.getId()
                        )
                        .ifPresentOrElse(
                                assignment -> {
                                    if (!assignment.isActiveAssignment()) {
                                        assignment.restore();
                                    }
                                },
                                () -> userOrganizationRoleRepository.save(
                                        UserOrganizationRole.builder()
                                                .user(user)
                                                .organization(organization)
                                                .role(role)
                                                .active(true)
                                                .build()
                                )
                        );
            }
        }
    }
}
