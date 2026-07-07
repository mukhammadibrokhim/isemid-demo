package uz.uzinfocom.app.platform.iam.application.shared.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.platform.i18n.LocalizedTextResolver;
import uz.uzinfocom.app.platform.iam.application.shared.dto.OrganizationLocalizedName;
import uz.uzinfocom.app.platform.iam.domain.Organization;

/**
 * Single place that decides which organization display name wins: the
 * locale matching the current request (LocaleContextHolder) if it has a
 * usable value, otherwise the organization's always-present default name.
 */
@Component
@RequiredArgsConstructor
public class OrganizationNameResolver {

    private final LocalizedTextResolver localizedTextResolver;

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

        String localized = localizedTextResolver.resolve(
                name.nameUz(),
                name.nameUzCyril(),
                name.nameRu(),
                name.nameKaa()
        );

        return StringUtils.hasText(localized) ? localized : name.name();
    }
}
