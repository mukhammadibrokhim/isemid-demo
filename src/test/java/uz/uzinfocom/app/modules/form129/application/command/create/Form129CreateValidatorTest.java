package uz.uzinfocom.app.modules.form129.application.command.create;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import uz.uzinfocom.app.modules.form129.application.exception.Form129ScopeViolationException;
import uz.uzinfocom.app.modules.form129.application.exception.Form129ValidationException;
import uz.uzinfocom.app.modules.form129.application.validator.Form129CreateValidator;
import uz.uzinfocom.app.modules.patient.application.command.CreatePatientCommand;
import uz.uzinfocom.app.modules.iam.domain.Organization;
import uz.uzinfocom.app.modules.iam.domain.enums.MedicalType;
import uz.uzinfocom.app.modules.iam.repository.OrganizationRepository;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class Form129CreateValidatorTest {

    private final OrganizationRepository organizationRepository = mock(OrganizationRepository.class);
    private final Form129CreateValidator validator = new Form129CreateValidator(organizationRepository);

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
                .isInstanceOf(Form129ScopeViolationException.class);
    }

    @Test
    void rejectsSameSenderAndReceiver() {
        CurrentOrganizationContext.set(organization(100L));

        assertThatThrownBy(() -> validator.validate(command(100L)))
                .isInstanceOf(Form129ValidationException.class);
    }

    @Test
    void rejectsReceiverThatIsNotSanepidService() {
        CurrentOrganizationContext.set(organization(100L));
        when(organizationRepository.findById(200L)).thenReturn(Optional.of(organization(200L, MedicalType.OTHER)));

        assertThatThrownBy(() -> validator.validate(command(200L)))
                .isInstanceOf(Form129ValidationException.class);
    }

    private CreateForm129Command command(Long receiverOrganizationId) {
        return new CreateForm129Command(
                null,                   // source
                null,                   // reportingInstitutionName
                null,                   // medicalId
                patient(),              // patient
                100L,                   // senderOrganizationId
                receiverOrganizationId, // receiverOrganizationId
                null,                   // sourceIntegrationClientId
                null, null,             // rwOutcome, rwResultText
                null, null,             // rprVdrlOutcome, rprVdrlResultText
                null, null,             // rpgaOutcome, rpgaResultText
                null, null,             // elisaOutcome, elisaResultText
                null, null,             // tphaOutcome, tphaResultText
                null, null,             // westernBlotOutcome, westernBlotResultText
                null, null,             // hbsAgOutcome, hbsAgResultText
                null, null,             // hbeAgOutcome, hbeAgResultText
                null, null,             // antiHbcIgGOutcome, antiHbcIgGResultText
                null, null,             // antiHbcIgMOutcome, antiHbcIgMResultText
                null, null,             // antiHbeOutcome, antiHbeResultText
                null, null,             // antiHbsOutcome, antiHbsResultText
                null, null,             // pcrQualitativeOutcome, pcrQualitativeResultText
                null, null,             // wrightHeddelsonOutcome, wrightHeddelsonResultText
                null                    // notifierFullName
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
