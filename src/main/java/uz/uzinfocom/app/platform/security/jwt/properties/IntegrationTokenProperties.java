package uz.uzinfocom.app.platform.security.jwt.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for the self-issued JWTs used by the inbound integration API
 * (external systems, not human SSO/DHP users). {@code issuer} is the single
 * source of truth for the fixed, code-defined internal issuer string — both
 * {@code IntegrationTokenIssuer} (stamps it on every token) and
 * {@code ProviderAuthenticationManagerRegistry} (routes by it) read from this
 * same bean, so the two can never drift out of sync.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app.integration.token")
public class IntegrationTokenProperties {

    private String issuer = "isemid-integration";

    private long ttlSeconds = 900;
}
