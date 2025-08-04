package uz.uzinfocom.isemid.features.classifier.mappers;

import org.mapstruct.*;
import uz.uzinfocom.isemid.features.classifier.dtos.requests.CatalogRequest;
import uz.uzinfocom.isemid.features.classifier.dtos.responses.CatalogDisplayedResponse;
import uz.uzinfocom.isemid.features.classifier.dtos.responses.CatalogResponse;
import uz.uzinfocom.isemid.features.classifier.models.Catalog;

@Mapper(componentModel = "spring", uses = CatalogMapperHelper.class)
public interface CatalogMapper {

    Catalog toEntity(CatalogRequest dto);

    CatalogResponse toDto(Catalog catalog);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void update(@MappingTarget Catalog entity, CatalogRequest request);

    @Mapping(target = "display", expression = "java(getDisplayName(catalog, lang))")
    CatalogDisplayedResponse toDisplayedDto(Catalog catalog, @Context String lang);

    default String getDisplayName(Catalog catalog, String lang) {
        if (catalog == null || lang == null) return null;
        return switch (lang.toLowerCase()) {
            case "uz" -> catalog.getNameUz();
            case "kaa" -> catalog.getNameKaa();
            case "uz-cyrl", "uz_cyrl" -> catalog.getNameUzCyril();
            default -> catalog.getNameRu();
        };
    }
}