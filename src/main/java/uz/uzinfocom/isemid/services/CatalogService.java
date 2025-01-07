package uz.uzinfocom.isemid.services;

import jakarta.servlet.http.HttpServletRequest;
import uz.uzinfocom.isemid.models.entities.Catalog;
import uz.uzinfocom.isemid.models.entities.enums.CatalogType;
import uz.uzinfocom.isemid.models.payload.requests.ReqCatalog;
import uz.uzinfocom.isemid.models.payload.responses.RespCatalog;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponsePage;

import java.util.List;
import java.util.Optional;

public interface CatalogService {
    ApiResponse<Catalog> createCatalog(ReqCatalog reqCatalog);

    ApiResponse<Catalog> updateCatalogById(long id, ReqCatalog reqCatalog);

    ApiResponse<List<RespCatalog>> findAllByType(CatalogType catalogType, String lang);

    ApiResponsePage getAllCatalogs(int page, int size, String type, String code, String parentCode, String nameRu, String nameUz, String nameUzCyril, String nameKaa, HttpServletRequest request);

    ApiResponse<List<RespCatalog>> findAllByTypeAndParentCode(CatalogType catalogType, String parentCode, String lang);

    ApiResponse<List<RespCatalog>> findFirst20ByTypeAndSearch(CatalogType catalogType, String lang, String search);

    Optional<Catalog> findByTypeAndParentCode(CatalogType catalogType, String parentCode);
}
