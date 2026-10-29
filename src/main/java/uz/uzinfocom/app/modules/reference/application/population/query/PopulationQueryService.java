package uz.uzinfocom.app.modules.reference.application.population.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.modules.reference.application.lookup.ReferenceLookupService;
import uz.uzinfocom.app.modules.reference.application.population.query.dto.PopulationDetailResponse;
import uz.uzinfocom.app.modules.reference.application.population.query.dto.PopulationNodeResponse;
import uz.uzinfocom.app.modules.reference.application.population.query.dto.PopulationYearValueResponse;
import uz.uzinfocom.app.modules.reference.domain.Population;
import uz.uzinfocom.app.modules.reference.domain.enums.PopulationGeoType;
import uz.uzinfocom.app.modules.reference.repository.PopulationRepository;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.platform.persistence.audit.AuditResolver;
import uz.uzinfocom.app.shared.exception.NotFoundException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Read side of the population reference: a year-scoped
 * republic → region → district drill-down for the reference table, plus a
 * by-id detail carrying the territory's whole year series and the
 * create/update audit stamps.
 * <p>
 * Territory names are never stored on {@code ref_population} — they are
 * resolved here through {@link ReferenceLookupService} against {@code
 * ref_region}/{@code ref_district} in the caller's current locale.
 */
@Service
@RequiredArgsConstructor
public class PopulationQueryService {

    private final PopulationRepository populationRepository;
    private final ReferenceLookupService referenceLookupService;
    private final MessageResolver messageResolver;
    private final AuditResolver auditResolver;

    /**
     * Hierarchy one level deep for a year. No {@code regionCode} → the
     * republic total row followed by every region. With {@code regionCode} →
     * that region's districts. {@code year} defaults to the latest year
     * present (falling back to the current calendar year).
     */
    @Transactional(readOnly = true)
    public List<PopulationNodeResponse> hierarchy(String regionCode, Integer year) {
        int resolvedYear = resolveYear(year);

        if (StringUtils.hasText(regionCode)) {
            return populationRepository
                    .findAllByRegionCodeAndYearAndGeoTypeAndDeletedFalse(
                            regionCode.trim(), resolvedYear, PopulationGeoType.DISTRICT)
                    .stream()
                    .map(row -> toNode(row, false))
                    .sorted(Comparator.comparing(PopulationNodeResponse::name, String.CASE_INSENSITIVE_ORDER))
                    .toList();
        }

        List<Population> rows = populationRepository.findAllByYearAndGeoTypeInAndDeletedFalse(
                resolvedYear,
                List.of(PopulationGeoType.REPUBLIC, PopulationGeoType.REGION, PopulationGeoType.OTHER));

        List<PopulationNodeResponse> republic = rows.stream()
                .filter(row -> row.getGeoType() == PopulationGeoType.REPUBLIC)
                .map(row -> toNode(row, true))
                .toList();

        List<PopulationNodeResponse> regions = rows.stream()
                .filter(row -> row.getGeoType() == PopulationGeoType.REGION)
                .map(row -> toNode(row, true))
                .sorted(Comparator.comparing(PopulationNodeResponse::name, String.CASE_INSENSITIVE_ORDER))
                .toList();

        // SOATO territories the feed carries that match no active region/district
        // (geoType OTHER) belong nowhere in the strict tree — surface them after
        // the regions so a super-admin can see and fix them (assign a code via PUT).
        List<PopulationNodeResponse> other = rows.stream()
                .filter(row -> row.getGeoType() == PopulationGeoType.OTHER)
                .map(row -> toNode(row, false))
                .sorted(Comparator.comparing(PopulationNodeResponse::name, String.CASE_INSENSITIVE_ORDER))
                .toList();

        List<PopulationNodeResponse> result = new ArrayList<>(republic);
        result.addAll(regions);
        result.addAll(other);
        return result;
    }

    @Transactional(readOnly = true)
    public List<Integer> availableYears() {
        return populationRepository.findDistinctYears();
    }

    @Transactional(readOnly = true)
    public PopulationDetailResponse getById(Long id) {
        Population row = populationRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("reference.population.not_found_by_id", id));

        String regionCode = blankToNull(row.getRegionCode());
        String districtCode = blankToNull(row.getDistrictCode());
        String regionName = regionCode != null ? referenceLookupService.getRegionName(regionCode) : null;
        String districtName = districtCode != null ? referenceLookupService.getDistrictName(districtCode) : null;

        List<PopulationYearValueResponse> years = populationRepository
                .findAllBySoatoIdAndDeletedFalseOrderByYearDesc(row.getSoatoId())
                .stream()
                .map(r -> new PopulationYearValueResponse(r.getId(), r.getYear(), r.getPopulation(), r.getSource()))
                .toList();

        return new PopulationDetailResponse(
                row.getId(),
                row.getGeoType(),
                row.getSoatoId(),
                nodeCode(row),
                resolveName(row),
                regionCode,
                regionName,
                districtCode,
                districtName,
                row.getYear(),
                row.getPopulation(),
                row.getSource(),
                row.getDeleted(),
                years,
                auditResolver.resolve(row)
        );
    }

    private static String blankToNull(String value) {
        return StringUtils.hasText(value) ? value : null;
    }

    private PopulationNodeResponse toNode(Population row, boolean hasChildren) {
        return new PopulationNodeResponse(
                row.getId(),
                row.getGeoType(),
                row.getSoatoId(),
                nodeCode(row),
                resolveName(row),
                row.getYear(),
                row.getPopulation(),
                row.getSource(),
                hasChildren
        );
    }

    private String nodeCode(Population row) {
        return switch (row.getGeoType()) {
            case REPUBLIC -> "UZ";
            case REGION -> row.getRegionCode();
            case DISTRICT -> row.getDistrictCode();
            case OTHER -> String.valueOf(row.getSoatoId());
        };
    }

    private String resolveName(Population row) {
        return switch (row.getGeoType()) {
            case REPUBLIC -> messageResolver.resolve("report.scope.republic");
            case REGION -> referenceLookupService.getRegionName(row.getRegionCode());
            case DISTRICT -> referenceLookupService.getDistrictName(row.getDistrictCode());
            case OTHER -> String.valueOf(row.getSoatoId());
        };
    }

    private int resolveYear(Integer year) {
        if (year != null) {
            return year;
        }
        return populationRepository.findMaxYear().orElseGet(() -> LocalDate.now().getYear());
    }
}
