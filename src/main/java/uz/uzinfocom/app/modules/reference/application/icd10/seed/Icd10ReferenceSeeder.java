package uz.uzinfocom.app.modules.reference.application.icd10.seed;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.reference.domain.Icd10;
import uz.uzinfocom.app.modules.reference.repository.Icd10Repository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Bulk-loads the ICD-10 hierarchy from a bundled CSV export the first time
 * the application starts against an empty {@code ref_icd10} table. Mirrors
 * the intent of {@code 20260605-1800-seed-reference-data.xml} (which seeds
 * country/region/district/neighborhood/catalog the same way) as a
 * Spring-native equivalent for this table.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class Icd10ReferenceSeeder implements ApplicationRunner {

    private static final String SEED_RESOURCE = "base/mkb10_reference.csv";

    private final Icd10Repository icd10Repository;

    @Override
    @Transactional
    public void run(@NonNull ApplicationArguments args) throws IOException {
        if (icd10Repository.count() > 0) {
            return;
        }

        List<Icd10Row> rows = readRows();
        List<Icd10> entities = new ArrayList<>();

        for (Icd10Row row : rows) {
            entities.add(row.toEntity());
        }

        // First pass: insert every node without its parent link, so no row's
        // insert can ever depend on another row already existing — avoids
        // any FK-ordering assumption about the source file's row order.
        //
        // ExternallyIdentifiedEntity's @Version field defaults to 0L (not
        // null), so Spring Data JPA's isNew() check treats every freshly
        // built entity as "existing" and routes save() through merge()
        // rather than persist(). merge() returns a *different*, separately
        // managed instance — mutating the pre-save `entities` objects after
        // this point would silently never get flushed. The returned list is
        // the one Hibernate is actually tracking.
        List<Icd10> saved = icd10Repository.saveAllAndFlush(entities);
        Map<Long, Icd10> byId = saved.stream().collect(Collectors.toMap(Icd10::getId, entity -> entity));

        // Second pass: every id is now a real row, so wire up parent links.
        // Entities are the managed instances from the flush above, so this
        // is a plain dirty-checked UPDATE at transaction commit.
        for (Icd10Row row : rows) {
            if (row.parentId() == null) {
                continue;
            }
            Icd10 parent = byId.get(row.parentId());
            if (parent != null) {
                byId.get(row.id()).setParent(parent);
            }
        }

        log.info("Seeded {} MKB-10 reference nodes from {}", entities.size(), SEED_RESOURCE);
    }

    private List<Icd10Row> readRows() throws IOException {
        List<Icd10Row> rows = new ArrayList<>();
        ClassPathResource resource = new ClassPathResource(SEED_RESOURCE);

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            reader.readLine(); // header

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                rows.add(Icd10Row.parse(splitCsvLine(line)));
            }
        }

        return rows;
    }

    /**
     * Minimal RFC4180-style CSV line splitter: handles double-quoted fields
     * containing commas or escaped ({@code ""}) double quotes — MKB-10 names
     * legitimately contain both (e.g. "Cholera, unspecified").
     */
    static List<String> splitCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        field.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    field.append(c);
                }
            } else if (c == '"') {
                inQuotes = true;
            } else if (c == ',') {
                fields.add(field.toString());
                field.setLength(0);
            } else {
                field.append(c);
            }
        }
        fields.add(field.toString());
        return fields;
    }

    record Icd10Row(
            Long id,
            UUID uuid,
            Long parentId,
            Long secondaryId,
            String code,
            int level,
            boolean lastLevel,
            int usageLimit,
            String nameUz,
            String nameUzCyril,
            String nameRu,
            String nameKaa,
            String comment
    ) {
        static Icd10Row parse(List<String> f) {
            return new Icd10Row(
                    Long.parseLong(f.get(0)),
                    UUID.fromString(f.get(1)),
                    f.get(2).isEmpty() ? null : Long.parseLong(f.get(2)),
                    f.get(3).isEmpty() ? null : Long.parseLong(f.get(3)),
                    f.get(4),
                    Integer.parseInt(f.get(5)),
                    Boolean.parseBoolean(f.get(6)),
                    Integer.parseInt(f.get(7)),
                    f.get(8),
                    f.get(9),
                    f.get(10),
                    f.get(11),
                    f.get(12).isEmpty() ? null : f.get(12)
            );
        }

        Icd10 toEntity() {
            return Icd10.builder()
                    .id(id)
                    .uuid(uuid)
                    .secondaryId(secondaryId)
                    .code(code)
                    .level(level)
                    .lastLevel(lastLevel)
                    .usageLimit(usageLimit)
                    .nameUz(nameUz)
                    .nameUzCyril(nameUzCyril)
                    .nameRu(nameRu)
                    .nameKaa(nameKaa)
                    .comment(comment)
                    .deleted(false)
                    .build();
        }
    }
}
