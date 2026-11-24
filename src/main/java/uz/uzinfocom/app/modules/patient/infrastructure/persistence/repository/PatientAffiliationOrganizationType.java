package uz.uzinfocom.app.modules.patient.infrastructure.persistence.repository;

import uz.uzinfocom.app.modules.patient.domain.enums.AffiliationType;

/**
 * Projection for {@link PatientAffiliationJpaRepository#findTypesByPatientIdInAndOrganizationIdAndTypeIn} —
 * "for this patient, what kind of affiliation (workplace/study) do they have
 * with this organization". Not a general-purpose DTO; scoped to that one query.
 */
public record PatientAffiliationOrganizationType(Long patientId, AffiliationType type) {
}
