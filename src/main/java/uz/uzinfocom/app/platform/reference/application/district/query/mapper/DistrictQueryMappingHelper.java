package uz.uzinfocom.app.platform.reference.application.district.query.mapper;

import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.platform.i18n.LocalizedTextResolver;
import uz.uzinfocom.app.platform.reference.domain.District;

@Component
@RequiredArgsConstructor
public class DistrictQueryMappingHelper {

    private final LocalizedTextResolver localizedTextResolver;

    @Named("districtLookupName")
    public String districtLookupName(District district) {
        if (district == null) {
            return null;
        }

        return localizedTextResolver.resolve(
                district.getNameUz(),
                district.getNameUzCyril(),
                district.getNameRu(),
                district.getNameKaa()
        );
    }
}
