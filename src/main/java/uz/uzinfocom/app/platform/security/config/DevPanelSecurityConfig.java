package uz.uzinfocom.app.platform.security.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import uz.uzinfocom.app.platform.devmonitoring.security.DevUserDetailsService;
import uz.uzinfocom.app.platform.security.filter.PrincipalCaptureFilter;
import uz.uzinfocom.app.platform.security.handler.JsonAccessDeniedHandler;
import uz.uzinfocom.app.platform.security.handler.JsonAuthenticationEntryPoint;

/**
 * A second, independent {@code SecurityFilterChain} scoped only to
 * {@code /v1/dev/**} - the developer monitoring panel. Deliberately kept
 * separate from {@link SecurityConfig}: every other endpoint in this app
 * authenticates via an external SSO/DHP bearer JWT, but this panel is a
 * purely internal ops tool with its own local {@code DevUser} accounts
 * (HTTP Basic, no session/CSRF needed for a machine-consumed monitoring API).
 *
 * <p>Ordered ahead of the main chain ({@code @Order(1)} vs. the main chain's
 * implicit {@code LOWEST_PRECEDENCE}) so Spring Security picks this chain
 * first for any {@code /v1/dev/**} request and never falls through to the
 * SSO/DHP resource-server chain for this path.
 */
@Configuration
@RequiredArgsConstructor
public class DevPanelSecurityConfig {

    private final DevUserDetailsService devUserDetailsService;
    private final JsonAuthenticationEntryPoint authenticationEntryPoint;
    private final JsonAccessDeniedHandler accessDeniedHandler;
    private final PrincipalCaptureFilter principalCaptureFilter;

    /**
     * Kept separate from {@code SecurityConfig.integrationClientPasswordEncoder} -
     * human dev-panel account hashing and machine integration-client secret
     * hashing are unrelated concerns that shouldn't be coupled just because
     * both happen to use BCrypt today.
     */
    @Bean
    public PasswordEncoder devUserPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider devUserAuthenticationProvider(PasswordEncoder devUserPasswordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(devUserDetailsService);
        provider.setPasswordEncoder(devUserPasswordEncoder);
        return provider;
    }

    @Bean
    @Order(1)
    public SecurityFilterChain devPanelSecurityFilterChain(
            HttpSecurity http,
            DaoAuthenticationProvider devUserAuthenticationProvider
    ) {
        http
                .securityMatcher("/v1/dev/**")
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(basic -> basic.authenticationEntryPoint(authenticationEntryPoint))
                .authenticationProvider(devUserAuthenticationProvider)
                .exceptionHandling(exceptions -> exceptions.accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
                .addFilterBefore(principalCaptureFilter, AuthorizationFilter.class);

        return http.build();
    }
}
