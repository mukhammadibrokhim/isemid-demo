package uz.uzinfocom.isemid.features.report.service.impls;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.isemid.commons.utils.SpecificationUtils;
import uz.uzinfocom.isemid.features.form058.reposiotries.Form058Repository;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.organization.models.enums.LevelType;
import uz.uzinfocom.isemid.features.organization.models.enums.MedicalType;
import uz.uzinfocom.isemid.features.report.dtos.Form8CityComparisonDto;
import uz.uzinfocom.isemid.features.report.dtos.Form8CitySummaryDto;
import uz.uzinfocom.isemid.features.report.dtos.Form8RegionComparisonDto;
import uz.uzinfocom.isemid.features.report.dtos.Form8RegionSummaryDto;
import uz.uzinfocom.isemid.features.report.dtos.Form8ReportDto;
import uz.uzinfocom.isemid.features.report.service.Form8ReportService;
import uz.uzinfocom.isemid.features.report.utils.Form8ReportHelper;
import uz.uzinfocom.isemid.features.user.models.User;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.springframework.util.StringUtils.hasText;

@Service
@RequiredArgsConstructor
public class Form8ReportServiceImpl implements Form8ReportService {
    private final Form058Repository form058Repository;
    private final Form8ReportHelper helper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public List<Form8RegionComparisonDto> getRegionCitySummary(
            LocalDate fromDate1,
            LocalDate toDate1,
            LocalDate fromDate2,
            LocalDate toDate2,
            String mkb10Code,
            User currentUser,
            String stateCode,
            String cityCode
    ) {
        Organization organization = requireCurrentOrganization(currentUser);
        SanepidAccessFilter accessFilter = resolveSanepidAccessFilter(organization);
        String lang = LocaleContextHolder.getLocale().getLanguage();

        /*
         * Nol natijali (formasi yo‘q) hudud/shaharlarni ham 0 bilan qaytarish uchun
         * zero-fill qamrovi: REPUBLICAN — butun respublika, qolgan barcha darajalar —
         * faqat foydalanuvchining o‘z viloyati (bu SANEPID cheklovidan mustaqil, har
         * qanday tashkilot turiga tegishli).
         */
        String enumerationStateCode = organization.getLevelType() == LevelType.REPUBLICAN
                ? null
                : organization.getStateCode();

        List<Form8RegionSummaryDto> firstPeriod = fetchRegionSummaries(
                fromDate1, toDate1, mkb10Code, lang, accessFilter, enumerationStateCode, stateCode, cityCode
        );

        List<Form8RegionSummaryDto> secondPeriod = fetchRegionSummaries(
                fromDate2, toDate2, mkb10Code, lang, accessFilter, enumerationStateCode, stateCode, cityCode
        );

        return mergeRegionSummaries(firstPeriod, secondPeriod);
    }

    private List<Form8RegionSummaryDto> fetchRegionSummaries(
            LocalDate fromDate,
            LocalDate toDate,
            String mkb10Code,
            String lang,
            SanepidAccessFilter accessFilter,
            String enumerationStateCode,
            String stateCode,
            String cityCode
    ) {
        Timestamp startDate = fromDate != null ? Timestamp.valueOf(fromDate.atStartOfDay()) : null;
        Timestamp endDate = toDate != null ? Timestamp.valueOf(toDate.atTime(LocalTime.MAX)) : null;

        List<Form058Repository.RegionCitySummaryProjection> rows = form058Repository.findRegionCitySummary(
                normalizeCode(mkb10Code),
                startDate,
                endDate,
                lang,
                normalizeCode(stateCode),
                normalizeCode(cityCode),
                accessFilter.stateCode(),
                accessFilter.cityCode(),
                accessFilter.levelTypes(),
                enumerationStateCode
        );

        List<Form8RegionSummaryDto> result = new ArrayList<>(rows.size());

        for (Form058Repository.RegionCitySummaryProjection row : rows) {
            result.add(buildRegionSummary(row));
        }

        return result;
    }

