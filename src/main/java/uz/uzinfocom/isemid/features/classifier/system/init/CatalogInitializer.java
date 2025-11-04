package uz.uzinfocom.isemid.features.classifier.system.init;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uz.uzinfocom.isemid.commons.seed.CsvLoaderUtil;
import uz.uzinfocom.isemid.commons.seed.ModuleDataInitializer;
import uz.uzinfocom.isemid.features.classifier.models.Catalog;
import uz.uzinfocom.isemid.features.classifier.repositories.CatalogRepository;

import java.util.*;

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
        if (rows == null || rows.isEmpty()) {
            log.warn("⚠️ catalog.csv is empty or not found!");
            return;
        }

        String[] header = rows.getFirst();
        Map<String, Integer> indexMap = new HashMap<>();
        for (int i = 0; i < header.length; i++) {
            indexMap.put(header[i].trim().toLowerCase(), i);
        }

        Set<String> existingCodes = catalogRepository.findAllCodes();

        List<Catalog> newCatalogs = rows.stream()
                .skip(1) // ? Skip header
                .map(row -> {
                    String code = row[indexMap.get("code")];
                    if (code == null || existingCodes.contains(code)) {
                        return null;
                    }

                    return Catalog.builder()
                            .code(code)
                            .nameUz(row[indexMap.get("name_uz")])
                            .nameUzCyril(row[indexMap.get("name_uz_cyril")])
                            .nameRu(row[indexMap.get("name_ru")])
                            .nameKaa(row[indexMap.get("name_kaa")])
                            .parentCode(row[indexMap.get("parent_code")])
                            .type(row[indexMap.get("type")])
                            .build();
                })
                .filter(Objects::nonNull)
                .toList();

        // ✅ 4) Save only new ones
        if (!newCatalogs.isEmpty()) {
            catalogRepository.saveAll(newCatalogs);
            log.info("✅ {} new catalogs inserted.", newCatalogs.size());
        } else {
            log.info("ℹ No new catalogs to insert.");
        }
    }

}
