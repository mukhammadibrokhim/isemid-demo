package uz.uzinfocom.app.platform.reference.application.catalog.query.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.uzinfocom.app.platform.reference.application.catalog.query.dto.CatalogLookupResponse;
import uz.uzinfocom.app.platform.reference.application.catalog.query.dto.CatalogResponse;
import uz.uzinfocom.app.platform.reference.application.catalog.query.dto.CatalogTableResponse;
import uz.uzinfocom.app.platform.reference.application.catalog.query.projection.CatalogTableProjection;
import uz.uzinfocom.app.platform.reference.domain.Catalog;

@Mapper(componentModel = "spring", uses = CatalogQueryMappingHelper.class)
public interface CatalogMapper {

    CatalogResponse toResponse(Catalog catalog);

    CatalogTableResponse toTableResponse(CatalogTableProjection projection);

    @Mapping(target = "name", source = "catalog", qualifiedByName = "catalogName")
    CatalogLookupResponse toLookupResponse(Catalog catalog);
}
