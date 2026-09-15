package uz.uzinfocom.app.orchestration.scope;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.patient.domain.enums.AffiliationType;
import uz.uzinfocom.app.modules.patient.infrastructure.persistence.repository.PatientAffiliationJpaRepository;

import java.util.EnumSet;

/**
 * Whether an organization may act on a case (Form058/Form0581) — either
 * because it is the sender or receiver, or because the patient's workplace
 * or place of study (a {@code PatientAffiliation} of type {@code WORKPLACE}/
 * {@code EDUCATIONAL}) is that organization. This is the same "external"
 * access rule {@code Form058Specification} already applies when listing
 * forms with {@code affiliation=true} — this class makes it reusable for
 * single-entity command-side authorization (e.g. assigning cards/acts),
 * not just query-time filtering.
 * <p>
 * Takes raw ids rather than {@code Form058}/{@code Form0581} entities so it
 * stays usable from any module without pulling either into {@code platform}.
 */
@Component
@RequiredArgsConstructor
public class FormAccessScopeResolver {

    /**
     * Public so callers that need "which affiliation types count as external
     * access" without a full {@link #canAccess} check (e.g. {@code
     * NotificationEventListener} resolving every organization a patient is
     * affiliated with) can reuse the same definition instead of duplicating it.
     */
    public static final EnumSet<AffiliationType> AFFILIATION_TYPES =
            EnumSet.of(AffiliationType.WORKPLACE, AffiliationType.EDUCATIONAL);

    private final PatientAffiliationJpaRepository patientAffiliationRepository;

    public boolean canAccess(Long organizationId, Long senderOrganizationId, Long receiverOrganizationId, Long patientId) {
        if (organizationId == null) {
            return false;
        }

        if (organizationId.equals(senderOrganizationId) || organizationId.equals(receiverOrganizationId)) {
            return true;
        }

        return patientId != null
                && patientAffiliationRepository.existsByPatientIdAndOrganizationIdAndTypeIn(patientId, organizationId, AFFILIATION_TYPES);
    }
}
