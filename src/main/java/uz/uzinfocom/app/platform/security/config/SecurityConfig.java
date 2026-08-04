package uz.uzinfocom.app.platform.security.config;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.DefaultHttpSecurityExpressionHandler;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import uz.uzinfocom.app.platform.iam.application.sync.RoleSyncProperties;
import uz.uzinfocom.app.platform.security.filter.IntegrationApiKeyAuthenticationFilter;
import uz.uzinfocom.app.platform.security.filter.IntegrationBasicAuthenticationFilter;
import uz.uzinfocom.app.platform.security.filter.IntegrationIpAllowlistAuthenticationFilter;
import uz.uzinfocom.app.platform.security.filter.OrganizationContextFilter;
import uz.uzinfocom.app.platform.security.filter.PrincipalCaptureFilter;
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
    private final PrincipalCaptureFilter principalCaptureFilter;
    private final IntegrationApiKeyAuthenticationFilter integrationApiKeyAuthenticationFilter;
    private final IntegrationBasicAuthenticationFilter integrationBasicAuthenticationFilter;
    private final IntegrationIpAllowlistAuthenticationFilter integrationIpAllowlistAuthenticationFilter;
    private final JsonAuthenticationEntryPoint authenticationEntryPoint;
    private final JsonAccessDeniedHandler accessDeniedHandler;
    private final DynamicRouteAuthorizationManager dynamicRouteAuthorizationManager;
    private final ApplicationContext applicationContext;

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
        WebExpressionAuthorizationManager loggersAuthorizationManager = WebExpressionAuthorizationManager
                .withExpressionHandler(loggersExpressionHandler)
                .expression("@adminAccessGuard.isAdmin()");

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
                .addFilterBefore(organizationContextFilter, AuthorizationFilter.class)
                .addFilterBefore(principalCaptureFilter, AuthorizationFilter.class)
                // The three integration-credential filters run before organizationContextFilter,
                // which reads SecurityContextHolder's authentication and requires it to already
                // be set - exactly like the Bearer/JWT path the oauth2ResourceServer DSL wires up.
                // OrganizationContextFilter.class must already be registered (line above) before
                // it can be used as a reference point here.
                .addFilterBefore(integrationApiKeyAuthenticationFilter, OrganizationContextFilter.class)
                .addFilterBefore(integrationBasicAuthenticationFilter, OrganizationContextFilter.class)
                .addFilterBefore(integrationIpAllowlistAuthenticationFilter, OrganizationContextFilter.class);

        return http.build();
    }
}
