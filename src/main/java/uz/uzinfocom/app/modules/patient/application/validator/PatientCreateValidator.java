package uz.uzinfocom.app.modules.patient.application.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.modules.patient.application.command.CreatePatientAddressCommand;
import uz.uzinfocom.app.modules.patient.application.command.CreatePatientAffiliationCommand;
import uz.uzinfocom.app.modules.patient.application.command.CreatePatientCommand;
import uz.uzinfocom.app.modules.patient.application.exception.PatientValidationException;
import uz.uzinfocom.app.platform.reference.repository.DistrictRepository;
import uz.uzinfocom.app.platform.reference.repository.NeighborhoodRepository;
import uz.uzinfocom.app.platform.reference.repository.RegionRepository;

@Component
@RequiredArgsConstructor
public class PatientCreateValidator {

    private final RegionRepository regionRepository;
    private final DistrictRepository districtRepository;
    private final NeighborhoodRepository neighborhoodRepository;

    public void validate(CreatePatientCommand command) {
        if (command == null) {
            return;
        }

        for (CreatePatientAddressCommand address : command.addresses()) {
            validateRegionCode(address.regionCode());
            validateDistrictCode(address.districtCode());
            validateNeighborhoodCode(address.neighborhoodCode());
        }

        for (CreatePatientAffiliationCommand affiliation : command.affiliations()) {
            validateRegionCode(affiliation.regionCode());
            validateDistrictCode(affiliation.cityCode());
        }
    }

    /**
     * Blank/missing codes are left to bean-validation on the request DTO —
     * this only rejects a code that was actually supplied but does not exist
     * in the reference catalog.
     */
    private void validateRegionCode(String regionCode) {
        if (StringUtils.hasText(regionCode) && !regionRepository.existsByCodeAndDeletedFalse(regionCode)) {
            throw new PatientValidationException("error.patient.region-not-found", regionCode);
        }
    }

    private void validateDistrictCode(String districtCode) {
        if (StringUtils.hasText(districtCode) && !districtRepository.existsByCodeAndDeletedFalse(districtCode)) {
            throw new PatientValidationException("error.patient.district-not-found", districtCode);
        }
    }

    private void validateNeighborhoodCode(String neighborhoodCode) {
        if (StringUtils.hasText(neighborhoodCode) && !neighborhoodRepository.existsByCodeAndDeletedFalse(neighborhoodCode)) {
            throw new PatientValidationException("error.patient.neighborhood-not-found", neighborhoodCode);
        }
    }
}
