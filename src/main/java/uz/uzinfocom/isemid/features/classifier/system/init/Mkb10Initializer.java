package uz.uzinfocom.isemid.features.classifier.system.init;

import com.opencsv.CSVReader;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import uz.uzinfocom.isemid.features.classifier.models.Mkb10;
import uz.uzinfocom.isemid.features.classifier.repositories.Mkb10Repository;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
@ConditionalOnProperty(name = "runner.mkb10.init", havingValue = "true")
public class Mkb10Initializer implements CommandLineRunner {
    private final Mkb10Repository mkb10Repository;
    private final ResourceLoader resourceLoader;

    @Override
    @SneakyThrows
    public void run(String... args) {
        log.info("📦 Starting initialization of MKB-10 data from CSV...");

        Resource resource = resourceLoader.getResource("classpath:/base/mkb10.csv");
        InputStreamReader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
        BufferedReader bufferedReader = new BufferedReader(reader);

        try (CSVReader csvReader = new CSVReader(bufferedReader)) {
            List<String[]> records = csvReader.readAll();
            log.info("✅ Loaded {} rows from CSV file (including header)", records.size());

            Set<String> allCodes = mkb10Repository.findAllCodes();
            log.info("📚 Found {} existing MKB-10 codes in the database", allCodes.size());

            Map<Long, Mkb10> mkb10Map = records.stream()
                    .skip(1) // Skip header row
                    .map(record -> Mkb10.builder()
                            .id(Long.parseLong(record[1]))
                            .code(record[2])
                            .isLastLevel(Boolean.parseBoolean(record[6]))
                            .level(Integer.parseInt(record[7]))
                            .nameKaa(record[8])
                            .nameRu(record[8])
                            .nameUz(record[10])
                            .nameUzCyril(record[11])
                            .parentId(record[12].isEmpty() ? null : Long.parseLong(record[12]))
                            .secondaryId(Long.parseLong(record[13]))
                            .usageLimit(Integer.parseInt(record[16]))
                            .build())
                    .filter(mkb10 -> !allCodes.contains(mkb10.getCode()))
                    .collect(Collectors.toMap(Mkb10::getId, mkb10 -> mkb10));

            if (mkb10Map.isEmpty()) {
                log.warn("⚠️ No new MKB-10 entries found to insert.");
            } else {
                mkb10Repository.saveAll(mkb10Map.values());
                log.info("✅ Inserted {} new MKB-10 entries into the database", mkb10Map.size());
            }

        } catch (Exception e) {
            log.error("❌ Failed to load MKB-10 data: {}", e.getMessage(), e);
            throw e; // let Spring fail fast if initialization fails
        }
    }
}
