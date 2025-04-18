package uz.uzinfocom.isemid.features.classifier.mappers;

import org.mapstruct.Named;
import org.springframework.stereotype.Component;
import uz.uzinfocom.isemid.features.classifier.dtos.responses.CatalogRespDto;
import uz.uzinfocom.isemid.features.classifier.models.Catalog;
import uz.uzinfocom.isemid.features.classifier.services.CatalogService;

@Component
public class CatalogConverter {

    private final CatalogService catalogService;

    public CatalogConverter(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @Named("toCatalogEntity")
    public Catalog toCatalogEntity(String code) {
        return catalogService.findByCode(code);
    }
    @Named("toCatalog")
    public CatalogRespDto toCatalog(String code) {
        if (code == null) {
            return null;
        }
        Catalog catalog = catalogService.findByCode(code);
        return (catalog != null) ? CatalogRespDto.builder()
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
    public String toCode(CatalogRespDto catalogDto) {
        return (catalogDto != null) ? catalogDto.getCode() : null;
    }
}
