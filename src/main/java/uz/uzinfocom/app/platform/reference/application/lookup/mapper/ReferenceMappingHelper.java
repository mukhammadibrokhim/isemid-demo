package uz.uzinfocom.app.platform.reference.application.lookup.mapper;

import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.platform.reference.application.lookup.ReferenceLookupService;
import uz.uzinfocom.app.platform.reference.domain.enums.CatalogType;

@Component
@RequiredArgsConstructor
public class ReferenceMappingHelper {

    private final ReferenceLookupService referenceLookupService;

    @Named("countryName")
    public String countryName(String code) {
        return referenceLookupService.getCountryName(code);
    }

    @Named("regionName")
    public String regionName(String code) {
        return referenceLookupService.getRegionName(code);
    }

    @Named("districtName")
    public String districtName(String code) {
        return referenceLookupService.getDistrictName(code);
    }

    @Named("neighborhoodName")
    public String neighborhoodName(String code) {
        return referenceLookupService.getNeighborhoodName(code);
    }

    @Named("genderName")
    public String genderName(String code) {
        return referenceLookupService.getCatalogName(CatalogType.GENDER, code);
    }

    @Named("citizenshipName")
    public String citizenshipName(String code) {
        return referenceLookupService.getCatalogName(CatalogType.CITIZENSHIP, code);
    }

    @Named("residenceTypeName")
    public String residenceTypeName(String code) {
        return referenceLookupService.getCatalogName(CatalogType.RESIDENCE_TYPE, code);
    }

    @Named("populationTypeName")
    public String populationTypeName(String code) {
        return referenceLookupService.getCatalogName(CatalogType.POPULATION_TYPE, code);
    }

    @Named("diseasePlaceName")
    public String diseasePlaceName(String code) {
        return referenceLookupService.getCatalogName(CatalogType.DISEASE_PLACE, code);
    }

    @Named("formCategoryName")
    public String formCategoryName(String code) {
        return referenceLookupService.getCatalogName(CatalogType.FORM_CATEGORY, code);
    }

    @Named("sickPatientCategoryName")
    public String sickPatientCategoryName(String code) {
        return referenceLookupService.getCatalogName(CatalogType.SICK_PATIENT_CATEGORY, code);
    }

    @Named("hospitalPlaceName")
    public String hospitalPlaceName(String code) {
        return referenceLookupService.getCatalogName(CatalogType.HOSPITAL_PLACE, code);
    }

    @Named("deliveryMethodName")
    public String deliveryMethodName(String code) {
        return referenceLookupService.getCatalogName(CatalogType.DELIVERY_METHOD, code);
    }

    @Named("isolationStatusName")
    public String isolationStatusName(String code) {
        return referenceLookupService.getCatalogName(CatalogType.ISOLATION_STATUS, code);
    }
}
