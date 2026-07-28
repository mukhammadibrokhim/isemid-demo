package uz.uzinfocom.app.platform.devmonitoring.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import uz.uzinfocom.app.platform.devmonitoring.domain.DevUser;
import uz.uzinfocom.app.platform.devmonitoring.repository.DevUserRepository;

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
