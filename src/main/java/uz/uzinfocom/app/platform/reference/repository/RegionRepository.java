package uz.uzinfocom.app.platform.reference.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import uz.uzinfocom.app.platform.reference.application.lookup.projection.ReferenceItemProjection;
import uz.uzinfocom.app.platform.reference.domain.Region;

import java.util.List;
import java.util.Optional;

public interface RegionRepository extends JpaRepository<Region, Long>, JpaSpecificationExecutor<Region> {

    Optional<Region> findByIdAndDeletedFalse(Long id);

    Optional<Region> findByCodeAndDeletedFalse(String code);

    boolean existsByCodeAndDeletedFalse(String code);

    boolean existsByCode(String code);

    List<Region> findAllByDeletedFalseOrderByNameUzAsc();

    List<Region> findAllByParentCodeAndDeletedFalseOrderByNameUzAsc(String parentCode);

    @Query("""
        select
            r.code as code,
            r.parentCode as parentCode,
            r.nameUz as nameUz,
            r.nameUzCyril as nameUzCyril,
            r.nameRu as nameRu,
            r.nameKaa as nameKaa
        from Region r
        where r.deleted = false
        order by r.nameUz asc
    """)
    List<ReferenceItemProjection> findAllReferenceItems();
}
