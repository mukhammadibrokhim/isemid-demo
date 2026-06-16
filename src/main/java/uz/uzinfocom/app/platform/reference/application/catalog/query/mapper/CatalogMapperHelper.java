package uz.uzinfocom.app.platform.reference.application.catalog.query.mapper;

import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.platform.reference.application.catalog.query.dto.CatalogMinResponse;
import uz.uzinfocom.app.platform.reference.domain.Catalog;
import uz.uzinfocom.app.platform.reference.repository.CatalogRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CatalogMapperHelper {

    private final CatalogRepository catalogRepository;

    @Named("toCatalog")
    public CatalogMinResponse toCatalog(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }

        return catalogRepository.findFirstByCodeAndDeletedFalse(code)
                .map(this::toResponse)
                .orElseGet(() -> new CatalogMinResponse(null, code, code, code, code, code));
    }

    @Named("toCatalog")
    public List<CatalogMinResponse> toCatalog(List<String> codes) {
        if (codes == null) {
            return List.of();
        }
        return codes.stream().map(this::toCatalog).toList();
    }

    private CatalogMinResponse toResponse(Catalog catalog) {
        return new CatalogMinResponse(
                catalog.getId(),
                catalog.getCode(),
                catalog.getNameUz(),
                catalog.getNameUzCyril(),
                catalog.getNameRu(),
                catalog.getNameKaa()
        );
    }
}
