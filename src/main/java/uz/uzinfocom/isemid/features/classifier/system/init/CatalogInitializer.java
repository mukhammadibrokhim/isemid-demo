package uz.uzinfocom.isemid.features.classifier.system.init;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uz.uzinfocom.isemid.commons.seed.CsvLoaderUtil;
import uz.uzinfocom.isemid.commons.seed.ModuleDataInitializer;
import uz.uzinfocom.isemid.features.classifier.models.Catalog;
import uz.uzinfocom.isemid.features.classifier.repositories.CatalogRepository;

import java.util.List;
import java.util.Set;

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
//@ConditionalOnProperty(name = "runner.catalog.init", havingValue = "true")
public class CatalogInitializer implements ModuleDataInitializer {
    private final CsvLoaderUtil csvLoaderUtil;
    private final CatalogRepository catalogRepository;

    @Override
    public void init() {
        log.info("♻️ Initializing default Catalogs...");

        List<String[]> rows = csvLoaderUtil.readCsv("classpath:/base/catalog.csv");
        Set<String> existingCodes = catalogRepository.findAllCodes();

        List<Catalog> newCatalogs = rows.stream()
                .skip(1) //skip header
                .map(record -> Catalog.builder()
                        .nameKaa(record[7])
                        .nameRu(record[8])
                        .nameUz(record[9])
                        .nameUzCyril(record[10])
                        .code(record[11])
                        .parentCode(record[12])
                        .type(record[13])
                        .build())
                .filter(catalog -> !existingCodes.contains(catalog.getCode()))
                .toList();

        if (!newCatalogs.isEmpty()) {
            catalogRepository.saveAll(newCatalogs);
            log.info("✅ {} new catalogs inserted successfully.", newCatalogs.size());
        } else {
            log.info("ℹ No new catalogs to insert.");
        }
    }
}
