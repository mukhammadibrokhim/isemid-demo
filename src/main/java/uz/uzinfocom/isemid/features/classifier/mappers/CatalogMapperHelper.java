package uz.uzinfocom.isemid.features.classifier.mappers;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.mapstruct.Context;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;
import uz.uzinfocom.isemid.features.classifier.dtos.responses.CatalogMinResponse;
import uz.uzinfocom.isemid.features.classifier.models.Catalog;
import uz.uzinfocom.isemid.features.classifier.services.CatalogService;
import uz.uzinfocom.isemid.features.form058.constants.Form058Sources;

@Component
@RequiredArgsConstructor
@Log4j2
public class CatalogMapperHelper {
    private final CatalogService catalogService;

    @Named("toEntity")
    public Catalog toEntity(String code) {
        return catalogService.findByCode(code);
    }

    @Named("toNeighborhoodCode")
    public String toNeighborhoodCode(String neighborhood, @Context String source) {
        if (Objects.nonNull(source) && Form058Sources.DMED.equalsIgnoreCase(source.strip())) {
            Catalog catalog = catalogService.findByInn(neighborhood);
            boolean isExplicit = Objects.nonNull(catalog);
            log.info("Form-058 is received from: {}, neighborhood_code: {}, mapped_neighborhood_code: {}",
                Form058Sources.DMED,neighborhood, isExplicit ?catalog.getCode():"NOT_MAPPED");
            return isExplicit ? catalog.getCode() : null;
        }
        log.info("Form-058 is received from {}", source);
        return neighborhood;

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
