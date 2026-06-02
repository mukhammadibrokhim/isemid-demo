package uz.uzinfocom.app.platform.iam.application.role.query.projection;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.StringUtils;

import java.util.Locale;

public interface RoleTableProjection {

    Long getId();

    String getName();

    Boolean getActive();

    String getDescriptionUz();

    String getDescriptionRu();

    String getDescriptionUzCyril();

    String getDescriptionKaa();

    default String getDescription() {
        Locale locale = LocaleContextHolder.getLocale();

        String language = locale.getLanguage().toLowerCase(Locale.ROOT);
        String tag = locale.toLanguageTag().toLowerCase(Locale.ROOT);

        if (tag.startsWith("uz-cyrl") && StringUtils.hasText(getDescriptionUzCyril())) {
            return getDescriptionUzCyril();
        }

        if ("ru".equals(language) && StringUtils.hasText(getDescriptionRu())) {
            return getDescriptionRu();
        }

        if ("kaa".equals(language) && StringUtils.hasText(getDescriptionKaa())) {
            return getDescriptionKaa();
        }

        if ("uz".equals(language) && StringUtils.hasText(getDescriptionUz())) {
            return getDescriptionUz();
        }

        return firstAvailable(
                getDescriptionUz(),
                getDescriptionUzCyril(),
                getDescriptionRu(),
                getDescriptionKaa()
        );
    }

    private static String firstAvailable(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value;
            }
        }

        return null;
    }
}