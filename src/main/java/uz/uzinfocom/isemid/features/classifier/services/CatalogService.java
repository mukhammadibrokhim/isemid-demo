package uz.uzinfocom.isemid.features.classifier.services;

import org.springframework.data.domain.Page;
import uz.uzinfocom.isemid.features.classifier.dtos.requests.CatalogRequest;
import uz.uzinfocom.isemid.features.classifier.eums.CatalogType;
import uz.uzinfocom.isemid.features.classifier.models.Catalog;

import java.util.List;
import java.util.Optional;

public interface CatalogService {

    Page<Catalog> filter(int page, int size, String type, String code, String parentCode, String nameRu, String nameUz, String nameUzCyril, String nameKaa);

    Catalog create(CatalogRequest catalogRequest);

    Catalog update(long id, CatalogRequest catalogRequest);

    void delete(long id);

    Catalog findByCode(String code);

    List<Catalog> findAllByType(CatalogType catalogType);

    List<Catalog> findAllByTypeAndParentCode(CatalogType catalogType, String parentCode);

    List<Catalog> findFirst20ByTypeAndSearch(CatalogType catalogType, String search);

    Page<Catalog> getByHierarchical(int page, int size, CatalogType type);

    Optional<Catalog> findByTypeAndParentCode(CatalogType catalogType, String code);

    Optional<Catalog> findByTypeAndCode(CatalogType catalogType, String code);

    List<Catalog> finAllByType(CatalogType catalogType);

    Catalog getById(long id);
}
