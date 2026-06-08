package uz.uzinfocom.app.platform.reference.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import uz.uzinfocom.app.platform.reference.application.lookup.projection.ReferenceItemProjection;
import uz.uzinfocom.app.platform.reference.domain.Neighborhood;

import java.util.List;
import java.util.Optional;

public interface NeighborhoodRepository extends JpaRepository<Neighborhood, Long>, JpaSpecificationExecutor<Neighborhood> {

    Optional<Neighborhood> findByIdAndDeletedFalse(Long id);

    Optional<Neighborhood> findByCodeAndDeletedFalse(String code);

    boolean existsByCodeAndDeletedFalse(String code);

    boolean existsByCode(String code);

    List<Neighborhood> findAllByDeletedFalseOrderByNameUzAsc();

    List<Neighborhood> findAllByParentCodeAndDeletedFalseOrderByNameUzAsc(String parentCode);

    @Query("""
        select
            n.code as code,
            n.parentCode as parentCode,
            n.nameUz as nameUz,
            n.nameUzCyril as nameUzCyril,
            n.nameRu as nameRu,
            n.nameKaa as nameKaa
        from Neighborhood n
        where n.deleted = false
        order by n.nameUz asc
    """)
    List<ReferenceItemProjection> findAllReferenceItems();
}
