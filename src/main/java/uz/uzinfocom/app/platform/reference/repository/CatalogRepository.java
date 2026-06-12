package uz.uzinfocom.app.platform.reference.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import uz.uzinfocom.app.platform.reference.application.lookup.projection.ReferenceItemProjection;
import uz.uzinfocom.app.platform.reference.domain.Catalog;

import java.util.List;
import java.util.Optional;

public interface CatalogRepository extends JpaRepository<Catalog, Long>, JpaSpecificationExecutor<Catalog> {

    Optional<Catalog> findByIdAndDeletedFalse(Long id);

    Optional<Catalog> findByTypeAndCodeAndDeletedFalse(String type, String code);

    Optional<Catalog> findFirstByCodeAndDeletedFalse(String code);

    boolean existsByTypeAndCode(String type, String code);

    List<Catalog> findAllByTypeAndDeletedFalseOrderByNameUzAsc(String type);

    List<Catalog> findAllByTypeAndParentCodeAndDeletedFalseOrderByNameUzAsc(
            String type,
            String parentCode
    );

    List<ReferenceItemProjection> findAllProjectedByTypeAndDeletedFalseOrderByNameUzAsc(String type);
}
