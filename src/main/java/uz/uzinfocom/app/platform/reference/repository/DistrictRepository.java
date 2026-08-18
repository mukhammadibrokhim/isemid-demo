package uz.uzinfocom.app.platform.reference.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.uzinfocom.app.platform.reference.application.lookup.projection.GeoReferenceItemProjection;
import uz.uzinfocom.app.platform.reference.domain.District;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface DistrictRepository extends JpaRepository<District, Long>, JpaSpecificationExecutor<District> {

    Optional<District> findByIdAndDeletedFalse(Long id);

    Optional<District> findByCodeAndDeletedFalse(String code);

    boolean existsByCodeAndDeletedFalse(String code);

    boolean existsByCode(String code);

    List<District> findAllByDeletedFalseOrderByNameUzAsc();

    List<District> findAllByParentCodeAndDeletedFalseOrderByNameUzAsc(String parentCode);

    /**
     * Existence check for many codes in one round trip - used to validate every
     * district code on a patient (addresses + affiliations) without a separate
     * query per code. See PatientCreateValidator.
     */
    @Query("select d.code from District d where d.code in :codes and d.deleted = false")
    Set<String> findExistingCodes(@Param("codes") Collection<String> codes);

    @Query("""
        select
            d.code as code,
            d.parentCode as parentCode,
            d.nameUz as nameUz,
            d.nameUzCyril as nameUzCyril,
            d.nameRu as nameRu,
            d.nameKaa as nameKaa,
            d.soatoId as soatoId,
            '' as tin,
            '' as uzcadRegistryCode
        from District d
        where d.deleted = false
        order by d.nameUz asc
    """)
    List<GeoReferenceItemProjection> findAllReferenceItems();
}