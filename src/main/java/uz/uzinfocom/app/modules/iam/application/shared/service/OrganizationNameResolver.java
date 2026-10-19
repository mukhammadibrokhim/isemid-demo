package uz.uzinfocom.app.modules.iam.application.shared.service;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.modules.iam.application.shared.dto.OrganizationLocalizedName;
import uz.uzinfocom.app.modules.iam.domain.Organization;

import java.util.Locale;

/**
 * Single place that decides which organization display name wins: the
 * locale matching the current request (LocaleContextHolder) if it has a
 * usable value, otherwise the organization's always-present default name.
 */
@Component
public class OrganizationNameResolver {

    public String resolve(Organization organization) {
        if (organization == null) {
            return null;
        }

        return resolve(new OrganizationLocalizedName(
                organization.getName(),
                organization.getNameUz(),
                organization.getNameUzCyril(),
                organization.getNameRu(),
                organization.getNameKaa()
        ));
    }

    public String resolve(OrganizationLocalizedName name) {
        if (name == null) {
            return null;
        }

        // The default name is Latin-script uz, so a missing uz value must fall
        // back to it before any Cyrillic/Russian alias (the generic
        // LocalizedTextResolver chain would pick those first).
        Locale locale = LocaleContextHolder.getLocale();
        String language = locale.getLanguage();

        String localized;
        if ("ru".equalsIgnoreCase(language)) {
            localized = firstNonBlank(name.nameRu(), name.nameUzCyril());
        } else if ("kaa".equalsIgnoreCase(language)) {
            localized = firstNonBlank(name.nameKaa());
        } else if ("Cyrl".equalsIgnoreCase(locale.getScript())) {
            localized = firstNonBlank(name.nameUzCyril());
        } else {
            localized = firstNonBlank(name.nameUz());
        }

        return StringUtils.hasText(localized) ? localized : name.name();
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return null;
    }
}
