package uz.uzinfocom.app.modules.patient.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.app.modules.patient.application.exception.PatientIdentifierNotFoundException;
import uz.uzinfocom.app.modules.patient.application.query.dto.detail.RevealedPatientIdentifierResponse;
import uz.uzinfocom.app.modules.patient.domain.model.Patient;
import uz.uzinfocom.app.modules.patient.domain.model.PatientIdentifier;
import uz.uzinfocom.app.platform.audit.application.SensitiveAccessAuditor;
import uz.uzinfocom.app.platform.audit.domain.AuditEntityType;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Hands out one patient identifier in full, after writing an audit row. The caller (a form query
 * service) is responsible for the two prior checks: the reveal permission ({@code @PreAuthorize})
 * and that the form — and therefore the patient — is visible in the caller's organization scope.
 */
@Service
@RequiredArgsConstructor
public class PatientIdentifierRevealService {

    private final SensitiveAccessAuditor auditor;

    /**
     * @param patient    the patient of an already scope-checked form
     * @param entityType the form kind the caller reached the patient through
     * @param entityId   that form's id (what the audit row points at)
     */
    public RevealedPatientIdentifierResponse reveal(
            Patient patient, Long identifierId, AuditEntityType entityType, Long entityId
    ) {
        PatientIdentifier identifier = patient.getIdentifiers().stream()
                .filter(candidate -> identifierId.equals(candidate.getId()))
                .findFirst()
                .orElseThrow(() -> new PatientIdentifierNotFoundException(identifierId));

        Map<String, Object> details = new LinkedHashMap<>();
        details.put("patientId", patient.getId());
        details.put("identifierId", identifier.getId());
        details.put("typeCode", identifier.getTypeCode());
        auditor.recordReveal(entityType, entityId, details);

        return new RevealedPatientIdentifierResponse(
                identifier.getId(), identifier.getTypeCode(), identifier.getValue());
    }
}