    private Form8RegionSummaryDto buildRegionSummary(
            Form058Repository.RegionCitySummaryProjection row
    ) {
        List<Form8CitySummaryDto> cityCategoryRows = parseCities(row.getCities());

        Map<String, String> cityNamesByCode = new LinkedHashMap<>();
        Map<String, Map<String, Long>> countsByCityCode = new LinkedHashMap<>();
        Map<String, Long> stateCounts = new HashMap<>();

        for (Form8CitySummaryDto cityCategoryRow : cityCategoryRows) {
            String cityCode = cityCategoryRow.getCityCode();

            cityNamesByCode.putIfAbsent(cityCode, cityCategoryRow.getCityName());

            countsByCityCode
                    .computeIfAbsent(cityCode, key -> new HashMap<>())
                    .merge(cityCategoryRow.getCategoryCode(), cityCategoryRow.getCount(), Long::sum);

            stateCounts.merge(cityCategoryRow.getCategoryCode(), cityCategoryRow.getCount(), Long::sum);
        }

        List<Form8ReportDto> cityStatistics = new ArrayList<>(countsByCityCode.size());

        for (Map.Entry<String, Map<String, Long>> entry : countsByCityCode.entrySet()) {
            cityStatistics.add(
                    helper.buildReportFromCategoryCounts(
                            cityNamesByCode.get(entry.getKey()),
                            entry.getValue()
                    )
            );
        }

        return Form8RegionSummaryDto.builder()
                .stateCode(row.getStateCode())
                .statistics(helper.buildReportFromCategoryCounts(row.getStateName(), stateCounts))
                .cities(cityStatistics)
                .build();
    }

    /**
     * Ikki davr natijasini stateCode bo‘yicha (va ichida shahar nomi bo‘yicha)
     * birlashtirib, har biri uchun birinchi/ikkinchi davr va farq (o‘sish/pasayish)
     * qiymatlarini quradi.
     */
    private List<Form8RegionComparisonDto> mergeRegionSummaries(
            List<Form8RegionSummaryDto> firstPeriod,
            List<Form8RegionSummaryDto> secondPeriod
    ) {
        Map<String, Form8RegionSummaryDto> firstByState = toMapByStateCode(firstPeriod);
        Map<String, Form8RegionSummaryDto> secondByState = toMapByStateCode(secondPeriod);

        Set<String> stateCodes = new LinkedHashSet<>();
        stateCodes.addAll(firstByState.keySet());
        stateCodes.addAll(secondByState.keySet());

        List<Form8RegionComparisonDto> result = new ArrayList<>(stateCodes.size());

        for (String stateCode : stateCodes) {
            Form8RegionSummaryDto first = firstByState.get(stateCode);
            Form8RegionSummaryDto second = secondByState.get(stateCode);

            String name = first != null
                    ? first.getStatistics().getName()
                    : second.getStatistics().getName();

            Form8ReportDto firstStats = first != null ? first.getStatistics() : helper.zero(name);
            Form8ReportDto secondStats = second != null ? second.getStatistics() : helper.zero(name);

            List<Form8CityComparisonDto> cities = mergeCities(
                    first != null ? first.getCities() : List.of(),
                    second != null ? second.getCities() : List.of()
            );

            result.add(
                    Form8RegionComparisonDto.builder()
                            .stateCode(stateCode)
                            .firstPeriod(firstStats)
                            .secondPeriod(secondStats)
                            .difference(helper.diff(firstStats, secondStats))
                            .cities(cities)
                            .build()
            );
        }

        return result;
    }

