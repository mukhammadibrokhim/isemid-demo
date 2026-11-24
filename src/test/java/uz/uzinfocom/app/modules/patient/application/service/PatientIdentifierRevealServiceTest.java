package uz.uzinfocom.app.modules.patient.application.service;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import uz.uzinfocom.app.modules.patient.application.exception.PatientIdentifierNotFoundException;
import uz.uzinfocom.app.modules.patient.application.query.dto.detail.RevealedPatientIdentifierResponse;
import uz.uzinfocom.app.modules.patient.domain.model.Patient;
import uz.uzinfocom.app.modules.patient.domain.model.PatientIdentifier;
import uz.uzinfocom.app.platform.audit.application.SensitiveAccessAuditor;
import uz.uzinfocom.app.platform.audit.domain.AuditEntityType;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class PatientIdentifierRevealServiceTest {

    private final SensitiveAccessAuditor auditor = mock(SensitiveAccessAuditor.class);
    private final PatientIdentifierRevealService service = new PatientIdentifierRevealService(auditor);

    private Patient patientWithIdentifier() {
        Patient patient = new Patient();
        patient.setId(5L);
        PatientIdentifier identifier = new PatientIdentifier();
        identifier.setId(11L);
        identifier.setTypeCode("TAX");
        identifier.setValue("32512345678042");
        patient.getIdentifiers().add(identifier);
        return patient;
    }

    @Test
    @SuppressWarnings("unchecked")
    void returnsFullValueAndAuditsWithoutTheValue() {
        RevealedPatientIdentifierResponse response =
                service.reveal(patientWithIdentifier(), 11L, AuditEntityType.FORM058, 99L);

        assertThat(response.value()).isEqualTo("32512345678042");

        ArgumentCaptor<Map<String, Object>> details = ArgumentCaptor.forClass(Map.class);
        verify(auditor).recordReveal(eq(AuditEntityType.FORM058), eq(99L), details.capture());
        assertThat(details.getValue())
                .containsEntry("identifierId", 11L)
                .containsEntry("typeCode", "TAX")
                .doesNotContainValue("32512345678042");
    }

    @Test
    void unknownIdentifierIsNotFoundAndNotAudited() {
        assertThatThrownBy(() -> service.reveal(patientWithIdentifier(), 404L, AuditEntityType.FORM058, 99L))
                .isInstanceOf(PatientIdentifierNotFoundException.class);

        verify(auditor, never()).recordReveal(any(), any(), any());
    }

    @Test
    void auditFailureBlocksTheReveal() {
        doThrow(new IllegalStateException("db down")).when(auditor).recordReveal(any(), any(), any());

        assertThatThrownBy(() -> service.reveal(patientWithIdentifier(), 11L, AuditEntityType.FORM058, 99L))
                .isInstanceOf(IllegalStateException.class);
    }
}
