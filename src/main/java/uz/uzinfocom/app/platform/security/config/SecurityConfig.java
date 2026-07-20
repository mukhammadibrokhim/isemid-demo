package uz.uzinfocom.app.platform.security.config;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import uz.uzinfocom.app.platform.iam.application.sync.RoleSyncProperties;
import uz.uzinfocom.app.platform.security.filter.OrganizationContextFilter;
import uz.uzinfocom.app.platform.security.handler.JsonAccessDeniedHandler;
import uz.uzinfocom.app.platform.security.handler.JsonAuthenticationEntryPoint;
import uz.uzinfocom.app.platform.security.jwt.ProviderAuthenticationManagerRegistry;
import uz.uzinfocom.app.platform.security.properties.AuthProvidersProperties;
import uz.uzinfocom.app.platform.security.whitelist.SecurityRouteCatalog;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
@EnableConfigurationProperties({
        AuthProvidersProperties.class,
        RoleSyncProperties.class
})
public class SecurityConfig {

    private final ProviderAuthenticationManagerRegistry authenticationManagerRegistry;
    private final OrganizationContextFilter organizationContextFilter;
    private final JsonAuthenticationEntryPoint authenticationEntryPoint;
    private final JsonAccessDeniedHandler accessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        AuthenticationManagerResolver<HttpServletRequest> resolver = authenticationManagerRegistry.resolver();

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
                        .requestMatchers(SecurityRouteCatalog.OPEN_PATTERNS.toArray(String[]::new)).permitAll()
                        // /Loggers lets a caller view/change log levels at runtime — previously fell through to
                        // plain anyRequest().authenticated(), so ANY authenticated user (any role) could use it.
                        // Restricted to admins now that AdminAccessGuard exists as a single, reusable check.
                        .requestMatchers("/v1/actuator/loggers/**")
                        .access(new WebExpressionAuthorizationManager("@adminAccessGuard.isAdmin()"))
                        .anyRequest().authenticated()
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
