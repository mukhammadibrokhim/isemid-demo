package uz.uzinfocom.app.modules.form0581.application.query.mapper.helper;

import org.mapstruct.Named;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.form0581.application.query.projection.Form0581TableProjection;
import uz.uzinfocom.app.modules.patient.domain.enums.AddressType;
import uz.uzinfocom.app.platform.reference.application.lookup.mapper.ReferenceMappingHelper;

import java.util.List;

@Component
public class Form0581TableAddressMapperHelper {

    private final ReferenceMappingHelper referenceMappingHelper;

    public Form0581TableAddressMapperHelper(ReferenceMappingHelper referenceMappingHelper) {
        this.referenceMappingHelper = referenceMappingHelper;
    }

    @Named("permanentRegionName")
    public String permanentRegionName(Form0581TableProjection.PatientProjection patient) {
        var address = findPermanentAddress(patient);

        if (address == null || isBlank(address.getRegionCode())) {
            return null;
        }

        return referenceMappingHelper.regionName(address.getRegionCode());
    }

    @Named("permanentDistrictName")
    public String permanentDistrictName(Form0581TableProjection.PatientProjection patient) {
        var address = findPermanentAddress(patient);

        if (address == null || isBlank(address.getDistrictCode())) {
            return null;
        }

        return referenceMappingHelper.districtName(address.getDistrictCode());
    }

    @Named("permanentNeighborhoodName")
    public String permanentNeighborhoodName(Form0581TableProjection.PatientProjection patient) {
        var address = findPermanentAddress(patient);

        if (address == null || isBlank(address.getNeighborhoodCode())) {
            return null;
        }

        return referenceMappingHelper.neighborhoodName(address.getNeighborhoodCode());
    }

    @Named("permanentStreetAddress")
    public String permanentStreetAddress(Form0581TableProjection.PatientProjection patient) {
        var address = findPermanentAddress(patient);

        if (address == null || isBlank(address.getStreetAddress())) {
            return null;
        }

        return address.getStreetAddress();
    }

    @Named("permanentHouseNumber")
    public String permanentHouseNumber(Form0581TableProjection.PatientProjection patient) {
        var address = findPermanentAddress(patient);

        if (address == null || isBlank(address.getHouseNumber())) {
            return null;
        }

        return address.getHouseNumber();
    }

    @Named("permanentApartmentNumber")
    public String permanentApartmentNumber(Form0581TableProjection.PatientProjection patient) {
        var address = findPermanentAddress(patient);

        if (address == null || isBlank(address.getApartmentNumber())) {
            return null;
        }

        return address.getApartmentNumber();
    }

    private Form0581TableProjection.PatientAddressProjection findPermanentAddress(
            Form0581TableProjection.PatientProjection patient
    ) {
        if (patient == null) {
            return null;
        }

        List<Form0581TableProjection.PatientAddressProjection> addresses = patient.getAddresses();

        if (addresses == null || addresses.isEmpty()) {
            return null;
        }

        return addresses.stream()
                .filter(address -> AddressType.PERMANENT.equals(address.getType()))
                .findFirst()
                .orElse(null);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
