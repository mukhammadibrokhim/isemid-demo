package uz.uzinfocom.app.platform.reference.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uz.uzinfocom.app.platform.reference.application.lookup.projection.ReferenceItemProjection;
import uz.uzinfocom.app.platform.reference.domain.District;

import java.util.List;
import java.util.Optional;

public interface DistrictRepository extends JpaRepository<District, Long> {

    Optional<District> findByIdAndDeletedFalse(Long id);

    Optional<District> findByCodeAndDeletedFalse(String code);

    boolean existsByCodeAndDeletedFalse(String code);

    boolean existsByCode(String code);

    List<District> findAllByDeletedFalseOrderBySortOrderAscNameUzAsc();

    List<District> findAllByParentCodeAndDeletedFalseOrderBySortOrderAscNameUzAsc(String parentCode);

    @Query("""
        select
            d.code as code,
            d.parentCode as parentCode,
            d.nameUz as nameUz,
            d.nameUzCyril as nameUzCyril,
            d.nameRu as nameRu,
            d.nameKaa as nameKaa
        from District d
        where d.deleted = false
        order by d.sortOrder asc, d.nameUz asc
    """)
    List<ReferenceItemProjection> findAllReferenceItems();
}
