package uz.uzinfocom.isemid.features.report.service.impls;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.commons.utils.SpecificationUtils;
import uz.uzinfocom.isemid.features.classifier.utils.CatalogLocaleUtil;
import uz.uzinfocom.isemid.features.form058.reposiotries.Form058Repository;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.organization.models.enums.LevelType;
import uz.uzinfocom.isemid.features.organization.models.enums.MedicalType;
import uz.uzinfocom.isemid.features.report.dtos.Form8CitySummaryDto;
import uz.uzinfocom.isemid.features.report.dtos.Form8RegionComparisonDto;
import uz.uzinfocom.isemid.features.report.dtos.Form8ReportDto;
import uz.uzinfocom.isemid.features.report.service.Form8ReportService;
import uz.uzinfocom.isemid.features.report.utils.Form8ReportHelper;
import uz.uzinfocom.isemid.features.user.models.User;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.springframework.util.StringUtils.hasText;

@Service
@RequiredArgsConstructor
public class Form8ReportServiceImpl implements Form8ReportService {
    private final Form058Repository form058Repository;
    private final Form8ReportHelper helper;
    private final ObjectMapper objectMapper;
    private final MessageUtil messageUtil;

    @Override
    @Transactional(readOnly = true)
    public Form8RegionComparisonDto getRegionCitySummary(
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

        List<Form058Repository.RegionCitySummaryProjection> firstRows = fetchRows(
                fromDate1, toDate1, mkb10Code, lang, accessFilter, enumerationStateCode, stateCode, cityCode
        );

        List<Form058Repository.RegionCitySummaryProjection> secondRows = fetchRows(
                fromDate2, toDate2, mkb10Code, lang, accessFilter, enumerationStateCode, stateCode, cityCode
        );

        Map<String, Long> firstPeriodCounts = aggregateCounts(firstRows);
        Map<String, Long> secondPeriodCounts = aggregateCounts(secondRows);

        String name = resolveDisplayName(organization, stateCode, cityCode, firstRows, secondRows);

        Form8ReportDto firstStats = helper.buildReportFromCategoryCounts(name, firstPeriodCounts);
        Form8ReportDto secondStats = helper.buildReportFromCategoryCounts(name, secondPeriodCounts);

        return Form8RegionComparisonDto.builder()
                .firstPeriod(firstStats)
                .secondPeriod(secondStats)
                .difference(helper.diff(firstStats, secondStats))
                .build();
    }

    private List<Form058Repository.RegionCitySummaryProjection> fetchRows(
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

        return form058Repository.findRegionCitySummary(
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
    }

    /**
     * Berilgan davr uchun barcha hudud/shaharlar bo‘yicha (mavjud access
     * cheklovlari doirasida) qaytgan qatorlarni bitta categoryCode -> son
     * xaritasiga birlashtiradi.
     */
    private Map<String, Long> aggregateCounts(List<Form058Repository.RegionCitySummaryProjection> rows) {
        Map<String, Long> counts = new HashMap<>();

        for (Form058Repository.RegionCitySummaryProjection row : rows) {
            for (Form8CitySummaryDto cityCategoryRow : parseCities(row.getCities())) {
                counts.merge(cityCategoryRow.getCategoryCode(), cityCategoryRow.getCount(), Long::sum);
            }
        }

        return counts;
    }

    /**
     * Natijadagi umumlashtirilgan hisobotning nomi.
     * Foydalanuvchi cityCode bo‘yicha filtr bergan bo‘lsa — o‘sha shahar/tuman nomi,
     * faqat stateCode bergan bo‘lsa — o‘sha viloyat nomi qaytadi. Hech qanday filtr
     * berilmagan bo‘lsa (standart holat), joriy tashkilot darajasiga mos hudud nomi
     * (respublika/viloyat/shahar-tuman) ishlatiladi.
     */
    private String resolveDisplayName(
            Organization organization,
            String stateCode,
            String cityCode,
            List<Form058Repository.RegionCitySummaryProjection> firstRows,
            List<Form058Repository.RegionCitySummaryProjection> secondRows
    ) {
        if (hasText(cityCode)) {
            String resolvedCityName = resolveCityName(firstRows);

            if (!hasText(resolvedCityName)) {
                resolvedCityName = resolveCityName(secondRows);
            }

            if (hasText(resolvedCityName)) {
                return resolvedCityName;
            }
        } else if (hasText(stateCode)) {
            String resolvedStateName = resolveStateName(firstRows);

            if (!hasText(resolvedStateName)) {
                resolvedStateName = resolveStateName(secondRows);
            }

            if (hasText(resolvedStateName)) {
                return resolvedStateName;
            }
        }

        return resolveOrganizationDisplayName(organization);
    }

    private String resolveStateName(List<Form058Repository.RegionCitySummaryProjection> rows) {
        return rows.isEmpty() ? null : rows.get(0).getStateName();
    }

    private String resolveCityName(List<Form058Repository.RegionCitySummaryProjection> rows) {
        for (Form058Repository.RegionCitySummaryProjection row : rows) {
            for (Form8CitySummaryDto cityCategoryRow : parseCities(row.getCities())) {
                if (hasText(cityCategoryRow.getCityName())) {
                    return cityCategoryRow.getCityName();
                }
            }
        }

        return null;
    }

    /**
     * Natijadagi umumlashtirilgan hisobotning standart nomi — joriy foydalanuvchi
     * tashkilotining darajasiga mos hudud nomi (respublika/viloyat/shahar-tuman).
     */
    private String resolveOrganizationDisplayName(Organization organization) {
        return switch (organization.getLevelType()) {
            case REPUBLICAN -> messageUtil.get("country.name");
            case REGIONAL -> CatalogLocaleUtil.displayName(organization.getState());
            case URBAN, AREA, DISTRICT, INTERDISTRICT, NOT_DEFINED ->
                    CatalogLocaleUtil.displayName(organization.getCity());
        };
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
