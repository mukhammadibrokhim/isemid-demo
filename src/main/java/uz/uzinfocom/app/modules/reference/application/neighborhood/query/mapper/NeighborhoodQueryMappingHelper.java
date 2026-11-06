package uz.uzinfocom.app.modules.reference.application.neighborhood.query.mapper;

import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.reference.application.lookup.ReferenceLookupService;
import uz.uzinfocom.app.modules.reference.application.lookup.dto.ReferenceItem;
import uz.uzinfocom.app.platform.i18n.LocalizedTextResolver;
import uz.uzinfocom.app.modules.reference.domain.Neighborhood;

@Component
@RequiredArgsConstructor
public class NeighborhoodQueryMappingHelper {

    private final LocalizedTextResolver localizedTextResolver;
    private final ReferenceLookupService referenceLookupService;

    /** Region code of the neighborhood's district, or null when the district is unknown. */
    @Named("neighborhoodGrandParentCode")
    public String neighborhoodGrandParentCode(String districtCode) {
        ReferenceItem district = referenceLookupService.findDistrict(districtCode);
        return district == null ? null : district.parentCode();
    }

    /** Localized region name of the neighborhood's district, or null when the district is unknown. */
    @Named("neighborhoodGrandParentName")
    public String neighborhoodGrandParentName(String districtCode) {
        String regionCode = neighborhoodGrandParentCode(districtCode);
        return regionCode == null ? null : referenceLookupService.getRegionName(regionCode);
    }

    @Named("neighborhoodLookupName")
    public String neighborhoodLookupName(Neighborhood neighborhood) {
        if (neighborhood == null) {
            return null;
        }

        return localizedTextResolver.resolve(
                neighborhood.getNameUz(),
                neighborhood.getNameUzCyril(),
                neighborhood.getNameRu(),
                neighborhood.getNameKaa()
        );
    }
}
