package uz.uzinfocom.app.platform.security.auth;

import org.springframework.security.core.Authentication;
import uz.uzinfocom.app.platform.security.principal.IntegrationClientPrincipal;

/**
 * Marks an {@link Authentication} as belonging to a registered
 * {@code IntegrationClient}, regardless of which credential it authenticated
 * with (client-credentials JWT, API key, Basic Auth, or IP allow-list).
 * {@link InboundCallerContext} and {@code OrganizationContextFilter} key off
 * this instead of a specific token class so every credential kind gets the
 * same scope/source-key/org/IP handling for free.
 */
public interface IntegrationClientAuthentication extends Authentication {

    IntegrationClientPrincipal getPrincipal();
}
