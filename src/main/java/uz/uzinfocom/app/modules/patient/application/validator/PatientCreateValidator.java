package uz.uzinfocom.app.modules.patient.application.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.modules.patient.application.command.CreatePatientAddressCommand;
import uz.uzinfocom.app.modules.patient.application.command.CreatePatientAffiliationCommand;
import uz.uzinfocom.app.modules.patient.application.command.CreatePatientCommand;
import uz.uzinfocom.app.modules.patient.application.exception.PatientValidationException;
import uz.uzinfocom.app.modules.reference.repository.DistrictRepository;
import uz.uzinfocom.app.modules.reference.repository.NeighborhoodRepository;
import uz.uzinfocom.app.modules.reference.repository.RegionRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class PatientCreateValidator {

    private final RegionRepository regionRepository;
    private final DistrictRepository districtRepository;
    private final NeighborhoodRepository neighborhoodRepository;

    /**
     * Resolves every region/district/neighborhood code across all addresses and
     * affiliations with one existence query per catalog, not per code - a patient
     * can carry several addresses and affiliations, and checking each code
     * individually would issue a separate SELECT per code (N+1).
     */
    public void validate(CreatePatientCommand command) {
        if (command == null) {
            return;
        }

        List<CreatePatientAddressCommand> addresses = command.addresses();
        List<CreatePatientAffiliationCommand> affiliations = command.affiliations();

        Set<String> existingRegionCodes = regionRepository.findExistingCodes(codes(Stream.concat(
                addresses.stream().map(CreatePatientAddressCommand::regionCode),
                affiliations.stream().map(CreatePatientAffiliationCommand::regionCode)
        )));
        Set<String> existingDistrictCodes = districtRepository.findExistingCodes(codes(Stream.concat(
                addresses.stream().map(CreatePatientAddressCommand::districtCode),
                affiliations.stream().map(CreatePatientAffiliationCommand::districtCode)
        )));
        Set<String> existingNeighborhoodCodes = neighborhoodRepository.findExistingCodes(
                codes(addresses.stream().map(CreatePatientAddressCommand::neighborhoodCode))
        );

        for (CreatePatientAddressCommand address : addresses) {
            validateRegionCode(address.regionCode(), existingRegionCodes);
            validateDistrictCode(address.districtCode(), existingDistrictCodes);
            validateNeighborhoodCode(address.neighborhoodCode(), existingNeighborhoodCodes);
        }

        for (CreatePatientAffiliationCommand affiliation : affiliations) {
            validateRegionCode(affiliation.regionCode(), existingRegionCodes);
            validateDistrictCode(affiliation.districtCode(), existingDistrictCodes);
        }
    }

    private Set<String> codes(Stream<String> values) {
        return values.filter(StringUtils::hasText).collect(Collectors.toSet());
    }

    private void validateRegionCode(String regionCode, Set<String> existingCodes) {
        if (StringUtils.hasText(regionCode) && !existingCodes.contains(regionCode)) {
            throw new PatientValidationException("error.patient.region-not-found", regionCode);
        }
    }

    private void validateDistrictCode(String districtCode, Set<String> existingCodes) {
        if (StringUtils.hasText(districtCode) && !existingCodes.contains(districtCode)) {
            throw new PatientValidationException("error.patient.district-not-found", districtCode);
        }
    }

    private void validateNeighborhoodCode(String neighborhoodCode, Set<String> existingCodes) {
        if (StringUtils.hasText(neighborhoodCode) && !existingCodes.contains(neighborhoodCode)) {
            throw new PatientValidationException("error.patient.neighborhood-not-found", neighborhoodCode);
        }
    }
}
