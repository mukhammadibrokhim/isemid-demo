package uz.uzinfocom.app.platform.devpanel.application.actuator;

import org.springframework.boot.actuate.endpoint.web.WebEndpointHttpMethod;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Who may call which actuator operation through the dev panel. Three tiers on top of
 * "authenticated dev account":
 *
 * <ul>
 *     <li>{@code ROLE_DEV_USER} - plain observation: health, info, metrics, prometheus,
 *     loggers (read), scheduled tasks, liquibase.</li>
 *     <li>{@code ROLE_DEV_ADMIN} - anything that reveals internals (env, configprops, beans,
 *     conditions, mappings, thread dumps) and anything that changes state (POST/DELETE,
 *     e.g. a logger level).</li>
 * </ul>
 *
 * Property <em>values</em> in env/configprops are a third gate owned by actuator itself:
 * {@code management.endpoint.env.roles} / {@code ...configprops.roles} = {@code DEV_SUPER_ADMIN}
 * (see {@link DevActuatorSecurityContext}); everyone else sees them masked.
 */
@Component
public class DevActuatorAccessPolicy {

    public static final String ROLE_USER = "ROLE_DEV_USER";
    public static final String ROLE_ADMIN = "ROLE_DEV_ADMIN";

    private static final Set<String> INTERNALS = Set.of(
            "env", "configprops", "beans", "conditions", "mappings", "threaddump"
    );

    public String requiredAuthority(String endpointId, WebEndpointHttpMethod method) {
        if (method != WebEndpointHttpMethod.GET || INTERNALS.contains(endpointId)) {
            return ROLE_ADMIN;
        }
        return ROLE_USER;
    }

    public boolean isSensitive(String endpointId) {
        return INTERNALS.contains(endpointId);
    }

    public void assertAllowed(Authentication authentication, String endpointId, WebEndpointHttpMethod method) {
        String required = requiredAuthority(endpointId, method);
        boolean granted = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> required.equals(authority.getAuthority()));
        if (!granted) {
            throw new AccessDeniedException("Actuator " + method + " /" + endpointId + " requires " + required);
        }
    }
}
