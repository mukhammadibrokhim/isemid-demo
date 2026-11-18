package uz.uzinfocom.app.platform.security.auth;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FormPermissionGuardTest {

    private final FormPermissionGuard guard = new FormPermissionGuard();

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    private static void grant(String... a) {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("u", "n", a));
    }

    @Test
    void listDirectionNeedsTheMatchingReadSide() {
        grant("PERMISSION_FORM058_READ_SENT");

        assertThat(guard.canList("FORM058", "OUTGOING")).isTrue();
        assertThat(guard.canList("FORM058", "INCOMING")).isFalse();
        assertThat(guard.canList("FORM058", "ALL")).isFalse();
        assertThat(guard.canList("FORM0581", "OUTGOING")).isFalse();
        assertThat(guard.canList("FORM058", null)).isFalse();
    }

    @Test
    void allDirectionNeedsBothSides() {
        grant("PERMISSION_FORM058_READ_SENT", "PERMISSION_FORM058_READ_RECEIVED");

        assertThat(guard.canList("FORM058", "ALL")).isTrue();
    }

    @Test
    void readAnyAcceptsEitherSide() {
        grant("PERMISSION_FORM129_READ_RECEIVED");

        assertThat(guard.canReadAny("FORM129")).isTrue();
        assertThat(guard.canReadAny("FORM058")).isFalse();
    }

    @Test
    void cancelOrRejectDependsOnTheSide() {
        grant("PERMISSION_FORM058_CANCEL");

        assertThat(guard.canCancelOrReject("FORM058")).isTrue();
        assertThatCode(() -> guard.requireCancelOrReject("FORM058", true, false)).doesNotThrowAnyException();
        assertThatThrownBy(() -> guard.requireCancelOrReject("FORM058", false, true))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void noAuthenticationMeansNoPermission() {
        assertThat(guard.has("FORM058", "CREATE")).isFalse();
    }
}
