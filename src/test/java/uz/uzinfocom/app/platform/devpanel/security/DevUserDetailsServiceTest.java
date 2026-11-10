package uz.uzinfocom.app.platform.devpanel.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import uz.uzinfocom.app.platform.devpanel.domain.DevUser;
import uz.uzinfocom.app.platform.devpanel.domain.DevUserRole;
import uz.uzinfocom.app.platform.devpanel.repository.DevUserRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DevUserDetailsServiceTest {

    private final DevUserRepository devUserRepository = mock(DevUserRepository.class);
    private final DevUserDetailsService service = new DevUserDetailsService(devUserRepository);

    @Test
    void loadsAnEnabledAccountAsAnEnabledUserDetails() {
        DevUser devUser = DevUser.builder()
                .username("dev-oncall")
                .passwordHash("hash")
                .enabled(true)
                .build();
        devUser.setId(7L);
        when(devUserRepository.findByUsername("dev-oncall")).thenReturn(Optional.of(devUser));

        UserDetails userDetails = service.loadUserByUsername("dev-oncall");

        assertThat(userDetails.getUsername()).isEqualTo("dev-oncall");
        assertThat(userDetails.getPassword()).isEqualTo("hash");
        assertThat(userDetails.isEnabled()).isTrue();
        assertThat(((DevUserPrincipal) userDetails).getDevUserId()).isEqualTo(7L);
        assertThat(userDetails.getAuthorities())
                .extracting(Object::toString)
                .containsExactlyInAnyOrder("ROLE_DEV_MONITORING", "ROLE_DEV_USER");
    }

    @Test
    void grantsRoleDevAdminToAnAdminAccount() {
        DevUser devUser = DevUser.builder()
                .username("dev-admin-user")
                .passwordHash("hash")
                .enabled(true)
                .role(DevUserRole.ADMIN)
                .build();
        when(devUserRepository.findByUsername("dev-admin-user")).thenReturn(Optional.of(devUser));

        UserDetails userDetails = service.loadUserByUsername("dev-admin-user");

        assertThat(userDetails.getAuthorities())
                .extracting(Object::toString)
                .containsExactlyInAnyOrder("ROLE_DEV_MONITORING", "ROLE_DEV_USER", "ROLE_DEV_ADMIN");
    }

    @Test
    void grantsRoleDevSuperAdminOnlyToASuperAdminAccount() {
        DevUser devUser = DevUser.builder()
                .username("dev-root")
                .passwordHash("hash")
                .enabled(true)
                .role(DevUserRole.SUPER_ADMIN)
                .build();
        when(devUserRepository.findByUsername("dev-root")).thenReturn(Optional.of(devUser));

        UserDetails userDetails = service.loadUserByUsername("dev-root");

        assertThat(userDetails.getAuthorities())
                .extracting(Object::toString)
                .containsExactlyInAnyOrder(
                        "ROLE_DEV_MONITORING", "ROLE_DEV_USER", "ROLE_DEV_ADMIN", "ROLE_DEV_SUPER_ADMIN"
                );
    }

    @Test
    void loadsARevokedAccountAsADisabledUserDetailsSoSpringSecurityRejectsIt() {
        DevUser devUser = DevUser.builder()
                .username("dev-oncall")
                .passwordHash("hash")
                .enabled(false)
                .build();
        when(devUserRepository.findByUsername("dev-oncall")).thenReturn(Optional.of(devUser));

        UserDetails userDetails = service.loadUserByUsername("dev-oncall");

        assertThat(userDetails.isEnabled()).isFalse();
    }

    @Test
    void rejectsAnUnknownUsername() {
        when(devUserRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("ghost"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
