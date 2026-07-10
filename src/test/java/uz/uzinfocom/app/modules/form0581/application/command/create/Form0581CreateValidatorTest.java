package uz.uzinfocom.app.modules.form0581.application.command.create;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import uz.uzinfocom.app.modules.form0581.application.exception.Form0581ScopeViolationException;
import uz.uzinfocom.app.modules.form0581.application.exception.Form0581ValidationException;
import uz.uzinfocom.app.modules.form0581.application.validator.Form0581CreateValidator;
import uz.uzinfocom.app.modules.patient.application.command.CreatePatientCommand;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.iam.domain.enums.MedicalType;
import uz.uzinfocom.app.platform.iam.repository.OrganizationRepository;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class Form0581CreateValidatorTest {

    private final OrganizationRepository organizationRepository = mock(OrganizationRepository.class);
    private final Form0581CreateValidator validator = new Form0581CreateValidator(organizationRepository);

    @AfterEach
    void clearContext() {
        CurrentOrganizationContext.clear();
    }

    @Test
    void acceptsCurrentSenderOrganizationWithSanepidReceiver() {
        CurrentOrganizationContext.set(organization(100L));
        when(organizationRepository.findById(200L)).thenReturn(Optional.of(organization(200L, MedicalType.SANEPID_SERVICE)));

        assertThatCode(() -> validator.validate(command(200L)))
                .doesNotThrowAnyException();
    }

    @Test
    void rejectsSenderOutsideCurrentOrganization() {
        CurrentOrganizationContext.set(organization(300L));

        assertThatThrownBy(() -> validator.validate(command(200L)))
                .isInstanceOf(Form0581ScopeViolationException.class);
    }

    @Test
    void rejectsSameSenderAndReceiver() {
        CurrentOrganizationContext.set(organization(100L));

        assertThatThrownBy(() -> validator.validate(command(100L)))
                .isInstanceOf(Form0581ValidationException.class);
    }

    @Test
    void rejectsReceiverThatIsNotSanepidService() {
        CurrentOrganizationContext.set(organization(100L));
        when(organizationRepository.findById(200L)).thenReturn(Optional.of(organization(200L, MedicalType.OTHER)));

        assertThatThrownBy(() -> validator.validate(command(200L)))
                .isInstanceOf(Form0581ValidationException.class);
    }

    private CreateForm0581Command command(Long receiverOrganizationId) {
        return new CreateForm0581Command(
                "A82",                  // mkb10Code
                "Rabies",               // mkb10Name
                null,                   // injuryLocalization
                null,                   // injuryDateTime
                null,                   // dpuVisitDateTime
                null,                   // injuryRegionCode
                null,                   // injuryDistrictCode
                null,                   // injuryAddress
                null,                   // animalCategoryCode
                null,                   // animalColor
                null,                   // animalType
                null,                   // animalBreed
                null,                   // ownerLastName
                null,                   // ownerFirstName
                null,                   // ownerMiddleName
                null,                   // ownerRegionCode
                null,                   // ownerDistrictCode
                null,                   // ownerNeighborhoodCode
                null,                   // ownerStreet
                null,                   // ownerHouseNumber
                null,                   // ownerApartmentNumber
                patient(),              // patient
                null,                   // source
                100L,                   // senderOrganizationId
                receiverOrganizationId, // receiverOrganizationId
                null,                   // otherPeopleInjured
                List.of(),              // otherInjuredPeople
                null,                   // hospitalizedAt
                null,                   // hospitalOrganizationId
                null,                   // antirabicAssistanceInfo
                null,                   // notifierFullName
                null,                   // receiverFullName
                null                    // messageSentAt
        );
    }

    private CreatePatientCommand patient() {
        return new CreatePatientCommand(
                "Patient",
                null, null, null, null, null,
                null, null,
                null, null, null, null, null,
                null, null, null
        );
    }

    private Organization organization(Long id) {
        Organization organization = new Organization();
        organization.setId(id);
        return organization;
    }

    private Organization organization(Long id, MedicalType medicalType) {
        Organization organization = organization(id);
        organization.setMedicalType(medicalType);
        return organization;
    }
}
