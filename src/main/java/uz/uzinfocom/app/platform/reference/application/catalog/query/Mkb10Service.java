package uz.uzinfocom.app.platform.reference.application.catalog.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.app.platform.i18n.LocalizedTextResolver;
import uz.uzinfocom.app.platform.reference.application.catalog.query.dto.Mkb10Projection;
import uz.uzinfocom.app.platform.reference.domain.Catalog;
import uz.uzinfocom.app.platform.reference.repository.CatalogRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class Mkb10Service {

    private static final String MKB10_TYPE = "MKB10";

    private final CatalogRepository catalogRepository;
    private final LocalizedTextResolver localizedTextResolver;

    public List<Mkb10Projection> getNamesByCodesAndLocale(List<String> codes, String locale) {
        if (codes == null || codes.isEmpty()) {
            return List.of();
        }

        return codes.stream()
                .map(code -> catalogRepository.findByTypeAndCodeAndDeletedFalse(MKB10_TYPE, code)
                        .or(() -> catalogRepository.findFirstByCodeAndDeletedFalse(code))
                        .map(catalog -> toProjection(catalog, code))
                        .orElseGet(() -> new Mkb10Projection(code, code)))
                .toList();
    }

    private Mkb10Projection toProjection(Catalog catalog, String fallbackCode) {
        String name = localizedTextResolver.resolve(
                catalog.getNameUz(),
                catalog.getNameUzCyril(),
                catalog.getNameRu(),
                catalog.getNameKaa()
        );
        return new Mkb10Projection(catalog.getCode() == null ? fallbackCode : catalog.getCode(), name);
    }
}
