package uz.uzinfocom.app.platform.reference.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.uzinfocom.app.platform.reference.application.lookup.projection.GeoReferenceItemProjection;
import uz.uzinfocom.app.platform.reference.domain.Region;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface RegionRepository extends JpaRepository<Region, Long>, JpaSpecificationExecutor<Region> {

    Optional<Region> findByIdAndDeletedFalse(Long id);

    Optional<Region> findByCodeAndDeletedFalse(String code);

    boolean existsByCodeAndDeletedFalse(String code);

    boolean existsByCode(String code);

    List<Region> findAllByDeletedFalseOrderByNameUzAsc();

    /**
     * Existence check for many codes in one round trip - used to validate every
     * region code on a patient (addresses + affiliations) without a separate
     * query per code. See PatientCreateValidator.
     */
    @Query("select r.code from Region r where r.code in :codes and r.deleted = false")
    Set<String> findExistingCodes(@Param("codes") Collection<String> codes);

    @Query("""
        select
            r.code as code,
            r.parentCode as parentCode,
            r.nameUz as nameUz,
            r.nameUzCyril as nameUzCyril,
            r.nameRu as nameRu,
            r.nameKaa as nameKaa,
            r.soatoId as soatoId,
            '' as tin,
            '' as uzcadRegistryCode
        from Region r
        where r.deleted = false
        order by r.nameUz asc
    """)
    List<GeoReferenceItemProjection> findAllReferenceItems();
}