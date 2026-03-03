package uz.uzinfocom.isemid.commons.configs.security.constant;

public class SecurityConstants {
    public static final String[] WHITE_LIST = {
            "/swagger-ui/**",
            "/api-docs/**",
            "/actuator/**",
            "/api/actuator/**",
            "/v1/auth/**",
            "/v1/db/**",
            "/v1/legal-entity/**",
            "/v1/citizen/qr**",
            "/v1/catalog/country/**",
            "/v1/catalog/state/**",
            "/v1/catalog/city/**",
            "/v1/catalog/gender/**",
            "/v1/catalog/neighborhood/**",
            "/v1/catalog/positions/**",
            "/v1/catalog/levels/**",
            "/v1/report/monitoring/**"
    };
}
