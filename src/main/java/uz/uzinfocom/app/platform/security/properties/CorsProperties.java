package uz.uzinfocom.app.platform.security.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Which browser origins (SPA frontends) are allowed to call this API
 * cross-origin. Previously there was no {@code CorsConfigurationSource}
 * bean at all - {@code SecurityConfig}'s {@code .cors(Customizer
 * .withDefaults())} had nothing to consult, so every cross-origin
 * browser request (including the login-proxy endpoints in {@code
 * platform.auth}, meant to be called directly from a frontend) was
 * silently rejected by the browser itself, before this app ever saw the
 * request.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app.cors")
public class CorsProperties {

    private List<String> allowedOrigins = new ArrayList<>();

    private List<String> allowedMethods = List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");

    private List<String> allowedHeaders = List.of("*");

    private List<String> exposedHeaders = new ArrayList<>();

    /**
     * Left false: this API authenticates via {@code Authorization: Bearer},
     * never cookies, so the frontend never needs {@code credentials:
     * "include"} - and the CORS spec forbids combining {@code
     * allowCredentials=true} with a wildcard origin anyway.
     */
    private boolean allowCredentials;

    private long maxAgeSeconds = 3600;
}
