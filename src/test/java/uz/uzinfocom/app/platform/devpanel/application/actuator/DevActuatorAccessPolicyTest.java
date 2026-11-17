package uz.uzinfocom.app.platform.devpanel.application.actuator;

import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.endpoint.web.WebEndpointHttpMethod;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DevActuatorAccessPolicyTest {

    private final DevActuatorAccessPolicy policy = new DevActuatorAccessPolicy();

    private static Authentication account(String... authorities) {
        return UsernamePasswordAuthenticationToken.authenticated("u", "n/a", AuthorityUtils.createAuthorityList(authorities));
    }

    private static final Authentication USER = account("ROLE_DEV_USER");
    private static final Authentication ADMIN = account("ROLE_DEV_USER", "ROLE_DEV_ADMIN");

    @Test
    void anyDevAccountMayReadPlainObservationEndpoints() {
        for (String id : new String[]{"health", "info", "metrics", "prometheus", "loggers", "scheduledtasks", "liquibase"}) {
            assertThatCode(() -> policy.assertAllowed(USER, id, WebEndpointHttpMethod.GET)).doesNotThrowAnyException();
            assertThat(policy.isSensitive(id)).isFalse();
        }
    }

    @Test
    void endpointsThatRevealInternalsNeedAdmin() {
        for (String id : new String[]{"env", "configprops", "beans", "conditions", "mappings", "threaddump"}) {
            assertThat(policy.isSensitive(id)).isTrue();
            assertThatThrownBy(() -> policy.assertAllowed(USER, id, WebEndpointHttpMethod.GET))
                    .isInstanceOf(AccessDeniedException.class);
            assertThatCode(() -> policy.assertAllowed(ADMIN, id, WebEndpointHttpMethod.GET)).doesNotThrowAnyException();
        }
    }

    @Test
    void everyWriteNeedsAdminEvenOnAnOtherwiseOpenEndpoint() {
        assertThatThrownBy(() -> policy.assertAllowed(USER, "loggers", WebEndpointHttpMethod.POST))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> policy.assertAllowed(USER, "metrics", WebEndpointHttpMethod.DELETE))
                .isInstanceOf(AccessDeniedException.class);
        assertThatCode(() -> policy.assertAllowed(ADMIN, "loggers", WebEndpointHttpMethod.POST)).doesNotThrowAnyException();
    }

    @Test
    void anonymousCallerIsAlwaysDenied() {
        assertThatThrownBy(() -> policy.assertAllowed(null, "health", WebEndpointHttpMethod.GET))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void securityContextMapsActuatorRoleNamesOntoDevPanelAuthorities() {
        DevActuatorSecurityContext superAdmin = DevActuatorSecurityContext.of(
                account("ROLE_DEV_USER", "ROLE_DEV_ADMIN", "ROLE_DEV_SUPER_ADMIN"));
        DevActuatorSecurityContext admin = DevActuatorSecurityContext.of(ADMIN);

        assertThat(superAdmin.isUserInRole("DEV_SUPER_ADMIN")).isTrue();
        assertThat(admin.isUserInRole("DEV_SUPER_ADMIN")).isFalse();
        assertThat(admin.isUserInRole("DEV_ADMIN")).isTrue();
        assertThat(admin.getPrincipal()).isNotNull();

        // The panel's own publishers see health details but never property values.
        DevActuatorSecurityContext system = DevActuatorSecurityContext.system();
        assertThat(system.isUserInRole("DEV_MONITORING")).isTrue();
        assertThat(system.isUserInRole("DEV_SUPER_ADMIN")).isFalse();
        assertThat(system.getPrincipal()).isNotNull();
    }
}
