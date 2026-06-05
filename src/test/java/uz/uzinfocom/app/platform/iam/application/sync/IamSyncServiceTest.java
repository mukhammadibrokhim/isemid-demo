package uz.uzinfocom.app.platform.iam.application.sync;

import org.junit.jupiter.api.Test;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.security.access.AccessDeniedException;
import uz.uzinfocom.app.platform.cache.SecurityCacheNames;
import uz.uzinfocom.app.platform.iam.application.sync.dto.IamSyncResult;
import uz.uzinfocom.app.platform.iam.application.sync.dto.UserSyncResult;
import uz.uzinfocom.app.platform.iam.application.sync.mapper.OrganizationRemoteMapper;
import uz.uzinfocom.app.platform.iam.application.sync.mapper.UserRemoteMapper;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.iam.domain.Role;
import uz.uzinfocom.app.platform.iam.domain.User;
import uz.uzinfocom.app.platform.iam.domain.enums.MedicalType;
import uz.uzinfocom.app.platform.iam.domain.enums.OrganizationLevel;
import uz.uzinfocom.app.platform.iam.infrastructure.remote.ProviderIamRemoteClient;
import uz.uzinfocom.app.platform.iam.infrastructure.remote.payload.RemoteOrganizationPayload;
import uz.uzinfocom.app.platform.iam.infrastructure.remote.payload.RemotePractitionerPayload;
import uz.uzinfocom.app.platform.iam.repository.OrganizationRepository;
import uz.uzinfocom.app.platform.iam.repository.RoleRepository;
import uz.uzinfocom.app.platform.iam.repository.UserRepository;
import uz.uzinfocom.app.platform.security.claims.ExternalIdentityPayload;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IamSyncServiceTest {

    @Test
    void missingUserAndOrganizationAreSyncedFromTokenClaims() {
        UUID userUuid = UUID.randomUUID();
        UUID organizationUuid = UUID.randomUUID();
        String providerKey = "sso";
        String rawToken = "token";

        OrganizationRepository organizationRepository = mock(OrganizationRepository.class);
        RoleRepository roleRepository = mock(RoleRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        ProviderIamRemoteClient remoteClient = mock(ProviderIamRemoteClient.class);
        OrganizationRemoteMapper organizationMapper = mock(OrganizationRemoteMapper.class);
        UserRemoteMapper userMapper = mock(UserRemoteMapper.class);

        Organization organization = organization(20L, organizationUuid);
        Role role = Role.builder()
                .name("isemid_doctor")
                .active(true)
                .deleted(false)
                .build();
        role.setId(10L);

        User user = User.builder()
                .uuid(userUuid)
                .username("doctor")
                .active(true)
                .organizations(Set.of(organization))
                .build();
        user.setId(1L);

        RemoteOrganizationPayload remoteOrganization = new RemoteOrganizationPayload(
                organizationUuid,
                null,
                "Central Clinic",
                null,
                List.of(),
                true,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                null,
                "Organization"
        );
        RemotePractitionerPayload remotePractitioner = new RemotePractitionerPayload(
                userUuid,
                null,
                null,
                true,
                null,
                null,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                "Practitioner"
        );

        when(organizationRepository.findByUuid(organizationUuid)).thenReturn(Optional.empty());
        when(remoteClient.fetchOrganization(providerKey, organizationUuid, rawToken)).thenReturn(remoteOrganization);
        when(organizationMapper.toEntity(remoteOrganization)).thenReturn(organization);
        when(organizationRepository.saveAndFlush(organization)).thenReturn(organization);

        when(roleRepository.findByNormalizedName("isemid_doctor")).thenReturn(Optional.of(role));
        when(roleRepository.getReferenceById(10L)).thenReturn(role);

        when(userRepository.findByUuid(userUuid)).thenReturn(Optional.empty());
        when(remoteClient.fetchPractitioner(providerKey, userUuid, rawToken)).thenReturn(remotePractitioner);
        when(userMapper.toEntity(any(), any(), any())).thenReturn(user);
        when(userRepository.saveAndFlush(user)).thenReturn(user);

        RoleSyncProperties roleSyncProperties = new RoleSyncProperties();
        roleSyncProperties.setCreateMissingRoles(true);

        OrganizationSyncService organizationSyncService = new OrganizationSyncService(
                organizationRepository,
                remoteClient,
                organizationMapper
        );
        RoleSyncService roleSyncService = new RoleSyncService(
                roleRepository,
                roleSyncProperties,
                new ConcurrentMapCacheManager(SecurityCacheNames.ROLE_BY_NAME)
        );
        UserSyncService userSyncService = new UserSyncService(
                userRepository,
                remoteClient,
                userMapper,
                securityCacheManager()
        );
        IamSyncService iamSyncService = new IamSyncService(
                organizationSyncService,
                roleSyncService,
                userSyncService
        );

        IamSyncResult result = iamSyncService.synchronize(
                new ExternalIdentityPayload(
                        providerKey,
                        userUuid,
                        "doctor",
                        "123456789",
                        Set.of(organizationUuid),
                        Set.of("isemid_doctor")
                ),
                rawToken
        );

        assertThat(result.user().getId()).isEqualTo(1L);
        assertThat(result.userCreated()).isTrue();
        assertThat(result.organizations()).containsExactly(organization);

        verify(remoteClient).fetchOrganization(providerKey, organizationUuid, rawToken);
        verify(organizationRepository).saveAndFlush(organization);
        verify(roleRepository, never()).findWithPermissionsById(any());
        verify(remoteClient).fetchPractitioner(providerKey, userUuid, rawToken);
        verify(userRepository).saveAndFlush(user);
    }

    @Test
    void unchangedUserSyncDoesNotEvictSecurityCachesOrSaveAgain() {
        UUID userUuid = UUID.randomUUID();
        UUID organizationUuid = UUID.randomUUID();
        Organization organization = organization(20L, organizationUuid);
        Role role = role(10L, "isemid_doctor");
        User user = user(1L, userUuid, "doctor", "123456789", Set.of(organization), Set.of(role));
        ConcurrentMapCacheManager cacheManager = securityCacheManager();

        cacheManager.getCache(SecurityCacheNames.SECURITY_USER_BY_ID).put(1L, "security-user");
        cacheManager.getCache(SecurityCacheNames.USER_AUTHORITIES_BY_USER_ID).put(1L, "authorities");
        cacheManager.getCache(SecurityCacheNames.SELECTED_ORGANIZATION_BY_USER_ID_AND_UUID)
                .put("1:" + organizationUuid, "selected-org");

        UserRepository userRepository = mock(UserRepository.class);
        ProviderIamRemoteClient remoteClient = mock(ProviderIamRemoteClient.class);
        UserRemoteMapper userMapper = mock(UserRemoteMapper.class);

        when(userRepository.findByUuid(userUuid)).thenReturn(Optional.of(user));

        UserSyncService userSyncService = new UserSyncService(
                userRepository,
                remoteClient,
                userMapper,
                cacheManager
        );

        UserSyncResult result = userSyncService.resolve(
                payload(userUuid, "doctor", "123456789", Set.of(organizationUuid), Set.of("isemid_doctor")),
                "token",
                List.of(organization),
                Set.of(role)
        );

        assertThat(result.changed()).isFalse();
        assertThat(cacheManager.getCache(SecurityCacheNames.SECURITY_USER_BY_ID).get(1L)).isNotNull();
        assertThat(cacheManager.getCache(SecurityCacheNames.USER_AUTHORITIES_BY_USER_ID).get(1L)).isNotNull();
        assertThat(cacheManager.getCache(SecurityCacheNames.SELECTED_ORGANIZATION_BY_USER_ID_AND_UUID)
                .get("1:" + organizationUuid)).isNotNull();

        verify(userRepository, never()).save(any());
    }

    @Test
    void changedUserRolesEvictAuthoritiesCacheOnly() {
        UUID userUuid = UUID.randomUUID();
        UUID organizationUuid = UUID.randomUUID();
        Organization organization = organization(20L, organizationUuid);
        Role oldRole = role(10L, "isemid_old");
        Role newRole = role(11L, "isemid_new");
        User user = user(1L, userUuid, "doctor", "123456789", Set.of(organization), Set.of(oldRole));
        ConcurrentMapCacheManager cacheManager = securityCacheManager();

        cacheManager.getCache(SecurityCacheNames.SECURITY_USER_BY_ID).put(1L, "security-user");
        cacheManager.getCache(SecurityCacheNames.USER_AUTHORITIES_BY_USER_ID).put(1L, "authorities");
        cacheManager.getCache(SecurityCacheNames.SELECTED_ORGANIZATION_BY_USER_ID_AND_UUID)
                .put("1:" + organizationUuid, "selected-org");

        UserRepository userRepository = mock(UserRepository.class);
        ProviderIamRemoteClient remoteClient = mock(ProviderIamRemoteClient.class);
        UserRemoteMapper userMapper = mock(UserRemoteMapper.class);

        when(userRepository.findByUuid(userUuid)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        UserSyncService userSyncService = new UserSyncService(
                userRepository,
                remoteClient,
                userMapper,
                cacheManager
        );

        UserSyncResult result = userSyncService.resolve(
                payload(userUuid, "doctor", "123456789", Set.of(organizationUuid), Set.of("isemid_new")),
                "token",
                List.of(organization),
                Set.of(newRole)
        );

        assertThat(result.rolesChanged()).isTrue();
        assertThat(cacheManager.getCache(SecurityCacheNames.USER_AUTHORITIES_BY_USER_ID).get(1L)).isNull();
        assertThat(cacheManager.getCache(SecurityCacheNames.SECURITY_USER_BY_ID).get(1L)).isNotNull();
        assertThat(cacheManager.getCache(SecurityCacheNames.SELECTED_ORGANIZATION_BY_USER_ID_AND_UUID)
                .get("1:" + organizationUuid)).isNotNull();

        verify(userRepository, times(1)).save(user);
    }

    @Test
    void changedUserOrganizationsEvictSecurityUserAndSelectedOrganizationCaches() {
        UUID userUuid = UUID.randomUUID();
        UUID oldOrganizationUuid = UUID.randomUUID();
        UUID newOrganizationUuid = UUID.randomUUID();
        Organization oldOrganization = organization(20L, oldOrganizationUuid);
        Organization newOrganization = organization(21L, newOrganizationUuid);
        Role role = role(10L, "isemid_doctor");
        User user = user(1L, userUuid, "doctor", "123456789", Set.of(oldOrganization), Set.of(role));
        ConcurrentMapCacheManager cacheManager = securityCacheManager();

        cacheManager.getCache(SecurityCacheNames.SECURITY_USER_BY_ID).put(1L, "security-user");
        cacheManager.getCache(SecurityCacheNames.USER_AUTHORITIES_BY_USER_ID).put(1L, "authorities");
        cacheManager.getCache(SecurityCacheNames.SELECTED_ORGANIZATION_BY_USER_ID_AND_UUID)
                .put("1:" + oldOrganizationUuid, "old-selected-org");
        cacheManager.getCache(SecurityCacheNames.SELECTED_ORGANIZATION_BY_USER_ID_AND_UUID)
                .put("1:" + newOrganizationUuid, "new-selected-org");

        UserRepository userRepository = mock(UserRepository.class);
        ProviderIamRemoteClient remoteClient = mock(ProviderIamRemoteClient.class);
        UserRemoteMapper userMapper = mock(UserRemoteMapper.class);

        when(userRepository.findByUuid(userUuid)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        UserSyncService userSyncService = new UserSyncService(
                userRepository,
                remoteClient,
                userMapper,
                cacheManager
        );

        UserSyncResult result = userSyncService.resolve(
                payload(userUuid, "doctor", "123456789", Set.of(newOrganizationUuid), Set.of("isemid_doctor")),
                "token",
                List.of(newOrganization),
                Set.of(role)
        );

        assertThat(result.organizationsChanged()).isTrue();
        assertThat(cacheManager.getCache(SecurityCacheNames.SECURITY_USER_BY_ID).get(1L)).isNull();
        assertThat(cacheManager.getCache(SecurityCacheNames.USER_AUTHORITIES_BY_USER_ID).get(1L)).isNotNull();
        assertThat(cacheManager.getCache(SecurityCacheNames.SELECTED_ORGANIZATION_BY_USER_ID_AND_UUID)
                .get("1:" + oldOrganizationUuid)).isNull();
        assertThat(cacheManager.getCache(SecurityCacheNames.SELECTED_ORGANIZATION_BY_USER_ID_AND_UUID)
                .get("1:" + newOrganizationUuid)).isNull();

        verify(userRepository, times(1)).save(user);
    }

    @Test
    void roleSyncDoesNotReloadPermissionGraphWhenRoleSnapshotIsCached() {
        RoleRepository roleRepository = mock(RoleRepository.class);
        RoleSyncProperties roleSyncProperties = new RoleSyncProperties();
        Role role = role(10L, "isemid_doctor");

        when(roleRepository.findByNormalizedName("isemid_doctor")).thenReturn(Optional.of(role));
        when(roleRepository.getReferenceById(10L)).thenReturn(role);

        RoleSyncService roleSyncService = new RoleSyncService(
                roleRepository,
                roleSyncProperties,
                new ConcurrentMapCacheManager(SecurityCacheNames.ROLE_BY_NAME)
        );

        roleSyncService.resolve(Set.of("isemid_doctor"));
        roleSyncService.resolve(Set.of("isemid_doctor"));

        verify(roleRepository, times(1)).findByNormalizedName("isemid_doctor");
        verify(roleRepository, never()).findWithPermissionsById(any());
        verify(roleRepository, times(2)).getReferenceById(10L);
    }

    @Test
    void missingIsemidRoleCreatesInactivePlaceholderAndRejectsUser() {
        RoleRepository roleRepository = mock(RoleRepository.class);
        RoleSyncProperties roleSyncProperties = new RoleSyncProperties();
        roleSyncProperties.setCreateMissingRoles(true);

        Role placeholder = role(10L, "isemid_missing");
        placeholder.setActive(false);

        when(roleRepository.findByNormalizedName("isemid_missing")).thenReturn(Optional.empty());
        when(roleRepository.saveAndFlush(any(Role.class))).thenReturn(placeholder);

        RoleSyncService roleSyncService = new RoleSyncService(
                roleRepository,
                roleSyncProperties,
                new ConcurrentMapCacheManager(SecurityCacheNames.ROLE_BY_NAME)
        );

        assertThatThrownBy(() -> roleSyncService.resolve(Set.of("isemid_missing")))
                .isInstanceOf(AccessDeniedException.class);

        verify(roleRepository).saveAndFlush(any(Role.class));
        verify(roleRepository, never()).findWithPermissionsById(any());
    }

    private Organization organization(Long id, UUID uuid) {
        Organization organization = Organization.builder()
                .uuid(uuid)
                .name("Central Clinic")
                .active(true)
                .levelType(OrganizationLevel.REPUBLICAN)
                .medicalType(MedicalType.SANEPID_SERVICE)
                .regionCode("17")
                .districtCode("1701")
                .build();
        organization.setId(id);
        return organization;
    }

    private Role role(Long id, String name) {
        Role role = Role.builder()
                .name(name)
                .active(true)
                .deleted(false)
                .build();
        role.setId(id);
        return role;
    }

    private User user(
            Long id,
            UUID uuid,
            String username,
            String nnuzb,
            Set<Organization> organizations,
            Set<Role> roles
    ) {
        User user = User.builder()
                .uuid(uuid)
                .username(username)
                .nnuzb(nnuzb)
                .active(true)
                .organizations(organizations)
                .roles(roles)
                .build();
        user.setId(id);
        return user;
    }

    private ExternalIdentityPayload payload(
            UUID userUuid,
            String username,
            String nnuzb,
            Set<UUID> organizationUuids,
            Set<String> roleNames
    ) {
        return new ExternalIdentityPayload(
                "sso",
                userUuid,
                username,
                nnuzb,
                organizationUuids,
                roleNames
        );
    }

    private ConcurrentMapCacheManager securityCacheManager() {
        return new ConcurrentMapCacheManager(
                SecurityCacheNames.ROLE_BY_NAME,
                SecurityCacheNames.SECURITY_USER_BY_ID,
                SecurityCacheNames.USER_AUTHORITIES_BY_USER_ID,
                SecurityCacheNames.SELECTED_ORGANIZATION_BY_USER_ID_AND_UUID
        );
    }
}
