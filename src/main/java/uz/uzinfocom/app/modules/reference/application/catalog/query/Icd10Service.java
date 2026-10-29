package uz.uzinfocom.app.modules.reference.application.catalog.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.app.platform.i18n.LocalizedTextResolver;
import uz.uzinfocom.app.modules.reference.application.catalog.query.dto.Icd10Projection;
import uz.uzinfocom.app.modules.reference.domain.Catalog;
import uz.uzinfocom.app.modules.reference.repository.CatalogRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class Icd10Service {

    private static final String ICD10_TYPE = "ICD10";

    private final CatalogRepository catalogRepository;
    private final LocalizedTextResolver localizedTextResolver;

    public List<Icd10Projection> getNamesByCodesAndLocale(List<String> codes, String locale) {
        if (codes == null || codes.isEmpty()) {
            return List.of();
        }

        return codes.stream()
                .map(code -> catalogRepository.findByTypeAndCodeAndDeletedFalse(ICD10_TYPE, code)
                        .or(() -> catalogRepository.findFirstByCodeAndDeletedFalse(code))
                        .map(catalog -> toProjection(catalog, code))
                        .orElseGet(() -> new Icd10Projection(code, code)))
                .toList();
    }

    private Icd10Projection toProjection(Catalog catalog, String fallbackCode) {
        String name = localizedTextResolver.resolve(
                catalog.getNameUz(),
                catalog.getNameUzCyril(),
                catalog.getNameRu(),
                catalog.getNameKaa()
        );
        return new Icd10Projection(catalog.getCode() == null ? fallbackCode : catalog.getCode(), name);
    }
}
