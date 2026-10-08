package uz.uzinfocom.app.platform.devpanel.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.platform.devpanel.domain.DevUser;
import uz.uzinfocom.app.platform.devpanel.repository.DevUserRepository;

/**
 * Neutralizes the {@code dev-admin} account seeded by
 * {@code 20260729-1200-seed-dev-admin-user.xml} while it still has its
 * publicly known password. That migration runs in every environment, and the
 * forced password change on first login doesn't help: whoever logs in first -
 * possibly an attacker - picks the new password and owns the dev panel.
 *
 * <p>On startup, if {@code dev-admin} still accepts the seed password:
 * <ul>
 *   <li>{@code app.dev.admin-bootstrap.password} set → the password is
 *       replaced by that value (still flagged must-change-password);</li>
 *   <li>else {@code app.dev.admin-bootstrap.allow-default-password=true}
 *       (local machines only) → left as is, with a warning;</li>
 *   <li>else → the account is disabled. Re-enable it from another root
 *       account or restart with a bootstrap password.</li>
 * </ul>
 * An account whose password was already changed is never touched.
 */
@Slf4j
@Component
public class DevAdminBootstrapGuard implements ApplicationRunner {

    static final String SEEDED_USERNAME = "dev-admin";
    // Already public in the seed changelog; kept only to recognise the untouched account.
    static final String SEEDED_PASSWORD = "DevPanel-2026!";

    private final DevUserRepository devUserRepository;
    private final PasswordEncoder devUserPasswordEncoder;
    private final String bootstrapPassword;
    private final boolean allowDefaultPassword;

    public DevAdminBootstrapGuard(
            DevUserRepository devUserRepository,
            @Qualifier("devUserPasswordEncoder") PasswordEncoder devUserPasswordEncoder,
            @Value("${app.dev.admin-bootstrap.password:}") String bootstrapPassword,
            @Value("${app.dev.admin-bootstrap.allow-default-password:false}") boolean allowDefaultPassword
    ) {
        this.devUserRepository = devUserRepository;
        this.devUserPasswordEncoder = devUserPasswordEncoder;
        this.bootstrapPassword = bootstrapPassword;
        this.allowDefaultPassword = allowDefaultPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        devUserRepository.findByUsername(SEEDED_USERNAME)
                .filter(devUser -> Boolean.TRUE.equals(devUser.getEnabled()))
                .filter(devUser -> devUserPasswordEncoder.matches(SEEDED_PASSWORD, devUser.getPasswordHash()))
                .ifPresent(this::neutralize);
    }

    private void neutralize(DevUser devUser) {
        if (StringUtils.hasText(bootstrapPassword)) {
            if (bootstrapPassword.length() < 12 || SEEDED_PASSWORD.equals(bootstrapPassword)) {
                throw new IllegalStateException(
                        "app.dev.admin-bootstrap.password must be at least 12 characters and differ from the seed password");
            }
            devUser.setPasswordHash(devUserPasswordEncoder.encode(bootstrapPassword));
            devUser.setMustChangePassword(true);
            devUserRepository.save(devUser);
            log.warn("Dev-panel account '{}' still had the seed password - replaced with the configured bootstrap password.",
                    SEEDED_USERNAME);
            return;
        }

        if (allowDefaultPassword) {
            log.warn("Dev-panel account '{}' still uses the publicly known seed password "
                    + "(allowed by app.dev.admin-bootstrap.allow-default-password). Never do this on a reachable server.",
                    SEEDED_USERNAME);
            return;
        }

        devUser.setEnabled(false);
        devUserRepository.save(devUser);
        log.warn("Dev-panel account '{}' still had the publicly known seed password and was DISABLED. "
                + "Set DEV_PANEL_ADMIN_BOOTSTRAP_PASSWORD and restart to use it.", SEEDED_USERNAME);
    }
}
