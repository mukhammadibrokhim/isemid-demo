package uz.uzinfocom.app.platform.reference.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import uz.uzinfocom.app.platform.reference.application.lookup.projection.ReferenceItemProjection;
import uz.uzinfocom.app.platform.reference.domain.Catalog;
import uz.uzinfocom.app.platform.reference.domain.enums.CatalogType;

import java.util.List;
import java.util.Optional;

public interface CatalogRepository extends JpaRepository<Catalog, Long>, JpaSpecificationExecutor<Catalog> {

    Optional<Catalog> findByIdAndDeletedFalse(Long id);

    Optional<Catalog> findByTypeAndCodeAndDeletedFalse(CatalogType type, String code);

    boolean existsByTypeAndCode(CatalogType type, String code);

    boolean existsByTypeAndCodeAndDeletedFalse(CatalogType type, String code);

    List<Catalog> findAllByTypeAndDeletedFalseOrderBySortOrderAscNameUzAsc(CatalogType type);

    List<Catalog> findAllByTypeAndParentCodeAndDeletedFalseOrderBySortOrderAscNameUzAsc(
            CatalogType type,
            String parentCode
    );

    List<ReferenceItemProjection> findAllProjectedByTypeAndDeletedFalseOrderBySortOrderAscNameUzAsc(CatalogType type);
}
