package uz.uzinfocom.app.platform.iam.application.sync;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.iam.role-sync")
public class RoleSyncProperties {

    /**
     * Secure default: do not auto-create active authorization roles from external tokens.
     * When enabled, missing roles are created as inactive placeholders with no permissions.
     */
    private boolean createMissingRoles = false;
}