    private List<Form8CityComparisonDto> mergeCities(
            List<Form8ReportDto> firstCities,
            List<Form8ReportDto> secondCities
    ) {
        Map<String, Form8ReportDto> firstByName = toMapByName(firstCities);
        Map<String, Form8ReportDto> secondByName = toMapByName(secondCities);

        Set<String> names = new LinkedHashSet<>();
        names.addAll(firstByName.keySet());
        names.addAll(secondByName.keySet());

        List<Form8CityComparisonDto> result = new ArrayList<>(names.size());

        for (String name : names) {
            Form8ReportDto first = firstByName.containsKey(name) ? firstByName.get(name) : helper.zero(name);
            Form8ReportDto second = secondByName.containsKey(name) ? secondByName.get(name) : helper.zero(name);

            result.add(
                    Form8CityComparisonDto.builder()
                            .firstPeriod(first)
                            .secondPeriod(second)
                            .difference(helper.diff(first, second))
                            .build()
            );
        }

        return result;
    }

    private Map<String, Form8RegionSummaryDto> toMapByStateCode(List<Form8RegionSummaryDto> summaries) {
        Map<String, Form8RegionSummaryDto> map = new LinkedHashMap<>();

        for (Form8RegionSummaryDto summary : summaries) {
            map.put(summary.getStateCode(), summary);
        }

        return map;
    }

    private Map<String, Form8ReportDto> toMapByName(List<Form8ReportDto> reports) {
        Map<String, Form8ReportDto> map = new LinkedHashMap<>();

        for (Form8ReportDto report : reports) {
            map.put(report.getName(), report);
        }

        return map;
    }

    private List<Form8CitySummaryDto> parseCities(String citiesJson) {
        if (!hasText(citiesJson)) {
            return List.of();
        }

        try {
            return objectMapper.readValue(
                    citiesJson,
                    new TypeReference<List<Form8CitySummaryDto>>() {
                    }
            );
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "Failed to parse region summary cities JSON",
                    e
            );
        }
    }

    /**
     * {@link SpecificationUtils#addSanepidAccessPredicate} JPA Criteria orqali ishlaydi,
     * bu yerda esa native SQL uchun xuddi shu hudud cheklovi qiymatlarga aylantiriladi.
     */
    private SanepidAccessFilter resolveSanepidAccessFilter(Organization organization) {
        if (organization.getMedicalType() != MedicalType.SANEPID_SERVICE) {
            return SanepidAccessFilter.UNRESTRICTED;
        }

        String orgStateCode = organization.getStateCode();
        String orgCityCode = organization.getCityCode();

        return switch (organization.getLevelType()) {
            case REGIONAL -> new SanepidAccessFilter(orgStateCode, null, null);

            case URBAN -> "UZ-TK".equals(orgStateCode)
                    ? new SanepidAccessFilter(orgStateCode, null, null)
                    : new SanepidAccessFilter(null, orgCityCode, LevelType.URBAN.name());

            case AREA -> new SanepidAccessFilter(null, orgCityCode, LevelType.AREA.name());

            case DISTRICT, INTERDISTRICT, NOT_DEFINED -> new SanepidAccessFilter(
                    null,
                    orgCityCode,
                    String.join(
                            ",",
                            LevelType.DISTRICT.name(),
                            LevelType.INTERDISTRICT.name(),
                            LevelType.NOT_DEFINED.name()
                    )
            );

            default -> SanepidAccessFilter.UNRESTRICTED;
        };
    }

    private record SanepidAccessFilter(String stateCode, String cityCode, String levelTypes) {
        private static final SanepidAccessFilter UNRESTRICTED = new SanepidAccessFilter(null, null, null);
    }

    private String normalizeCode(String code) {
        if (!hasText(code)) {
            return null;
        }

        return code
                .trim()
                .toUpperCase(Locale.ROOT);
    }

    private Organization requireCurrentOrganization(User currentUser) {
        if (currentUser == null
                || currentUser.getCurrentOrganization() == null) {
            throw new IllegalStateException(
                    "Current user organization is required for Form 8 report"
            );
        }

        return currentUser.getCurrentOrganization();
    }
}
