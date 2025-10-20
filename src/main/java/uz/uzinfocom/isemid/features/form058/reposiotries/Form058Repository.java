package uz.uzinfocom.isemid.features.form058.reposiotries;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.isemid.features.form058.models.Form058;

import java.time.LocalDate;
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

    @Query("""
    SELECT COUNT(f)
    FROM Form058 f
    WHERE f.status = 'APPROVED'
      AND f.createdOrg.stateCode = :stateCode
      AND (:fromDate IS NULL OR f.createdAt >= :fromDate)
      AND (:toDate IS NULL OR f.createdAt <= :toDate)
      AND (COALESCE(:mkb10Codes, NULL) IS NULL OR f.mkb10Code IN :mkb10Codes)
""")
    Long countConfirmedByState(
            @Param("stateCode") String stateCode,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("mkb10Codes") List<String> mkb10Codes
    );

    @Query("""
    SELECT COUNT(f)
    FROM Form058 f
    WHERE f.status <> 'APPROVED'
      AND f.createdOrg.stateCode = :stateCode
      AND (:fromDate IS NULL OR f.createdAt >= :fromDate)
      AND (:toDate IS NULL OR f.createdAt <= :toDate)
      AND (COALESCE(:mkb10Codes, NULL) IS NULL OR f.mkb10Code IN :mkb10Codes)
""")
    Long countUnconfirmedByState(
            @Param("stateCode") String stateCode,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("mkb10Codes") List<String> mkb10Codes
    );



}
