package uz.uzinfocom.app.platform.i18n;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class LenientAcceptLanguageResolverTest {

    private final LenientAcceptLanguageResolver resolver = new LenientAcceptLanguageResolver();

    private Locale resolve(String header) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        if (header != null) {
            request.addHeader("Accept-Language", header);
        }
        return resolver.resolveLocale(request);
    }

    @Test
    void cyrillicSpellingsResolveToUzCyrl() {
        for (String tag : new String[]{"uz-Cyrl", "uz-cyrl", "uz_Cyrl", "uz-Cyrillic", "uz-Cyrl-UZ", "oz", "oz-UZ"}) {
            assertThat(resolve(tag).getScript()).as(tag).isEqualTo("Cyrl");
        }
    }

    @Test
    void otherLocalesAndDefault() {
        assertThat(resolve("uz-Latn").getScript()).isEmpty();
        assertThat(resolve("uz-UZ").getLanguage()).isEqualTo("uz");
        assertThat(resolve("ru-RU,ru;q=0.9").getLanguage()).isEqualTo("ru");
        assertThat(resolve("kaa").getLanguage()).isEqualTo("kaa");
        assertThat(resolve(null).getLanguage()).isEqualTo("uz");
        assertThat(resolve("fr").getLanguage()).isEqualTo("uz");
    }

    @Test
    void honoursQualityOrder() {
        assertThat(resolve("ru;q=0.5, uz_Cyrl;q=0.9").getScript()).isEqualTo("Cyrl");
    }
}
