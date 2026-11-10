package uz.uzinfocom.app.modules.reference.application.geosync;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;
import uz.uzinfocom.app.modules.reference.application.common.event.DistrictChangedEvent;
import uz.uzinfocom.app.modules.reference.application.common.event.NeighborhoodChangedEvent;
import uz.uzinfocom.app.modules.reference.application.geosync.dto.DistrictSyncResult;
import uz.uzinfocom.app.modules.reference.application.geosync.dto.EgovNeighborhoodRow;
import uz.uzinfocom.app.modules.reference.application.geosync.dto.EgovSoatoRow;
import uz.uzinfocom.app.modules.reference.application.geosync.dto.NeighborhoodSyncResult;
import uz.uzinfocom.app.modules.reference.domain.District;
import uz.uzinfocom.app.modules.reference.domain.Neighborhood;
import uz.uzinfocom.app.modules.reference.domain.Region;
import uz.uzinfocom.app.modules.reference.domain.enums.AdministrativeType;
import uz.uzinfocom.app.modules.reference.repository.DistrictRepository;
import uz.uzinfocom.app.modules.reference.repository.NeighborhoodRepository;
import uz.uzinfocom.app.modules.reference.repository.RegionRepository;
import uz.uzinfocom.app.shared.exception.ConflictException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * Brings {@code ref_district} and {@code ref_neighborhood} in line with the e-government
 * classifier registry (cs.egov.uz). The two are separate operations (separate endpoints): run
 * the district sync first when new districts may exist, because a neighborhood is only attached
 * to a district that is already active in our table.
 * <ul>
 *   <li>Districts (key {@code soato_id}): active -> left alone; soft-deleted -> reactivated;
 *       missing -> created (egov only knows the Russian name).</li>
 *   <li>Neighborhoods (key TIN, fallback {@code soato_id} = uzcad code): found -> reactivated if
 *       soft-deleted and any differing field (names, TIN, soato_id, 1C code, parent district)
 *       updated; missing -> created with {@code code = parentCode + 3-digit sequence}, the
 *       project's existing convention. Rows only we have are never touched.</li>
 * </ul>
 * The HTTP fetch runs before (and outside) the database transaction, so a fetch failure touches
 * nothing and no connection is held during the call. A dry run executes the same logic and then
 * rolls the transaction back, so its counts are exactly what a real run would do. One sync of
 * either kind runs at a time.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GeoReferenceSyncService {

    private static final int SEQUENCE_WIDTH = 3;
    private static final Pattern SEQUENCE_SUFFIX = Pattern.compile("\\d{" + SEQUENCE_WIDTH + "}");
    private static final int INVALID_TIN_REPORT_LIMIT = 100;

    private final EgovClassifierClient egovClassifierClient;
    private final RegionRepository regionRepository;
    private final DistrictRepository districtRepository;
    private final NeighborhoodRepository neighborhoodRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final TransactionTemplate transactionTemplate;

    private final ReentrantLock lock = new ReentrantLock();

    public DistrictSyncResult syncDistricts(boolean dryRun) {
        return runExclusively(() -> {
            List<EgovSoatoRow> soatoRows = egovClassifierClient.fetchSoato();

            return transactionTemplate.execute(status -> {
                DistrictPhase phase = applyDistricts(soatoRows, dryRun);
                persistOrRollback(status, dryRun, () -> {
                    districtRepository.saveAll(phase.toSave());
                    if (!phase.toSave().isEmpty()) {
                        eventPublisher.publishEvent(new DistrictChangedEvent());
                    }
                });
                DistrictSyncResult result = phase.result();
                log.info("District sync finished. dryRun={}, received={}, created={}, reactivated={}, unchanged={}",
                        dryRun, result.received(), result.created(), result.reactivated(), result.unchanged());
                return result;
            });
        });
    }

    public NeighborhoodSyncResult syncNeighborhoods(boolean dryRun) {
        return runExclusively(() -> {
            List<EgovNeighborhoodRow> rows = egovClassifierClient.fetchNeighborhoods();

            return transactionTemplate.execute(status -> {
                NeighborhoodPhase phase = applyNeighborhoods(rows, activeDistrictsBySoato(), dryRun);
                persistOrRollback(status, dryRun, () -> {
                    neighborhoodRepository.saveAll(phase.toSave());
                    if (!phase.toSave().isEmpty()) {
                        eventPublisher.publishEvent(new NeighborhoodChangedEvent());
                    }
                });
                NeighborhoodSyncResult result = phase.result();
                log.info("Neighborhood sync finished. dryRun={}, received={}, created={}, reactivated={}, moved={}, "
                                + "fieldsUpdated={}, unchanged={}, skipped={}",
                        dryRun, result.received(), result.created(), result.reactivated(), result.moved(),
                        result.fieldsUpdated(), result.unchanged(),
                        result.skippedInvalid() + result.skippedUnknownDistrict() + result.skippedDuplicate());
                return result;
            });
        });
    }

    private <T> T runExclusively(Supplier<T> action) {
        if (!lock.tryLock()) {
            throw new ConflictException("reference.geo_sync.in_progress");
        }
        try {
            return action.get();
        } finally {
            lock.unlock();
        }
    }

    /** A dry run discards everything (including in-memory changes to managed entities) by rolling back. */
    private static void persistOrRollback(TransactionStatus status, boolean dryRun, Runnable persist) {
        if (dryRun) {
            status.setRollbackOnly();
        } else {
            persist.run();
        }
    }

    // ---------------------------------------------------------------- districts

    private DistrictPhase applyDistricts(List<EgovSoatoRow> soatoRows, boolean dryRun) {
        Map<Integer, Region> regionBySoato = new HashMap<>();
        for (Region region : regionRepository.findAllByDeletedFalseOrderByNameUzAsc()) {
            regionBySoato.putIfAbsent(region.getSoatoId(), region);
        }

        Map<Integer, District> bySoato = new HashMap<>();
        Set<String> usedCodes = new HashSet<>();
        for (District district : districtRepository.findAll()) {
            usedCodes.add(district.getCode());
            if (district.getSoatoId() != null && district.getSoatoId() > 0) {
                bySoato.merge(district.getSoatoId(), district, GeoReferenceSyncService::preferActive);
            }
        }

        List<EgovSoatoRow> districtRows = soatoRows.stream()
                .filter(row -> row != null && row.id() != null && row.id().trim().matches("\\d{7}"))
                .sorted(Comparator.comparing(row -> row.id().trim()))
                .toList();

        List<District> toSave = new ArrayList<>();
        List<DistrictSyncResult.CreatedDistrict> created = new ArrayList<>();
        List<String> reactivated = new ArrayList<>();
        List<DistrictSyncResult.SkippedDistrict> skipped = new ArrayList<>();
        int unchanged = 0;

        for (EgovSoatoRow row : districtRows) {
            String id = row.id().trim();
            int soatoId = Integer.parseInt(id);
            District existing = bySoato.get(soatoId);

            if (existing != null) {
                if (existing.isDeleted()) {
                    existing.setDeleted(false);
                    toSave.add(existing);
                    reactivated.add(existing.getCode());
                } else {
                    unchanged++;
                }
                continue;
            }

            Region region = regionBySoato.get(Integer.parseInt(id.substring(0, 4)));
            if (region == null) {
                skipped.add(new DistrictSyncResult.SkippedDistrict(id, "region_not_found"));
                continue;
            }
            String code = newDistrictCode(region, soatoId);
            if (code == null || usedCodes.contains(code)) {
                skipped.add(new DistrictSyncResult.SkippedDistrict(id, "code_conflict"));
                continue;
            }

            String nameRu = trimOrNull(row.nameRu());
            District district = District.builder()
                    .code(code)
                    .parentCode(region.getCode())
                    .soatoId(soatoId)
                    .parentSoatoId(region.getSoatoId())
                    .legacySoatoId(soatoId)
                    .parentLegacySoatoId(region.getLegacySoatoId())
                    .administrativeType(administrativeType(nameRu))
                    // egov publishes only the Russian name; the rest are placeholders to translate.
                    .nameUz(nameRu)
                    .nameUzCyril(nameRu)
                    .nameRu(nameRu)
                    .nameKaa(nameRu)
                    .deleted(false)
                    .build();
            toSave.add(district);
            usedCodes.add(code);
            bySoato.put(soatoId, district);
            created.add(new DistrictSyncResult.CreatedDistrict(code, soatoId, nameRu));
        }

        return new DistrictPhase(toSave, new DistrictSyncResult(
                dryRun, districtRows.size(), created.size(), reactivated.size(), unchanged,
                created, reactivated, skipped));
    }

    /** {@code UZ-AN} + {@code 1703202} -> {@code AN-202}, the shape every seeded district code has. */
    private static String newDistrictCode(Region region, int soatoId) {
        String regionCode = region.getCode();
        int dash = regionCode == null ? -1 : regionCode.indexOf('-');
        if (dash < 0 || dash == regionCode.length() - 1) {
            return null;
        }
        return regionCode.substring(dash + 1) + "-" + String.format("%03d", soatoId % 1000);
    }

    private static AdministrativeType administrativeType(String nameRu) {
        return nameRu != null && nameRu.toLowerCase(Locale.ROOT).contains("город")
                ? AdministrativeType.CITY
                : AdministrativeType.DISTRICT;
    }

    // ------------------------------------------------------------- neighborhoods

    /**
     * Our active districts by SOATO id, plus short-code aliases. egov's neighborhood classifier
     * names the 2023 Namangan-city districts {@code 1714365}/{@code 1714367} (region + last three
     * digits) while we store them as {@code 1714401365}/{@code 1714401367} (region + city 401 +
     * last three digits). A 10-digit district is therefore also reachable under that 7-digit key,
     * but only when no real district owns the key and the key is unambiguous.
     */
    private Map<Integer, District> activeDistrictsBySoato() {
        Map<Integer, District> bySoato = new HashMap<>();
        for (District district : districtRepository.findAll()) {
            if (!district.isDeleted() && district.getSoatoId() != null && district.getSoatoId() > 0) {
                bySoato.putIfAbsent(district.getSoatoId(), district);
            }
        }

        Map<Integer, District> aliases = new HashMap<>();
        Set<Integer> ambiguous = new HashSet<>();
        for (District district : bySoato.values()) {
            Integer alias = shortSoatoAlias(district.getSoatoId());
            if (alias == null || bySoato.containsKey(alias)) {
                continue;
            }
            if (aliases.putIfAbsent(alias, district) != null) {
                ambiguous.add(alias);
            }
        }
        aliases.keySet().removeAll(ambiguous);
        aliases.forEach(bySoato::putIfAbsent);
        return bySoato;
    }

    /** {@code 1714401365} -> {@code 1714365}; null unless the id has 10 digits. */
    private static Integer shortSoatoAlias(int soatoId) {
        String digits = Integer.toString(soatoId);
        return digits.length() == 10 ? Integer.parseInt(digits.substring(0, 4) + digits.substring(7)) : null;
    }

    private NeighborhoodPhase applyNeighborhoods(
            List<EgovNeighborhoodRow> rows, Map<Integer, District> districtBySoato, boolean dryRun
    ) {
        List<Neighborhood> existingAll = neighborhoodRepository.findAll();

        Map<String, Neighborhood> byTin = new HashMap<>();
        Map<Integer, Neighborhood> bySoato = new HashMap<>();
        Set<String> usedCodes = new HashSet<>();
        Map<String, Integer> maxSequenceByParent = new HashMap<>();
        for (Neighborhood neighborhood : existingAll) {
            usedCodes.add(neighborhood.getCode());
            if (neighborhood.getTin() != null && !neighborhood.getTin().isBlank()) {
                byTin.merge(neighborhood.getTin().trim(), neighborhood, GeoReferenceSyncService::preferActive);
            }
            if (neighborhood.getSoatoId() != null && neighborhood.getSoatoId() > 0) {
                bySoato.merge(neighborhood.getSoatoId(), neighborhood, GeoReferenceSyncService::preferActive);
            }
            trackSequence(neighborhood, maxSequenceByParent);
        }

        // TIN order = the order the original migration numbered new neighborhoods in.
        List<EgovNeighborhoodRow> ordered = rows.stream()
                .filter(row -> row != null)
                .sorted(Comparator.comparing(row -> row.tin() == null ? "" : row.tin().trim()))
                .toList();

        Set<Long> handledIds = new HashSet<>();
        List<Neighborhood> toSave = new ArrayList<>();
        Map<Integer, Integer> unresolvedDistricts = new TreeMap<>();
        Map<Integer, Integer> aliasUsage = new TreeMap<>();
        Map<Integer, District> aliasTargets = new HashMap<>();
        List<String> invalidTins = new ArrayList<>();

        int created = 0;
        int reactivated = 0;
        int moved = 0;
        int fieldsUpdated = 0;
        int unchanged = 0;
        int skippedInvalid = 0;
        int skippedUnknownDistrict = 0;
        int skippedDuplicate = 0;

        for (EgovNeighborhoodRow row : ordered) {
            String tin = trimOrNull(row.tin());
            Integer uzcad = parsePositiveInt(row.uzcadCode());
            Integer districtSoato = parsePositiveInt(row.districtSoato());
            if (tin == null || uzcad == null || districtSoato == null) {
                skippedInvalid++;
                if (tin != null && invalidTins.size() < INVALID_TIN_REPORT_LIMIT) {
                    invalidTins.add(tin);
                }
                continue;
            }

            District district = districtBySoato.get(districtSoato);
            if (district == null) {
                skippedUnknownDistrict++;
                unresolvedDistricts.merge(districtSoato, 1, Integer::sum);
                continue;
            }
            // The district's own SOATO id is what parent_soato_id stores (egov may have sent the short alias).
            int parentSoatoId = district.getSoatoId();
            if (parentSoatoId != districtSoato) {
                aliasUsage.merge(districtSoato, 1, Integer::sum);
                aliasTargets.put(districtSoato, district);
            }

            Neighborhood existing = byTin.get(tin);
            if (existing == null) {
                existing = bySoato.get(uzcad);
            }

            if (existing == null) {
                Neighborhood neighborhood = Neighborhood.builder()
                        .code(nextCode(district.getCode(), maxSequenceByParent, usedCodes))
                        .parentCode(district.getCode())
                        .soatoId(uzcad)
                        .parentSoatoId(parentSoatoId)
                        .tin(tin)
                        .uzcadRegistryCode(trimOrNull(row.code()))
                        .nameUz(trimOrNull(row.nameUzLatin()))
                        .nameUzCyril(trimOrNull(row.nameUzCyril()))
                        .nameRu(trimOrNull(row.nameRu()))
                        .deleted(false)
                        .build();
                toSave.add(neighborhood);
                // Later rows of this same run can now find it (as a duplicate).
                byTin.put(tin, neighborhood);
                bySoato.put(uzcad, neighborhood);
                created++;
                continue;
            }

            // Unsaved id == a neighborhood created earlier in this run matched again (same TIN twice).
            if (existing.getId() == null || !handledIds.add(existing.getId())) {
                skippedDuplicate++;
                continue;
            }

            boolean reactivatedNow = false;
            if (existing.isDeleted()) {
                existing.setDeleted(false);
                reactivatedNow = true;
                reactivated++;
            }

            boolean movedNow = !district.getCode().equals(existing.getParentCode())
                    || !Integer.valueOf(parentSoatoId).equals(existing.getParentSoatoId());
            if (movedNow) {
                existing.setParentCode(district.getCode());
                existing.setParentSoatoId(parentSoatoId);
                moved++;
            }

            boolean fieldsNow = false;
            if (!tin.equals(trimOrNull(existing.getTin()))) {
                existing.setTin(tin);
                fieldsNow = true;
            }
            if (!uzcad.equals(existing.getSoatoId())) {
                existing.setSoatoId(uzcad);
                fieldsNow = true;
            }
            fieldsNow |= overwrite(trimOrNull(row.code()), existing.getUzcadRegistryCode(), existing::setUzcadRegistryCode);
            fieldsNow |= overwrite(trimOrNull(row.nameUzLatin()), existing.getNameUz(), existing::setNameUz);
            fieldsNow |= overwrite(trimOrNull(row.nameUzCyril()), existing.getNameUzCyril(), existing::setNameUzCyril);
            fieldsNow |= overwrite(trimOrNull(row.nameRu()), existing.getNameRu(), existing::setNameRu);

            if (fieldsNow) {
                fieldsUpdated++;
            }
            if (reactivatedNow || movedNow || fieldsNow) {
                toSave.add(existing);
            } else {
                unchanged++;
            }
        }

        List<NeighborhoodSyncResult.UnresolvedDistrict> unresolved = unresolvedDistricts.entrySet().stream()
                .map(entry -> new NeighborhoodSyncResult.UnresolvedDistrict(entry.getKey(), entry.getValue()))
                .toList();

        List<NeighborhoodSyncResult.AliasedDistrict> aliased = aliasUsage.entrySet().stream()
                .map(entry -> {
                    District target = aliasTargets.get(entry.getKey());
                    return new NeighborhoodSyncResult.AliasedDistrict(
                            entry.getKey(), target.getCode(), target.getSoatoId(), entry.getValue());
                })
                .toList();

        return new NeighborhoodPhase(toSave, new NeighborhoodSyncResult(
                dryRun, ordered.size(), created, reactivated, moved, fieldsUpdated, unchanged,
                skippedInvalid, skippedUnknownDistrict, skippedDuplicate, unresolved, aliased, invalidTins));
    }

    private static void trackSequence(Neighborhood neighborhood, Map<String, Integer> maxSequenceByParent) {
        String parent = neighborhood.getParentCode();
        String code = neighborhood.getCode();
        if (parent == null || code == null || !code.startsWith(parent)) {
            return;
        }
        String suffix = code.substring(parent.length());
        // Exactly 3 digits: "NG-401" + "054". A longer tail belongs to a different district whose
        // own code merely starts with this one ("NG-401365" + "001").
        if (SEQUENCE_SUFFIX.matcher(suffix).matches()) {
            maxSequenceByParent.merge(parent, Integer.parseInt(suffix), Math::max);
        }
    }

    private static String nextCode(String parentCode, Map<String, Integer> maxSequenceByParent, Set<String> usedCodes) {
        int sequence = maxSequenceByParent.getOrDefault(parentCode, 0);
        String candidate;
        do {
            sequence++;
            candidate = parentCode + String.format("%0" + SEQUENCE_WIDTH + "d", sequence);
        } while (usedCodes.contains(candidate));
        maxSequenceByParent.put(parentCode, sequence);
        usedCodes.add(candidate);
        return candidate;
    }

    // ------------------------------------------------------------------- helpers

    /** Replaces {@code current} with {@code incoming} when egov has a value and it differs. */
    private static boolean overwrite(String incoming, String current, Consumer<String> setter) {
        if (incoming == null || incoming.equals(trimOrNull(current))) {
            return false;
        }
        setter.accept(incoming);
        return true;
    }

    private static District preferActive(District first, District second) {
        return first.isDeleted() && !second.isDeleted() ? second : first;
    }

    private static Neighborhood preferActive(Neighborhood first, Neighborhood second) {
        return first.isDeleted() && !second.isDeleted() ? second : first;
    }

    private static Integer parsePositiveInt(String value) {
        if (value == null) {
            return null;
        }
        try {
            long parsed = Long.parseLong(value.trim());
            return parsed > 0 && parsed <= Integer.MAX_VALUE ? (int) parsed : null;
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private static String trimOrNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private record DistrictPhase(List<District> toSave, DistrictSyncResult result) {
    }

    private record NeighborhoodPhase(List<Neighborhood> toSave, NeighborhoodSyncResult result) {
    }
}
