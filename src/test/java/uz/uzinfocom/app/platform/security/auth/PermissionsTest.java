package uz.uzinfocom.app.platform.security.auth;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PermissionsTest {

    @Test
    void requiredCollectsEveryAuthorityConstant() {
        Set<String> required = Permissions.required();

        assertThat(required).contains(
                "PERMISSION_REPORTS_READ",
                "PERMISSION_ATTACH_ACT_VIEW_ALL",
                "PERMISSION_SPEECH_TO_TEXT_CREATE");
        assertThat(required).allMatch(a -> a.startsWith("PERMISSION_") && a.equals(a.toUpperCase()));
    }

    @Test
    void everyHasExpressionReferencesADeclaredAuthority() throws Exception {
        List<Field> has = Arrays.stream(Permissions.class.getDeclaredFields())
                .filter(f -> Modifier.isStatic(f.getModifiers()) && f.getName().startsWith("HAS_"))
                .toList();

        assertThat(has).isNotEmpty();
        for (Field f : has) {
            String expr = (String) f.get(null);
            String authority = expr.substring(expr.indexOf('\'') + 1, expr.lastIndexOf('\''));
            assertThat(Permissions.required())
                    .as("%s must reference a declared authority", f.getName())
                    .contains(authority);
            assertThat(f.getName()).isEqualTo("HAS_" + authority.substring("PERMISSION_".length()));
        }
    }

    @Test
    void everyAuthorityHasAMatchingHasExpression() {
        Set<String> hasNames = Set.copyOf(Arrays.stream(Permissions.class.getDeclaredFields())
                .map(Field::getName)
                .filter(n -> n.startsWith("HAS_"))
                .toList());

        for (String authority : Permissions.required()) {
            assertThat(hasNames).contains("HAS_" + authority.substring("PERMISSION_".length()));
        }
    }

    @Test
    void authorityNamesMatchTheSubjectActionConvention() {
        assertThat(AuthorityNames.permission("attach-act", "view all")).isEqualTo(Permissions.ATTACH_ACT_VIEW_ALL);
    }
}
