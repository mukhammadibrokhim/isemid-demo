package uz.uzinfocom.app.platform.security.auth;

import org.junit.jupiter.api.Test;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.security.core.GrantedAuthority;
import uz.uzinfocom.app.platform.cache.SecurityCacheNames;
import uz.uzinfocom.app.platform.iam.domain.Role;
import uz.uzinfocom.app.platform.iam.domain.User;
import uz.uzinfocom.app.platform.iam.repository.OrganizationRepository;
import uz.uzinfocom.app.platform.iam.repository.RoleRepository;
import uz.uzinfocom.app.platform.iam.repository.UserRepository;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SecurityAuthorityServiceTest {

    private final RoleRepository roleRepository = mock(RoleRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final OrganizationRepository organizationRepository = mock(OrganizationRepository.class);

    private final ConcurrentMapCacheManager securityCacheManager = new ConcurrentMapCacheManager(
            SecurityCacheNames.ROLE_PERMISSIONS_BY_ROLE_IDS,
            SecurityCacheNames.USER_AUTHORITIES_BY_USER_ID,
            SecurityCacheNames.USER_ORGANIZATION_IDS_BY_USER_ID,
            SecurityCacheNames.ORGANIZATION_BY_UUID
    );

    private final UserOrganizationSecurityCacheService userOrganizationSecurityCacheService =
            new UserOrganizationSecurityCacheService(userRepository);

    private final SecurityAuthorityService service = new SecurityAuthorityService(
            roleRepository,
            userRepository,
            organizationRepository,
            securityCacheManager,
            userOrganizationSecurityCacheService
    );

    @Test
    void loadsAuthoritiesFromGlobalUserRolesOnly() {
        Role role = availableRole();

        User user = User.builder()
                .roles(Set.of(role))
                .build();
        user.setId(1L);

        when(userRepository.findForAuthorizationById(1L))
                .thenReturn(Optional.of(user));

        when(roleRepository.findPermissionAuthorityNamesByRoleIds(Set.of(10L)))
                .thenReturn(Set.of("PERMISSION_PATIENT_READ"));

        Collection<? extends GrantedAuthority> authorities = service.loadAuthoritiesByUserId(1L);

        assertThat(authorityNames(authorities))
                .containsExactlyInAnyOrder(
                        "ROLE_ISEMID_ADMIN",
                        "PERMISSION_PATIENT_READ"
                );
    }

    @Test
    void cachesRolePermissionAuthoritiesByRoleIds() {
        Role role = availableRole();

        when(roleRepository.findPermissionAuthorityNamesByRoleIds(Set.of(10L)))
                .thenReturn(Set.of("PERMISSION_PATIENT_READ"));

        service.loadAuthoritiesByRoles(Set.of(role));
        service.loadAuthoritiesByRoles(Set.of(role));

        verify(roleRepository, times(1))
                .findPermissionAuthorityNamesByRoleIds(Set.of(10L));
    }

    @Test
    void validatesOrganizationMembershipByUsersOrganizationsIds() {
        when(userRepository.findOrganizationIdsByUserId(1L))
                .thenReturn(Set.of(20L, 30L));

        assertThat(service.userBelongsToOrganization(1L, 20L)).isTrue();
        assertThat(service.userBelongsToOrganization(1L, 99L)).isFalse();
    }

    @Test
    void returnsFalseWhenUserIdOrOrganizationIdIsNull() {
        assertThat(service.userBelongsToOrganization(null, 20L)).isFalse();
        assertThat(service.userBelongsToOrganization(1L, null)).isFalse();
        assertThat(service.userBelongsToOrganization(null, null)).isFalse();
    }

    private Role availableRole() {
        Role role = Role.builder()
                .name("isemid_admin")
                .active(true)
                .deleted(false)
                .build();

        role.setId(10L);
        return role;
    }

    private Set<String> authorityNames(Collection<? extends GrantedAuthority> authorities) {
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
    }
}