package uz.uzinfocom.isemid.features.classifier.mappers;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.uzinfocom.isemid.features.classifier.dtos.requests.CatalogRequest;
import uz.uzinfocom.isemid.features.classifier.dtos.responses.CatalogMiniResponse;
import uz.uzinfocom.isemid.features.classifier.dtos.responses.CatalogResponse;
import uz.uzinfocom.isemid.features.classifier.models.Catalog;

@Mapper(componentModel = "spring", uses = CatalogConverter.class)
public interface CatalogMapper {

    Catalog toEntity(CatalogRequest dto);

    CatalogResponse toDto(Catalog catalog);

    @Mapping(target = "display", expression = "java(getDisplayByLang(catalog, lang))")
    CatalogMiniResponse toDtoMini(Catalog catalog, @Context String lang);

    default String getDisplayByLang(Catalog catalog, @Context String lang) {
        if (catalog == null || lang == null) return null;

        return switch (lang.toLowerCase()) {
            case "uz" -> catalog.getNameUz();
            case "uzcyril" -> catalog.getNameUzCyril();
            case "kaa" -> catalog.getNameKaa();
            default -> catalog.getNameRu();
        };
    }
}