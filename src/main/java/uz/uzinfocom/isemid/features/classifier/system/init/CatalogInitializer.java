package uz.uzinfocom.isemid.features.classifier.system.init;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import uz.uzinfocom.isemid.commons.dtos.ApiException;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.classifier.models.Catalog;
import uz.uzinfocom.isemid.features.classifier.repositories.CatalogRepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
//@ConditionalOnProperty(name = "runner.catalog.init", havingValue = "true")
public class CatalogInitializer implements CommandLineRunner {

    private final ResourceLoader resourceLoader;
    private final CatalogRepository catalogRepository;
    private final MessageUtil messageUtil;

    @Override
    public void run(String... args) {
        log.info("♻️ Initializing default catalogs...");

        try {
            Resource resource = resourceLoader.getResource("classpath:/base/catalog.csv");

            if (!resource.exists()) {
                log.warn("⚠️ Catalog CSV resource not found.");
                return;
            }

            log.debug("📥 Reading file: {}", resource.getFilename());

            try (InputStreamReader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
                 BufferedReader bufferedReader = new BufferedReader(reader);
                 CSVReader csvReader = new CSVReader(bufferedReader)) {

                List<String[]> records = csvReader.readAll();
                log.debug("📊 Total records (including header): {}", records.size());

                Set<String> existingCodes = catalogRepository.findAllCodes();
                log.debug("📦 Existing catalog codes fetched: {}", existingCodes.size());

                List<Catalog> newCatalogs = records.stream()
                        .skip(1)
                        .map(record -> Catalog.builder()
                                .code(record[7])
                                .nameKaa(record[8])
                                .nameRu(record[9])
                                .nameUz(record[10])
                                .nameUzCyril(record[11])
                                .parentCode(record[12])
                                .type(record[13])
                                .build())
                        .filter(catalog -> !existingCodes.contains(catalog.getCode()))
                        .toList();

                log.debug("🆕 New catalogs prepared for insert: {}", newCatalogs.size());

                if (!newCatalogs.isEmpty()) {
                    catalogRepository.saveAll(newCatalogs);
                    log.info("✅ {} new catalogs inserted successfully.", newCatalogs.size());
                } else {
                    log.info("ℹ️ No new catalogs to insert.");
                }

            }
        } catch (IOException | CsvException e) {
            log.error("❌ Failed to initialize catalogs: {}", e.getMessage(), e);
            throw new ApiException(INTERNAL_SERVER_ERROR, messageUtil.get("message.failed"), e);
        }
    }
}
