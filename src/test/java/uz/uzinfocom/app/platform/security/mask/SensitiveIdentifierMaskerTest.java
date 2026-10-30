package uz.uzinfocom.app.platform.security.mask;

import org.junit.jupiter.api.Test;
import uz.uzinfocom.app.platform.security.auth.AdminAccessGuard;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SensitiveIdentifierMaskerTest {

    private final AdminAccessGuard guard = mock(AdminAccessGuard.class);
    private final SensitiveIdentifierMasker masker = new SensitiveIdentifierMasker(guard);

    @Test
    void masksEveryListedTypeForRegularCallers() {
        for (String type : List.of("PPN", "TAX", "FPPN", "DL", "MD", "BCT", "REO", "CZ", "TPPN", "NNUZB")) {
            assertThat(masker.mask(type, "AB1234567")).isEqualTo("AB1***567");
        }
    }

    @Test
    void typeMatchIgnoresCase() {
        assertThat(masker.mask("nnuzb", "31234567890123")).isEqualTo("312********123");
    }

    @Test
    void leavesOtherTypesUntouched() {
        assertThat(masker.mask("PASSPORT", "AB1234567")).isEqualTo("AB1234567");
        assertThat(masker.mask(null, "AB1234567")).isEqualTo("AB1234567");
    }

    @Test
    void superAdministratorSeesFullValue() {
        when(guard.isSuperAdmin()).thenReturn(true);

        assertThat(masker.mask("PPN", "AB1234567")).isEqualTo("AB1234567");
        assertThat(masker.maskForCaller("31234567890123")).isEqualTo("31234567890123");
    }

    @Test
    void shortValuesAreFullyHiddenAndNullsPass() {
        assertThat(masker.mask("DL", "123456")).isEqualTo("******");
        assertThat(masker.mask("DL", null)).isNull();
    }

    @Test
    void recognisesEchoedMaskedValue() {
        assertThat(SensitiveIdentifierMasker.isMaskedFormOf("AB1***567", "AB1234567")).isTrue();
        assertThat(SensitiveIdentifierMasker.isMaskedFormOf("AB1234567", "AB1234567")).isFalse();
    }
}
