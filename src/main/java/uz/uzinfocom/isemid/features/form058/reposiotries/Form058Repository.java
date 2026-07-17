package uz.uzinfocom.isemid.features.form058.reposiotries;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.integrations.bloodbank.dtos.InfectedPatientResponse;

import java.util.List;

@Repository
public interface Form058Repository extends JpaRepository<Form058, Long>, JpaSpecificationExecutor<Form058>, Form058RepositoryCustom {

    @Query("SELECT f FROM Form058 f " +
            "JOIN f.patient p " +
            "JOIN p.identifiers i " +
            "WHERE i.typeCode = 'NNUZB' AND i.value = :nnuzb")
    List<Form058> findAllByNnuzbIdentifier(@Param("nnuzb") String nnuzb);

    @EntityGraph(attributePaths = {"patient.addresses"})
    List<Form058> findAll(Specification<Form058> spec);

    /**
     * method for integration with blood bank system (external).
     * The system checks for existence of donor candidate in emis system
     * @param nnuzb
     * @return
     */
    @Query(value = """
        select new uz.uzinfocom.isemid.features.integrations.bloodbank.dtos.InfectedPatientResponse(f.finalMkb10Code,
             f.finalMkb10Name, f.diagnosisDate) from Form058 f join f.patient p join p.identifiers i
                 WHERE i.typeCode = 'NNUZB' AND i.value = :nnuzb
    """)
    List<InfectedPatientResponse> getInfectedPatientResponses(@Param("nnuzb") String nnuzb);
}
