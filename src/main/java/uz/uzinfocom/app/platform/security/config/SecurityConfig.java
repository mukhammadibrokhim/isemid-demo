package uz.uzinfocom.app.platform.security.config;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.DefaultHttpSecurityExpressionHandler;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import uz.uzinfocom.app.platform.iam.application.sync.RoleSyncProperties;
import uz.uzinfocom.app.platform.security.filter.OrganizationContextFilter;
import uz.uzinfocom.app.platform.security.handler.JsonAccessDeniedHandler;
import uz.uzinfocom.app.platform.security.handler.JsonAuthenticationEntryPoint;
import uz.uzinfocom.app.platform.security.jwt.ProviderAuthenticationManagerRegistry;
import uz.uzinfocom.app.platform.security.jwt.properties.IntegrationTokenProperties;
import uz.uzinfocom.app.platform.security.properties.AuthProvidersProperties;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
@EnableConfigurationProperties({
        AuthProvidersProperties.class,
        RoleSyncProperties.class,
        IntegrationTokenProperties.class
})
public class SecurityConfig {

    private final ProviderAuthenticationManagerRegistry authenticationManagerRegistry;
    private final OrganizationContextFilter organizationContextFilter;
    private final JsonAuthenticationEntryPoint authenticationEntryPoint;
    private final JsonAccessDeniedHandler accessDeniedHandler;
    private final DynamicRouteAuthorizationManager dynamicRouteAuthorizationManager;
    private final ApplicationContext applicationContext;

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

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        AuthenticationManagerResolver<HttpServletRequest> resolver = authenticationManagerRegistry.resolver();

        // A WebExpressionAuthorizationManager built via `new` (rather than as a
        // Spring-managed bean) never receives an ApplicationContext, so a SpEL bean
        // reference like "@adminAccessGuard" has no BeanResolver and fails at
        // evaluation time with "Failed to evaluate expression" - wiring the
        // ApplicationContext into the expression handler explicitly fixes this.
        DefaultHttpSecurityExpressionHandler loggersExpressionHandler = new DefaultHttpSecurityExpressionHandler();
        loggersExpressionHandler.setApplicationContext(applicationContext);
        WebExpressionAuthorizationManager loggersAuthorizationManager =
                new WebExpressionAuthorizationManager("@adminAccessGuard.isAdmin()");
        loggersAuthorizationManager.setExpressionHandler(loggersExpressionHandler);

        http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .authorizeHttpRequests(authorize -> authorize
                        .dispatcherTypeMatchers(
                                DispatcherType.ERROR, DispatcherType.FORWARD
                        ).permitAll()
                        // /Loggers lets a caller view/change log levels at runtime — previously fell through to
                        // plain anyRequest().authenticated(), so ANY authenticated user (any role) could use it.
                        // Restricted to admins now that AdminAccessGuard exists as a single, reusable check.
                        .requestMatchers("/v1/actuator/loggers/**")
                        .access(loggersAuthorizationManager)
                        // Which paths are public vs. authenticated is DB-backed and re-resolved on
                        // every request - see DynamicRouteAuthorizationManager and RouteAccessPolicy.
                        .anyRequest().access(dynamicRouteAuthorizationManager)
                )
                .oauth2ResourceServer(resourceServer -> resourceServer
                        .authenticationManagerResolver(resolver)
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .addFilterBefore(organizationContextFilter, AuthorizationFilter.class);

        return http.build();
    }
}
