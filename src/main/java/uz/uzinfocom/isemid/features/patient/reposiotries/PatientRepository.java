package uz.uzinfocom.isemid.features.patient.reposiotries;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.isemid.features.patient.models.Patient;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    @Query(value = """
            SELECT p.*
            FROM patient p
            JOIN pt_identifier i ON p.id = i.patient_id
            WHERE i.type_code = :typeCode
              AND i.value = :value
            """, nativeQuery = true)
    Optional<Patient> findByIdentifierTypeAndValue(@Param("typeCode") String typeCode,
                                                   @Param("value") String value);

    @Query("""
            SELECT DISTINCT p FROM Patient p
            LEFT JOIN FETCH p.addresses
            WHERE p.id IN :ids
            """)
    List<Patient> findAllWithAddressesByIdIn(@Param("ids") Collection<Long> ids);
}
