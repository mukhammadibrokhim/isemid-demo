package uz.uzinfocom.app.platform.security.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.security.authorization")
public class AuthorizationProperties {

    /**
     * Roles that bypass fine-grained permission checks.
     */
    private List<String> permissionBypassRoles = new ArrayList<>(List.of("isemid_super_admin"));
}
