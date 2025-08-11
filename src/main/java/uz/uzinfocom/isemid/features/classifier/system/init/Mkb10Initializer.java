package uz.uzinfocom.isemid.features.classifier.system.init;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uz.uzinfocom.isemid.commons.seed.CsvLoaderUtil;
import uz.uzinfocom.isemid.commons.seed.ModuleDataInitializer;
import uz.uzinfocom.isemid.features.classifier.models.Mkb10;
import uz.uzinfocom.isemid.features.classifier.repositories.Mkb10Repository;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
@ConditionalOnProperty(name = "runner.mkb10.init", havingValue = "true")
public class Mkb10Initializer implements ModuleDataInitializer {

    private final Mkb10Repository mkb10Repository;
    private final CsvLoaderUtil csvLoaderUtil;

    @Override
    public void init() {
        log.info("♻️ Initializing default MKB-10 data...");

        List<String[]> rows = csvLoaderUtil.readCsv("classpath:/base/mkb10.csv");
        Set<String> existingCodes = mkb10Repository.findAllCodes();

        Map<Long, Mkb10> newMkb10Map = rows.stream()
                .skip(1) // Skip header
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
                .filter(mkb10 -> !existingCodes.contains(mkb10.getCode()))
                .collect(Collectors.toMap(Mkb10::getId, mkb10 -> mkb10));

        if (newMkb10Map.isEmpty()) {
            log.info("ℹ No new MKB-10 entries to insert.");
            return;
        }

        mkb10Repository.saveAll(newMkb10Map.values());
        log.info("✅ Inserted {} new MKB-10 entries.", newMkb10Map.size());
    }
}
