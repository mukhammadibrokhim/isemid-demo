package uz.uzinfocom.app.modules.form058.application.query.mapper.helper;

import org.mapstruct.Named;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.form058.application.query.projection.Form058TableProjection;
import uz.uzinfocom.app.modules.patient.domain.enums.AddressType;
import uz.uzinfocom.app.platform.reference.application.lookup.mapper.ReferenceMappingHelper;

import java.util.List;

@Component
public class Form058TableAddressMapperHelper {

    private final ReferenceMappingHelper referenceMappingHelper;

    public Form058TableAddressMapperHelper(ReferenceMappingHelper referenceMappingHelper) {
        this.referenceMappingHelper = referenceMappingHelper;
    }

    @Named("permanentRegionName")
    public String permanentRegionName(Form058TableProjection.PatientProjection patient) {
        var address = findPermanentAddress(patient);

        if (address == null || isBlank(address.getRegionCode())) {
            return null;
        }

        return referenceMappingHelper.regionName(address.getRegionCode());
    }

    @Named("permanentDistrictName")
    public String permanentDistrictName(Form058TableProjection.PatientProjection patient) {
        var address = findPermanentAddress(patient);

        if (address == null || isBlank(address.getDistrictCode())) {
            return null;
        }

        return referenceMappingHelper.districtName(address.getDistrictCode());
    }

    @Named("permanentNeighborhoodName")
    public String permanentNeighborhoodName(Form058TableProjection.PatientProjection patient) {
        var address = findPermanentAddress(patient);

        if (address == null || isBlank(address.getNeighborhoodCode())) {
            return null;
        }

        return referenceMappingHelper.neighborhoodName(address.getNeighborhoodCode());
    }

    @Named("permanentStreetAddress")
    public String permanentStreetAddress(Form058TableProjection.PatientProjection patient) {
        var address = findPermanentAddress(patient);

        if (address == null || isBlank(address.getStreetAddress())) {
            return null;
        }

        return address.getStreetAddress();
    }

    @Named("permanentHouseNumber")
    public String permanentHouseNumber(Form058TableProjection.PatientProjection patient) {
        var address = findPermanentAddress(patient);

        if (address == null || isBlank(address.getHouseNumber())) {
            return null;
        }

        return address.getHouseNumber();
    }

    @Named("permanentApartmentNumber")
    public String permanentApartmentNumber(Form058TableProjection.PatientProjection patient) {
        var address = findPermanentAddress(patient);

        if (address == null || isBlank(address.getApartmentNumber())) {
            return null;
        }

        return address.getApartmentNumber();
    }

    private Form058TableProjection.PatientAddressProjection findPermanentAddress(
            Form058TableProjection.PatientProjection patient
    ) {
        if (patient == null) {
            return null;
        }

        List<Form058TableProjection.PatientAddressProjection> addresses = patient.getAddresses();

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