package uz.uzinfocom.app.platform.devpanel.security;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.platform.devpanel.domain.DevUser;
import uz.uzinfocom.app.platform.devpanel.repository.DevUserRepository;

@Service
@RequiredArgsConstructor
public class DevUserDetailsService implements UserDetailsService {

    private final DevUserRepository devUserRepository;

    @Override
    @Transactional(readOnly = true)
    public @NonNull UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        DevUser devUser = devUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Unknown dev-panel account: " + username));

        return new DevUserPrincipal(devUser);
    }
}
