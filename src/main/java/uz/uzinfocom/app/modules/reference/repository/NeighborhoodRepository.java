package uz.uzinfocom.app.modules.reference.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.uzinfocom.app.modules.reference.application.lookup.projection.GeoReferenceItemProjection;
import uz.uzinfocom.app.modules.reference.domain.Neighborhood;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface NeighborhoodRepository extends JpaRepository<Neighborhood, Long>, JpaSpecificationExecutor<Neighborhood> {

    Optional<Neighborhood> findByIdAndDeletedFalse(Long id);

    Optional<Neighborhood> findByCodeAndDeletedFalse(String code);

    boolean existsByCodeAndDeletedFalse(String code);

    boolean existsByCode(String code);

    List<Neighborhood> findAllByDeletedFalseOrderByNameUzAsc();

    /**
     * Existence check for many codes in one round trip - used to validate every
     * neighborhood code on a patient's addresses without a separate query per
     * code. See PatientCreateValidator.
     */
    @Query("select n.code from Neighborhood n where n.code in :codes and n.deleted = false")
    Set<String> findExistingCodes(@Param("codes") Collection<String> codes);

    @Query("""
        select
            n.code as code,
            n.parentCode as parentCode,
            n.nameUz as nameUz,
            n.nameUzCyril as nameUzCyril,
            n.nameRu as nameRu,
            n.nameKaa as nameKaa,
            n.soatoId as soatoId,
            n.tin as tin,
            n.uzcadRegistryCode as uzcadRegistryCode
        from Neighborhood n
        where n.deleted = false
        order by n.nameUz asc
    """)
    List<GeoReferenceItemProjection> findAllReferenceItems();
}