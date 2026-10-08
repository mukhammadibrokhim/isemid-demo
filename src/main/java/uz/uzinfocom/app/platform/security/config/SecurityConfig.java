package uz.uzinfocom.app.platform.security.config;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.DefaultHttpSecurityExpressionHandler;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.modules.iam.application.sync.RoleSyncProperties;
import uz.uzinfocom.app.platform.security.filter.IntegrationApiKeyAuthenticationFilter;
import uz.uzinfocom.app.platform.security.filter.IntegrationBasicAuthenticationFilter;
import uz.uzinfocom.app.platform.security.filter.IntegrationIpAllowlistAuthenticationFilter;
import uz.uzinfocom.app.platform.security.filter.OrganizationContextFilter;
import uz.uzinfocom.app.platform.security.filter.PrincipalCaptureFilter;
import uz.uzinfocom.app.platform.security.handler.JsonAccessDeniedHandler;
import uz.uzinfocom.app.platform.security.handler.JsonAuthenticationEntryPoint;
import uz.uzinfocom.app.platform.security.ip.IpAllowlistMatcher;
import uz.uzinfocom.app.platform.security.jwt.ProviderAuthenticationManagerRegistry;
import uz.uzinfocom.app.platform.security.jwt.properties.IntegrationTokenProperties;
import uz.uzinfocom.app.platform.security.properties.ActuatorAccessProperties;
import uz.uzinfocom.app.platform.security.properties.AuthProvidersProperties;
import uz.uzinfocom.app.platform.security.ratelimit.RateLimitFilter;
import uz.uzinfocom.app.platform.security.ratelimit.RateLimitProperties;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
@EnableConfigurationProperties({
        AuthProvidersProperties.class,
        RoleSyncProperties.class,
        IntegrationTokenProperties.class,
        ActuatorAccessProperties.class,
        RateLimitProperties.class
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
    private final RateLimitFilter rateLimitFilter;
    private final ActuatorAccessProperties actuatorAccessProperties;
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
                // Stateless bearer-token API: no cookies, so CSRF has nothing to protect.
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                // Spring Security's defaults (nosniff, X-Frame-Options DENY, no-store
                // cache headers, HSTS over HTTPS) stay on; a JSON API never needs to be
                // framed or to leak its URL as a referrer.
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp.policyDirectives("frame-ancestors 'none'"))
                        .referrerPolicy(referrer -> referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER))
                )
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
                        // Metrics expose internal endpoint names, pool sizes, error rates -
                        // admin-only like loggers. Prometheus can't send an SSO token, so its
                        // scrape endpoint is restricted by source network instead. Both rules
                        // run ahead of the DB-backed route policy, which can't loosen them.
                        .requestMatchers("/v1/actuator/metrics/**")
                        .access(loggersAuthorizationManager)
                        .requestMatchers("/v1/actuator/prometheus")
                        .access(prometheusAuthorizationManager())
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
                .addFilterBefore(rateLimitFilter, AuthorizationFilter.class)
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

    /**
     * Fails closed on an empty allow-list - {@link IpAllowlistMatcher#isAllowed}
     * treats an empty list as "no restriction", which is right for integration
     * clients but would make the scrape endpoint public here.
     */
    private AuthorizationManager<RequestAuthorizationContext> prometheusAuthorizationManager() {
        return (authentication, context) -> {
            String allowedIps = actuatorAccessProperties.getPrometheusAllowedIps();
            boolean allowed = StringUtils.hasText(allowedIps)
                    && IpAllowlistMatcher.isAllowed(context.getRequest().getRemoteAddr(), allowedIps);
            return new AuthorizationDecision(allowed);
        };
    }
}
