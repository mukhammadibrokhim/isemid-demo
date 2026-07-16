package uz.uzinfocom.app.modules.patient.application.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uz.uzinfocom.app.modules.patient.application.command.CreatePatientAddressCommand;
import uz.uzinfocom.app.modules.patient.application.command.CreatePatientAffiliationCommand;
import uz.uzinfocom.app.modules.patient.application.command.CreatePatientCommand;
import uz.uzinfocom.app.modules.patient.application.exception.PatientValidationException;
import uz.uzinfocom.app.modules.patient.domain.enums.AddressType;
import uz.uzinfocom.app.modules.patient.domain.enums.AffiliationType;
import uz.uzinfocom.app.platform.reference.repository.DistrictRepository;
import uz.uzinfocom.app.platform.reference.repository.NeighborhoodRepository;
import uz.uzinfocom.app.platform.reference.repository.RegionRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PatientCreateValidatorTest {

    private final RegionRepository regionRepository = mock(RegionRepository.class);
    private final DistrictRepository districtRepository = mock(DistrictRepository.class);
    private final NeighborhoodRepository neighborhoodRepository = mock(NeighborhoodRepository.class);
    private final PatientCreateValidator validator =
            new PatientCreateValidator(regionRepository, districtRepository, neighborhoodRepository);

    @BeforeEach
    void stubKnownCodes() {
        when(regionRepository.existsByCodeAndDeletedFalse("R1")).thenReturn(true);
        when(districtRepository.existsByCodeAndDeletedFalse("D1")).thenReturn(true);
        when(neighborhoodRepository.existsByCodeAndDeletedFalse("N1")).thenReturn(true);
    }

    @Test
    void doesNothingForNullCommand() {
        assertThatCode(() -> validator.validate(null)).doesNotThrowAnyException();
    }

    @Test
    void acceptsWhenAllCodesAreBlank() {
        CreatePatientCommand command = command(
                List.of(address(null, null, null)),
                List.of(affiliation(null, null))
        );

        assertThatCode(() -> validator.validate(command)).doesNotThrowAnyException();
    }

    @Test
    void acceptsKnownAddressCodes() {
        CreatePatientCommand command = command(
                List.of(address("R1", "D1", "N1")),
                List.of()
        );

        assertThatCode(() -> validator.validate(command)).doesNotThrowAnyException();
    }

    @Test
    void rejectsUnknownRegionCodeInAddress() {
        when(regionRepository.existsByCodeAndDeletedFalse("R9")).thenReturn(false);
        CreatePatientCommand command = command(
                List.of(address("R9", null, null)),
                List.of()
        );

        assertThatThrownBy(() -> validator.validate(command))
                .isInstanceOf(PatientValidationException.class);
    }

    @Test
    void rejectsUnknownDistrictCodeInAddress() {
        when(districtRepository.existsByCodeAndDeletedFalse("D9")).thenReturn(false);
        CreatePatientCommand command = command(
                List.of(address(null, "D9", null)),
                List.of()
        );

        assertThatThrownBy(() -> validator.validate(command))
                .isInstanceOf(PatientValidationException.class);
    }

    @Test
    void rejectsUnknownNeighborhoodCodeInAddress() {
        when(neighborhoodRepository.existsByCodeAndDeletedFalse("N9")).thenReturn(false);
        CreatePatientCommand command = command(
                List.of(address(null, null, "N9")),
                List.of()
        );

        assertThatThrownBy(() -> validator.validate(command))
                .isInstanceOf(PatientValidationException.class);
    }

    @Test
    void acceptsKnownAffiliationCodes() {
        CreatePatientCommand command = command(
                List.of(),
                List.of(affiliation("R1", "D1"))
        );

        assertThatCode(() -> validator.validate(command)).doesNotThrowAnyException();
    }

    @Test
    void rejectsUnknownRegionCodeInAffiliation() {
        when(regionRepository.existsByCodeAndDeletedFalse("R9")).thenReturn(false);
        CreatePatientCommand command = command(
                List.of(),
                List.of(affiliation("R9", null))
        );

        assertThatThrownBy(() -> validator.validate(command))
                .isInstanceOf(PatientValidationException.class);
    }

    @Test
    void rejectsUnknownCityCodeInAffiliation() {
        when(districtRepository.existsByCodeAndDeletedFalse("D9")).thenReturn(false);
        CreatePatientCommand command = command(
                List.of(),
                List.of(affiliation(null, "D9"))
        );

        assertThatThrownBy(() -> validator.validate(command))
                .isInstanceOf(PatientValidationException.class);
    }

    private CreatePatientCommand command(
            List<CreatePatientAddressCommand> addresses,
            List<CreatePatientAffiliationCommand> affiliations
    ) {
        return new CreatePatientCommand(
                "First",
                "Last",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                List.of(),
                addresses,
                affiliations
        );
    }

    private CreatePatientAddressCommand address(String regionCode, String districtCode, String neighborhoodCode) {
        return new CreatePatientAddressCommand(
                AddressType.PERMANENT,
                regionCode,
                districtCode,
                neighborhoodCode,
                null,
                null,
                null
        );
    }

    private CreatePatientAffiliationCommand affiliation(String regionCode, String cityCode) {
        return new CreatePatientAffiliationCommand(
                AffiliationType.WORKPLACE,
                null,
                null,
                regionCode,
                cityCode,
                null,
                null,
                null
        );
    }
}
