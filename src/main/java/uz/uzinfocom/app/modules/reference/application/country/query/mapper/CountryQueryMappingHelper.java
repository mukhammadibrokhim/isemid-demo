package uz.uzinfocom.app.modules.reference.application.country.query.mapper;

import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.platform.i18n.LocalizedTextResolver;
import uz.uzinfocom.app.modules.reference.domain.Country;

@Component
@RequiredArgsConstructor
public class CountryQueryMappingHelper {

    private final LocalizedTextResolver localizedTextResolver;

    @Named("countryLookupName")
    public String countryLookupName(Country country) {
        if (country == null) {
            return null;
        }

        return localizedTextResolver.resolve(
                country.getNameUz(),
                country.getNameUzCyril(),
                country.getNameRu(),
                country.getNameKaa()
        );
    }
}
