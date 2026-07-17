package uz.uzinfocom.app.modules.patient.application.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.patient.application.command.CreatePatientAddressCommand;
import uz.uzinfocom.app.modules.patient.application.command.CreatePatientAffiliationCommand;
import uz.uzinfocom.app.modules.patient.application.command.CreatePatientCommand;
import uz.uzinfocom.app.modules.patient.application.exception.PatientValidationException;
import uz.uzinfocom.app.platform.reference.repository.DistrictRepository;
import uz.uzinfocom.app.platform.reference.repository.NeighborhoodRepository;
import uz.uzinfocom.app.platform.reference.repository.RegionRepository;
import uz.uzinfocom.app.shared.validation.ReferenceCodeValidation;

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

    private void validateRegionCode(String regionCode) {
        ReferenceCodeValidation.requireExists(
                regionCode,
                regionRepository::existsByCodeAndDeletedFalse,
                () -> new PatientValidationException("error.patient.region-not-found", regionCode)
        );
    }

    private void validateDistrictCode(String districtCode) {
        ReferenceCodeValidation.requireExists(
                districtCode,
                districtRepository::existsByCodeAndDeletedFalse,
                () -> new PatientValidationException("error.patient.district-not-found", districtCode)
        );
    }

    private void validateNeighborhoodCode(String neighborhoodCode) {
        ReferenceCodeValidation.requireExists(
                neighborhoodCode,
                neighborhoodRepository::existsByCodeAndDeletedFalse,
                () -> new PatientValidationException("error.patient.neighborhood-not-found", neighborhoodCode)
        );
    }
}
