package uz.uzinfocom.app.modules.patient.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.uzinfocom.app.modules.patient.domain.enums.AffiliationType;
import uz.uzinfocom.app.modules.patient.domain.model.PatientAffiliation;

import java.util.Collection;
import java.util.List;

public interface PatientAffiliationJpaRepository extends JpaRepository<PatientAffiliation, Long> {

    boolean existsByPatientIdAndOrganizationIdAndTypeIn(Long patientId, Long organizationId, Collection<AffiliationType> types);

    @Query("""
            SELECT DISTINCT pa.organizationId
            FROM PatientAffiliation pa
            WHERE pa.patient.id = :patientId
              AND pa.type IN :types
              AND pa.organizationId IS NOT NULL
            """)
    List<Long> findDistinctOrganizationIdsByPatientIdAndTypeIn(
            @Param("patientId") Long patientId,
            @Param("types") Collection<AffiliationType> types
    );

    /**
     * Bulk "which affiliation type explains each of these patients' visibility
     * into organizationId" lookup — used by {@code GET /v1/form-058/affiliated}
     * to label each row WORKPLACE/EDUCATIONAL without an N+1 query per row.
     * If a patient has more than one matching row for the same organization
     * (e.g. both a stale WORKPLACE and a newer EDUCATIONAL entry), the caller
     * picks one; this query makes no ordering guarantee between them.
     */
    @Query("""
            SELECT new uz.uzinfocom.app.modules.patient.infrastructure.persistence.repository.PatientAffiliationOrganizationType(
                pa.patient.id, pa.type
            )
            FROM PatientAffiliation pa
            WHERE pa.patient.id IN :patientIds
              AND pa.organizationId = :organizationId
              AND pa.type IN :types
            """)
    List<PatientAffiliationOrganizationType> findTypesByPatientIdInAndOrganizationIdAndTypeIn(
            @Param("patientIds") Collection<Long> patientIds,
            @Param("organizationId") Long organizationId,
            @Param("types") Collection<AffiliationType> types
    );
}
