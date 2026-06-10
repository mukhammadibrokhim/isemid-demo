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
        String tag = locale.toLanguageTag();

        if ("ru".equalsIgnoreCase(language)) {
            return firstNonBlank(ru, uz, uzCyril, kaa);
        }

        if ("kaa".equalsIgnoreCase(language)) {
            return firstNonBlank(kaa, uz, ru, uzCyril);
        }

        if ("uz-Cyril".equalsIgnoreCase(tag)
                || "uz-UZ-Cyril".equalsIgnoreCase(tag)
                || "uz_Cyril".equalsIgnoreCase(tag)) {
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