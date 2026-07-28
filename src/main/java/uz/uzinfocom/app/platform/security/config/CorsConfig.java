package uz.uzinfocom.app.platform.security.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import uz.uzinfocom.app.platform.security.properties.CorsProperties;

import java.time.Duration;

/**
 * Without this bean, {@code .cors(Customizer.withDefaults())} in
 * {@link SecurityConfig} has nothing to consult - Spring Security adds no
 * CORS handling at all, and every cross-origin browser request (a frontend
 * calling the login-proxy endpoints, or any other API endpoint) is rejected
 * by the browser itself before this app ever sees it. Allowed origins are
 * config-driven ({@code app.cors.allowed-origins}) so each environment
 * lists only the frontend origins it actually serves.
 */
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(CorsProperties.class)
public class CorsConfig {

    private final CorsProperties corsProperties;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(corsProperties.getAllowedOrigins());
        configuration.setAllowedMethods(corsProperties.getAllowedMethods());
        configuration.setAllowedHeaders(corsProperties.getAllowedHeaders());
        configuration.setExposedHeaders(corsProperties.getExposedHeaders());
        configuration.setAllowCredentials(corsProperties.isAllowCredentials());
        configuration.setMaxAge(Duration.ofSeconds(corsProperties.getMaxAgeSeconds()));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
