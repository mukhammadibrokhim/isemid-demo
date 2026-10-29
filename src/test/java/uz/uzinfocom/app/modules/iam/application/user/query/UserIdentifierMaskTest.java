package uz.uzinfocom.app.modules.iam.application.user.query;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserIdentifierMaskTest {

    @Test
    void keepsFirstThreeAndLastThreeCharacters() {
        assertThat(UserIdentifierMask.mask("31234567890123")).isEqualTo("312********123");
    }

    @Test
    void hidesShortValuesCompletely() {
        assertThat(UserIdentifierMask.mask("123456")).isEqualTo("******");
        assertThat(UserIdentifierMask.mask("12")).isEqualTo("**");
    }

    @Test
    void passesNullAndBlankThrough() {
        assertThat(UserIdentifierMask.mask(null)).isNull();
        assertThat(UserIdentifierMask.mask("  ")).isEqualTo("  ");
    }
}
