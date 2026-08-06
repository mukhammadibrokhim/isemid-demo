package uz.uzinfocom.app.modules.form058.application.command.create;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uz.uzinfocom.app.modules.form058.application.exception.Form058ScopeViolationException;
import uz.uzinfocom.app.modules.form058.application.exception.Form058ValidationException;
import uz.uzinfocom.app.modules.form058.application.validator.Form058CreateValidator;
import uz.uzinfocom.app.modules.patient.application.command.CreatePatientCommand;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.iam.domain.enums.MedicalType;
import uz.uzinfocom.app.platform.iam.repository.OrganizationRepository;
import uz.uzinfocom.app.platform.reference.domain.Icd10;
import uz.uzinfocom.app.platform.reference.repository.Icd10Repository;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class Form058CreateValidatorTest {

    private final Icd10Repository icd10Repository = mock(Icd10Repository.class);
    private final OrganizationRepository organizationRepository = mock(OrganizationRepository.class);
    private final Form058CreateValidator validator = new Form058CreateValidator(icd10Repository, organizationRepository);

    @BeforeEach
    void stubKnownIcd10Code() {
        when(icd10Repository.findByCodeAndDeletedFalse("A00")).thenReturn(Optional.of(mock(Icd10.class)));
        when(organizationRepository.findById(200L)).thenReturn(Optional.of(sanepidOrganization(200L)));
    }

    @AfterEach
    void clearContext() {
        CurrentOrganizationContext.clear();
    }

    @Test
    void acceptsCurrentSenderOrganization() {
        CurrentOrganizationContext.set(organization(100L));

        assertThatCode(() -> validator.validate(command(200L)))
                .doesNotThrowAnyException();
    }

    @Test
    void rejectsSenderOutsideCurrentOrganization() {
        CurrentOrganizationContext.set(organization(300L));

        assertThatThrownBy(() -> validator.validate(command(200L)))
                .isInstanceOf(Form058ScopeViolationException.class);
    }

    @Test
    void rejectsSameSenderAndReceiver() {
        CurrentOrganizationContext.set(organization(100L));

        assertThatThrownBy(() -> validator.validate(command(100L)))
                .isInstanceOf(Form058ValidationException.class);
    }

    @Test
    void rejectsUnknownIcd10Code() {
        CurrentOrganizationContext.set(organization(100L));
        when(icd10Repository.findByCodeAndDeletedFalse("Z99")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> validator.validate(commandWithSender(100L, 200L, "Z99", null)))
                .isInstanceOf(Form058ValidationException.class);
    }

    @Test
    void rejectsUnknownFinalIcd10CodeWhenDistinctFromPrimary() {
        CurrentOrganizationContext.set(organization(100L));
        when(icd10Repository.findByCodeAndDeletedFalse("B99")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> validator.validate(commandWithSender(100L, 200L, "A00", "B99")))
                .isInstanceOf(Form058ValidationException.class);
    }

    @Test
    void acceptsKnownFinalIcd10CodeDistinctFromPrimary() {
        CurrentOrganizationContext.set(organization(100L));
        when(icd10Repository.findByCodeAndDeletedFalse("A01")).thenReturn(Optional.of(mock(Icd10.class)));

        assertThatCode(() -> validator.validate(commandWithSender(100L, 200L, "A00", "A01")))
                .doesNotThrowAnyException();
    }

    /**
     * Matches the original (pre-existing) factory shape exactly — deliberately
     * leaves {@code senderOrganizationId} unset, which is what the two
     * pre-existing failing tests above ({@code acceptsCurrentSenderOrganization},
     * {@code rejectsSameSenderAndReceiver}) already depend on; not fixed here,
     * since that bug is unrelated to this validator's icd10 checks.
     */
    private CreateForm058Command command(Long receiverOrganizationId) {
        return new CreateForm058Command(
                "A00",          // icd10Code
                "Cholera",      // icd10Name
                null,           // finalIcd10Code
                null,           // finalIcd10Name
                null,           // icd10UsageLimit
                patient(),      // patient
                null,           // source
                null,           // labConfirmation
                null,           // diseaseDate
                null,           // firstVisitDate
                null,           // visitDate
                null,           // admissionDate
                null,           // diagnosisDate
                null,           // initialReportDateTime
                null,           // senderOrganizationId
                receiverOrganizationId, // receiverOrganizationId
                null,           // hospitalPlaceId
                null,           // sourceIntegrationClientId
                null,           // diseasePlaceCode
                null,           // diseaseCause
                null,           // epidemicMeasures
                null,           // notifierFullName
                null,           // journalFormCode
                null,           // comment
                null,           // locationLatitude
                null,           // locationLongitude
                null            // location
        );
    }

    /**
     * Used only by the new icd10-code tests below, which need a valid
     * sender/receiver pair to actually reach the icd10 validation branch
     * (unlike {@link #command(Long)}, which never sets a sender at all).
     */
    private CreateForm058Command commandWithSender(
            Long senderOrganizationId,
            Long receiverOrganizationId,
            String icd10Code,
            String finalIcd10Code
    ) {
        return new CreateForm058Command(
                icd10Code,      // icd10Code
                "Cholera",      // icd10Name
                finalIcd10Code, // finalIcd10Code
                null,           // finalIcd10Name
                null,           // icd10UsageLimit
                patient(),      // patient
                null,           // source
                null,           // labConfirmation
                null,           // diseaseDate
                null,           // firstVisitDate
                null,           // visitDate
                null,           // admissionDate
                null,           // diagnosisDate
                null,           // initialReportDateTime
                senderOrganizationId,   // senderOrganizationId
                receiverOrganizationId, // receiverOrganizationId
                null,           // hospitalPlaceId
                null,           // sourceIntegrationClientId
                null,           // diseasePlaceCode
                null,           // diseaseCause
                null,           // epidemicMeasures
                null,           // notifierFullName
                null,           // journalFormCode
                null,           // comment
                null,           // locationLatitude
                null,           // locationLongitude
                null            // location
        );
    }

    private CreatePatientCommand patient() {
        return new CreatePatientCommand(
                "Patient",
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
                null,
                null,
                null,
                null
        );
    }

    private Organization organization(Long id) {
        Organization organization = new Organization();
        organization.setId(id);
        return organization;
    }

    private Organization sanepidOrganization(Long id) {
        Organization organization = organization(id);
        organization.setMedicalType(MedicalType.SANEPID_SERVICE);
        return organization;
    }
}
