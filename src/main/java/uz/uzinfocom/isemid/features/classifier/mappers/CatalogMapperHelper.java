package uz.uzinfocom.isemid.features.classifier.mappers;

import lombok.RequiredArgsConstructor;
import org.mapstruct.Context;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;
import uz.uzinfocom.isemid.features.classifier.dtos.responses.CatalogMinResponse;
import uz.uzinfocom.isemid.features.classifier.models.Catalog;
import uz.uzinfocom.isemid.features.classifier.services.CatalogService;
import uz.uzinfocom.isemid.features.form058.constants.Form058Sources;

@Component
@RequiredArgsConstructor
public class CatalogMapperHelper {
    private final CatalogService catalogService;

    @Named("toEntity")
    public Catalog toEntity(String code) {
        return catalogService.findByCode(code);
    }

    @Named("toNeighborhoodCode")
    public String toNeighborhoodCode(String neighborhood, @Context String source) {
        if (!Form058Sources.DMED.equals(source)) {
            return neighborhood;
        }
        Catalog catalog = catalogService.findByInn(neighborhood);
        return (catalog != null) ? catalog.getCode() : null;
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
}
