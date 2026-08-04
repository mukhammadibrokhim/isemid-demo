package uz.uzinfocom.app.platform.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Split out of {@link SecurityConfig} so that beans depending on {@link
 * PasswordEncoder} - like the integration-client credential filters, which
 * {@code SecurityConfig} itself injects as constructor fields to wire into
 * its filter chain - don't create a circular dependency back onto {@code
 * SecurityConfig} just to reach its {@code @Bean} method.
 */
@Configuration
public class PasswordEncoderConfig {

    /**
     * Hashes integration-client secrets (see {@code IntegrationClientCommandService}/
     * {@code IntegrationTokenService}). {@code @Primary} because it's the encoder every
     * pre-existing unqualified {@code PasswordEncoder} injection point in this codebase
     * was written against - the dev-panel's separate {@code devUserPasswordEncoder}
     * (see {@code DevPanelSecurityConfig}) is newer and always injected by explicit
     * {@code @Qualifier}, so it must not become the ambiguous default.
     */
    @Primary
    @Bean
    public PasswordEncoder integrationClientPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
