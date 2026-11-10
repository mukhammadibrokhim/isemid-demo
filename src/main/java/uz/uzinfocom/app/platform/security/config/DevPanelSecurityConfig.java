package uz.uzinfocom.app.platform.security.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.platform.devpanel.security.DevAuthProperties;
import uz.uzinfocom.app.platform.devpanel.security.DevPasswordChangeGuardFilter;
import uz.uzinfocom.app.platform.devpanel.security.DevTokenAuthenticationFilter;
import uz.uzinfocom.app.platform.devpanel.security.DevTwoFactorGuardFilter;
import uz.uzinfocom.app.platform.devpanel.security.DevUserDetailsService;
import uz.uzinfocom.app.platform.security.filter.PrincipalCaptureFilter;
import uz.uzinfocom.app.platform.security.handler.JsonAccessDeniedHandler;
import uz.uzinfocom.app.platform.security.handler.JsonAuthenticationEntryPoint;

/**
 * A second, independent {@code SecurityFilterChain} scoped only to
 * {@code /v1/dev/**} - the developer monitoring panel. Deliberately kept
 * separate from {@link SecurityConfig}: every other endpoint in this app
 * authenticates via an external SSO/DHP bearer JWT, but this panel is a
 * purely internal ops tool with its own local {@code DevUser} accounts.
 * Login is password + TOTP authenticator code at {@code POST /v1/dev/auth/login},
 * which returns a signed Bearer token (stateless: no session, no CSRF).
 *
 * <p>Ordered ahead of the main chain ({@code @Order(1)} vs. the main chain's
 * implicit {@code LOWEST_PRECEDENCE}) so Spring Security picks this chain
 * first for any {@code /v1/dev/**} request and never falls through to the
 * SSO/DHP resource-server chain for this path.
 */
@Configuration
@EnableConfigurationProperties(DevAuthProperties.class)
@RequiredArgsConstructor
public class DevPanelSecurityConfig {

    private final DevUserDetailsService devUserDetailsService;
    private final JsonAuthenticationEntryPoint authenticationEntryPoint;
    private final JsonAccessDeniedHandler accessDeniedHandler;
    private final PrincipalCaptureFilter principalCaptureFilter;
    private final DevTokenAuthenticationFilter devTokenAuthenticationFilter;
    private final DevPasswordChangeGuardFilter devPasswordChangeGuardFilter;
    private final DevTwoFactorGuardFilter devTwoFactorGuardFilter;

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
    public SecurityFilterChain devPanelSecurityFilterChain(HttpSecurity http) {
        http
                .securityMatcher("/v1/dev/**")
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // No HTTP Basic: a password alone must never open the panel. Callers log in at
                // POST /v1/dev/auth/login (password + authenticator code) and send the signed
                // token as 'Authorization: Bearer'.
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.POST, ApiPaths.Dev.ROOT + ApiPaths.Dev.AUTH_LOGIN).permitAll()
                        .anyRequest().authenticated())
                // Same slot the old Basic filter had: before AnonymousAuthenticationFilter, so the
                // context is still empty when the token is resolved.
                .addFilterBefore(devTokenAuthenticationFilter, BasicAuthenticationFilter.class)
                .addFilterBefore(principalCaptureFilter, AuthorizationFilter.class)
                .addFilterAfter(devPasswordChangeGuardFilter, AuthorizationFilter.class)
                .addFilterAfter(devTwoFactorGuardFilter, DevPasswordChangeGuardFilter.class);

        return http.build();
    }
}
