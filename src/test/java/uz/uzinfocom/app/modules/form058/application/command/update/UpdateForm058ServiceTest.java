package uz.uzinfocom.app.modules.form058.application.command.update;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import uz.uzinfocom.app.modules.form058.domain.model.Form058;
import uz.uzinfocom.app.modules.form058.infrastructure.persistence.repository.Form058JpaRepository;
import uz.uzinfocom.app.modules.patient.application.command.UpdatePatientAddressCommand;
import uz.uzinfocom.app.modules.patient.application.command.UpdatePatientAffiliationCommand;
import uz.uzinfocom.app.modules.patient.application.command.UpdatePatientCommand;
import uz.uzinfocom.app.modules.patient.domain.enums.AddressType;
import uz.uzinfocom.app.modules.patient.domain.enums.AffiliationType;
import uz.uzinfocom.app.modules.patient.domain.model.Patient;
import uz.uzinfocom.app.modules.patient.domain.model.PatientAffiliation;
import uz.uzinfocom.app.platform.security.context.CurrentUserProvider;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Regression test for the bug where {@code updatePatient} synced only
 * demographic fields/identifiers and silently dropped any addresses or
 * affiliations submitted on a Form058 update - unlike Form0581's equivalent,
 * which always upserted both. Also covers the affiliation-specific follow-up:
 * update corrects an existing affiliation matched by its own id, and never
 * creates a new one (see {@code UpdateForm058Service#upsertAffiliation}).
 */
class UpdateForm058ServiceTest {

    private final Form058JpaRepository form058Repository = mock(Form058JpaRepository.class);
    private final Form058UpdateMapper form058UpdateMapper = mock(Form058UpdateMapper.class);
    private final Form058UpdateValidator form058UpdateValidator = mock(Form058UpdateValidator.class);
    private final CurrentUserProvider currentUserProvider = mock(CurrentUserProvider.class);
    private final ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);

    private final UpdateForm058Service service = new UpdateForm058Service(
            form058Repository, form058UpdateMapper, form058UpdateValidator, currentUserProvider, eventPublisher
    );

    @Test
    void updateCorrectsExistingAffiliationByIdAndUpsertsAddresses() {
        Patient patient = new Patient();
        PatientAffiliation existingAffiliation = new PatientAffiliation();
        existingAffiliation.setId(42L);
        existingAffiliation.setType(AffiliationType.WORKPLACE);
        existingAffiliation.setOrganizationId(1L);
        patient.addAffiliation(existingAffiliation);

        Form058 form058 = new Form058();
        form058.setPatient(patient);

        when(form058Repository.findActiveByIdForUpdate(9L)).thenReturn(Optional.of(form058));
        when(form058Repository.save(form058)).thenReturn(form058);
        doNothing().when(form058UpdateValidator).validate(any(), any());
        when(form058UpdateMapper.toResult(form058)).thenReturn(null);

        UpdatePatientCommand patientCommand = new UpdatePatientCommand(
                "First", "Last", null, null, null, null,
                null, null, null, null, null, null, null,
                List.of(),
                List.of(new UpdatePatientAddressCommand(
                        null, AddressType.PERMANENT, "UZ-TK", "TK-283", null, "Street", "12", null)),
                List.of(new UpdatePatientAffiliationCommand(
                        42L, AffiliationType.WORKPLACE, LocalDate.of(2026, 1, 1),
                        "UZ-TK", "TK-283", 55L, null))
        );

        UpdateForm058Command command = new UpdateForm058Command(
                9L, null, null, null, null, null, null, null, null, null, null, null, null,
                patientCommand, null, null, null
        );

        service.update(command);

        assertThat(patient.getAffiliations()).hasSize(1);
        assertThat(patient.getAffiliations().get(0).getId()).isEqualTo(42L);
        assertThat(patient.getAffiliations().get(0).getType()).isEqualTo(AffiliationType.WORKPLACE);
        assertThat(patient.getAffiliations().get(0).getOrganizationId()).isEqualTo(55L);
        assertThat(patient.getAffiliations().get(0).getDistrictCode()).isEqualTo("TK-283");
        assertThat(patient.getAffiliations().get(0).getPatient()).isSameAs(patient);

        assertThat(patient.getAddresses()).hasSize(1);
        assertThat(patient.getAddresses().get(0).getType()).isEqualTo(AddressType.PERMANENT);
        assertThat(patient.getAddresses().get(0).getPatient()).isSameAs(patient);
    }

    @Test
    void updateNeverCreatesANewAffiliationEvenWhenNoneMatchTheGivenId() {
        Patient patient = new Patient();
        Form058 form058 = new Form058();
        form058.setPatient(patient);

        when(form058Repository.findActiveByIdForUpdate(9L)).thenReturn(Optional.of(form058));
        when(form058Repository.save(form058)).thenReturn(form058);
        doNothing().when(form058UpdateValidator).validate(any(), any());
        when(form058UpdateMapper.toResult(form058)).thenReturn(null);

        UpdatePatientCommand patientCommand = new UpdatePatientCommand(
                "First", "Last", null, null, null, null,
                null, null, null, null, null, null, null,
                List.of(),
                List.of(),
                List.of(new UpdatePatientAffiliationCommand(
                        999L, AffiliationType.WORKPLACE, LocalDate.of(2026, 1, 1),
                        "UZ-TK", "TK-283", 55L, null))
        );

        UpdateForm058Command command = new UpdateForm058Command(
                9L, null, null, null, null, null, null, null, null, null, null, null, null,
                patientCommand, null, null, null
        );

        service.update(command);

        assertThat(patient.getAffiliations()).isEmpty();
    }
}
