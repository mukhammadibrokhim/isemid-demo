package uz.uzinfocom.app.platform.i18n;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Locale;

@Component
public class LocalizedTextResolver {

    public String resolve(
            String uz,
            String uzCyril,
            String ru,
            String kaa
    ) {
        Locale locale = LocaleContextHolder.getLocale();

        String language = locale.getLanguage();

        if ("ru".equalsIgnoreCase(language)) {
            return firstNonBlank(ru, uz, uzCyril, kaa);
        }

        if ("kaa".equalsIgnoreCase(language)) {
            return firstNonBlank(kaa, uz, ru, uzCyril);
        }

        // Script-based check (not a tag-string match): BCP-47's script subtag
        // for Cyrillic is "Cyrl", correctly parsed only when the locale is
        // registered as "uz-Cyrl" (see I18nConfig) — not the "uz-Cyril" typo
        // this used to compare against, which never matched a real request.
        if ("Cyrl".equalsIgnoreCase(locale.getScript())) {
            return firstNonBlank(uzCyril, uz, ru, kaa);
        }

        return firstNonBlank(uz, ru, uzCyril, kaa);
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }

        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value;
            }
        }

        return null;
    }
}