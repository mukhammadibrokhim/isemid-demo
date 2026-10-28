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
 * usable value, otherwise the nearest other language that has one.
 */
@Component
public class OrganizationNameResolver {

    public String resolve(Organization organization) {
        if (organization == null) {
            return null;
        }

        return resolve(new OrganizationLocalizedName(
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

        Locale locale = LocaleContextHolder.getLocale();
        String language = locale.getLanguage();

        if ("ru".equalsIgnoreCase(language)) {
            return firstNonBlank(name.nameRu(), name.nameUzCyril(), name.nameUz(), name.nameKaa());
        }
        if ("kaa".equalsIgnoreCase(language)) {
            return firstNonBlank(name.nameKaa(), name.nameUz(), name.nameUzCyril(), name.nameRu());
        }
        if ("Cyrl".equalsIgnoreCase(locale.getScript())) {
            return firstNonBlank(name.nameUzCyril(), name.nameUz(), name.nameRu(), name.nameKaa());
        }
        return firstNonBlank(name.nameUz(), name.nameUzCyril(), name.nameRu(), name.nameKaa());
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
