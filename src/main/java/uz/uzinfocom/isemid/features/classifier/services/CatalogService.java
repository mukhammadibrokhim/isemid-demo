package uz.uzinfocom.isemid.features.classifier.services;

import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.dtos.ApiResponsePage;
import uz.uzinfocom.isemid.features.classifier.dtos.requests.CatalogRequest;
import uz.uzinfocom.isemid.features.classifier.dtos.responses.CatalogResponse;
import uz.uzinfocom.isemid.features.classifier.eums.CatalogType;
import uz.uzinfocom.isemid.features.classifier.models.Catalog;

import java.util.Optional;

public interface CatalogService {

    ApiResponsePage getFilteredCatalogs(int page, int size, String type, String code, String parentCode, String nameRu, String nameUz, String nameUzCyril, String nameKaa, String request);

    ApiResponse<CatalogResponse> create(CatalogRequest catalogRequest);

    ApiResponse<CatalogResponse> update(long id, CatalogRequest catalogRequest);

    ApiResponse<?> delete(long id);

    Catalog findByCode(String code);

    ApiResponse<?> findAllByType(CatalogType catalogType, String lang);

    ApiResponse<?> findAllByTypeAndParentCode(CatalogType catalogType, String parentCode, String lang);

    ApiResponse<?> findFirst20ByTypeAndSearch(CatalogType catalogType, String lang, String search);

    ApiResponsePage getByHierarchical(int page, int size, CatalogType type, String requestUrl);

    Optional<Catalog> findByTypeAndParentCode(CatalogType catalogType, String citizenshipid);

    Optional<Catalog> findByTypeAndCode(CatalogType catalogType, String code);
}
