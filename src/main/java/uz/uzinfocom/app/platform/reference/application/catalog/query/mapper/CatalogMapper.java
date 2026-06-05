package uz.uzinfocom.app.platform.reference.application.catalog.query.mapper;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import uz.uzinfocom.app.platform.reference.application.catalog.query.dto.CatalogResponse;
import uz.uzinfocom.app.platform.reference.application.catalog.query.dto.CatalogTableResponse;
import uz.uzinfocom.app.platform.reference.application.catalog.query.projection.CatalogTableProjection;
import uz.uzinfocom.app.platform.reference.application.lookup.ReferenceNameResolver;
import uz.uzinfocom.app.platform.reference.domain.Catalog;

@Mapper(componentModel = "spring")
public interface CatalogMapper {

    CatalogResponse toResponse(Catalog catalog);

    default CatalogTableResponse toTableResponse(
            CatalogTableProjection projection,
            @Context ReferenceNameResolver nameResolver
    ) {
        if (projection == null) {
            return null;
        }

        return new CatalogTableResponse(
                projection.getId(),
                projection.getType(),
                projection.getCode(),
                projection.getParentCode(),
                nameResolver.resolve(
                        projection.getNameUz(),
                        projection.getNameUzCyril(),
                        projection.getNameRu(),
                        projection.getNameKaa()
                ),
                projection.getSortOrder()
        );
    }
}
