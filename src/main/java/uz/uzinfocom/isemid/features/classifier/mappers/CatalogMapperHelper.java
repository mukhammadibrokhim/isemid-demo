package uz.uzinfocom.isemid.features.classifier.mappers;

import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import uz.uzinfocom.isemid.features.classifier.dtos.responses.CatalogDisplayedResponse;
import uz.uzinfocom.isemid.features.classifier.dtos.responses.CatalogMinResponse;
import uz.uzinfocom.isemid.features.classifier.models.Catalog;
import uz.uzinfocom.isemid.features.classifier.services.CatalogService;

@Component
@RequiredArgsConstructor
public class CatalogMapperHelper {
    private final CatalogService catalogService;

    @Named("toEntity")
    public Catalog toEntity(String code) {
        return catalogService.findByCode(code);
    }

    @Named("toCatalog")
    public CatalogMinResponse toCatalog(String code) {
        if (code == null) {
            return null;
        }
        Catalog catalog = catalogService.findByCode(code);
        return (catalog != null) ? CatalogMinResponse.builder()
                .id(catalog.getId())
                .code(catalog.getCode())
                .parentCode(catalog.getParentCode())
                .nameUz(catalog.getNameUz())
                .nameRu(catalog.getNameRu())
                .nameKaa(catalog.getNameKaa())
                .nameUzCyril(catalog.getNameUzCyril())
                .build() : null;
    }

    @Named("toCode")
    public String toCode(CatalogMinResponse catalogDto) {
        return (catalogDto != null) ? catalogDto.getCode() : null;
    }
}
