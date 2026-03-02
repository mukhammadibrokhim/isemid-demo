package uz.uzinfocom.isemid.features.classifier.utils;

import org.springframework.context.i18n.LocaleContextHolder;
import uz.uzinfocom.isemid.features.classifier.models.Catalog;

import java.util.Locale;

public final class CatalogLocaleUtil {

    private CatalogLocaleUtil() {}

    public static String displayName(Catalog c) {
        if (c == null) return null;

        Locale locale = LocaleContextHolder.getLocale();
        String lang = normalize(locale);

        return switch (lang) {
            case "ru" -> first(c.getNameRu(), c.getNameUz(), c.getCode());
            case "kaa" -> first(c.getNameKaa(), c.getNameUz(), c.getCode());
            case "uz-cyrl" -> first(c.getNameUzCyril(), c.getNameUz(), c.getCode());
            default -> first(c.getNameUz(), c.getNameUzCyril(), c.getCode());
        };
    }

    private static String normalize(Locale locale) {
        if (locale == null) return "uz";

        String lang = locale.getLanguage().toLowerCase();
        String tag = locale.toLanguageTag().toLowerCase();

        switch (lang) {
            case "ru" -> {
                return "ru";
            }
            case "kaa" -> {
                return "kaa";
            }
            case "uz" -> {
                if (tag.contains("cyrl")) return "uz-cyrl";
                return "uz";
            }
        }

        return "uz";
    }

    private static String first(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) return v;
        }
        return null;
    }
}