package uz.uzinfocom.app.modules.iam.application.permission;

import org.junit.jupiter.api.Test;
import uz.uzinfocom.app.platform.security.auth.Permissions;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PermissionCatalogValidatorTest {

    @Test
    void nothingMissingWhenAllRequiredAreGrantable() {
        assertThat(PermissionCatalogValidator.findMissing(new HashSet<>(Permissions.required()))).isEmpty();
    }

    @Test
    void reportsAuthoritiesThatCannotBeGranted() {
        Set<String> grantable = new HashSet<>(Permissions.required());
        grantable.remove(Permissions.REPORTS_READ);

        assertThat(PermissionCatalogValidator.findMissing(grantable)).containsExactly(Permissions.REPORTS_READ);
    }
}
