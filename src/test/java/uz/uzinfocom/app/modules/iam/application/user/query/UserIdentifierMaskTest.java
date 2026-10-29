package uz.uzinfocom.app.modules.iam.application.user.query;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserIdentifierMaskTest {

    @Test
    void keepsOnlyLastFourCharacters() {
        assertThat(UserIdentifierMask.mask("31234567890123")).isEqualTo("**********0123");
    }

    @Test
    void hidesShortValuesCompletely() {
        assertThat(UserIdentifierMask.mask("1234")).isEqualTo("****");
        assertThat(UserIdentifierMask.mask("12")).isEqualTo("**");
    }

    @Test
    void passesNullAndBlankThrough() {
        assertThat(UserIdentifierMask.mask(null)).isNull();
        assertThat(UserIdentifierMask.mask("  ")).isEqualTo("  ");
    }
}
