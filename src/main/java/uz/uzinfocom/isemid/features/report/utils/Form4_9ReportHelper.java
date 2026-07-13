package uz.uzinfocom.isemid.features.report.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import uz.uzinfocom.isemid.features.classifier.eums.CatalogType;
import uz.uzinfocom.isemid.features.classifier.models.Catalog;
import uz.uzinfocom.isemid.features.classifier.services.CatalogService;
import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.report.dtos.Form4_9ReportDto;

import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.util.StringUtils.hasText;

@Component
@RequiredArgsConstructor
public class Form4_9ReportHelper {

    private static final String CATEGORY_NO_ORGANIZED = "NO_ORGANIZED";
    private static final String CATEGORY_NOT_ORGANIZED = "NOT_ORGANIZED";
    private static final String CATEGORY_ORGANIZED = "ORGANIZED";
    private static final String CATEGORY_STUDENT_SCHOOL = "STUDENT_SCHOOL";
    private static final String CATEGORY_MIDDLE_STUDENT = "MIDDLE_STUDENT";
    private static final String CATEGORY_SECONDARY_STUDENT = "SECONDARY_STUDENT";
    private static final String CATEGORY_STUDENT = "STUDENT";
    private static final String CATEGORY_WORKER = "WORKER";
    private static final String CATEGORY_MEDICAL_WORKER = "MEDICAL_WORKER";
    private static final String CATEGORY_NOT_EMPLOYED = "NOT_EMPLOYED";
    private static final String CATEGORY_PENSIONER = "PENSIONER";
    private static final String CATEGORY_UNSHELTRED = "UNSHELTRED";
    private static final String CATEGORY_UNSHELTERED = "UNSHELTERED";
    private static final String CATEGORY_STAFF = "STAFF";

    private final CatalogService catalogService;

    // ============================================================
    // PUBLIC ENTRY POINTS
    // ============================================================

    public Object buildByParentCode(
            String parentCode,
            List<Form058> forms
    ) {
        String normalizedParentCode = normalizeCode(parentCode);

        if (!hasText(normalizedParentCode)) {
            return List.of();
        }

        List<Form058> safeForms = nullSafeForms(forms);
        List<Catalog> children = nullSafeCatalogs(
                catalogService.findAllByParentCode(normalizedParentCode)
        );

        boolean containsNeighborhoods = children.stream()
                .filter(Objects::nonNull)
                .map(Catalog::getType)
                .anyMatch(CatalogType.neighborhood.name()::equals);

        /*
         * Neighborhood darajasidan keyingi hisobot
         * tashkilotlar kesimida quriladi.
         */
        if (containsNeighborhoods) {
            return buildOrganizations(
                    normalizedParentCode,
                    safeForms
            );
        }

        List<Form4_9ReportDto> reports = new ArrayList<>(children.size());

        for (Catalog child : children) {
            if (child == null) {
                continue;
            }

            String childCode = normalizeCode(child.getCode());

            List<Form058> matchedForms = filterByLocation(
                    safeForms,
                    childCode
            );

            reports.add(
                    buildReport(
                            getDisplay(child),
                            matchedForms
                    )
            );
        }

        return reports;
    }

    public Object buildRegionReports(List<Form058> forms) {
        List<Form058> safeForms = nullSafeForms(forms);
        List<Catalog> regions = nullSafeCatalogs(
                catalogService.findAllByType(CatalogType.state)
        );

        List<Form4_9ReportDto> reports = new ArrayList<>(regions.size());

        for (Catalog region : regions) {
            if (region == null) {
                continue;
            }

            String regionCode = normalizeCode(region.getCode());

            List<Form058> regionForms = safeForms.stream()
                    .filter(Objects::nonNull)
                    .filter(form -> matchesRegion(form, regionCode))
                    .toList();

            reports.add(
                    buildReport(
                            getDisplay(region),
                            regionForms
                    )
            );
        }

        return reports;
    }

    public Object buildDistrictReports(
            String stateCode,
            List<Form058> forms
    ) {
        String normalizedStateCode = normalizeCode(stateCode);

        if (!hasText(normalizedStateCode)) {
            return List.of();
        }

        List<Form058> safeForms = nullSafeForms(forms);
        List<Catalog> districts = nullSafeCatalogs(
                catalogService.findAllByParentCode(normalizedStateCode)
        );

        List<Form4_9ReportDto> reports = new ArrayList<>(districts.size());

        for (Catalog district : districts) {
            if (district == null) {
                continue;
            }

            String districtCode = normalizeCode(district.getCode());

            /*
             * Faqat shu tumanga tegishli formalar olinadi.
             *
             * Oldingi koddagi:
             *
             * city.isBlank() && state.equals(stateCode)
             *
             * sharti olib tashlandi. Chunki cityCode bo‘sh bo‘lgan
             * bitta forma viloyatning barcha tumanlariga qo‘shilib ketardi.
             */
            List<Form058> districtForms = safeForms.stream()
                    .filter(Objects::nonNull)
                    .filter(form -> matchesDistrict(
                            form,
                            normalizedStateCode,
                            districtCode
                    ))
                    .toList();

            reports.add(
                    buildReport(
                            getDisplay(district),
                            districtForms
                    )
            );
        }

        return reports;
    }

    public Object buildOrganizations(
            String districtCode,
            List<Form058> forms
    ) {
        String normalizedDistrictCode = normalizeCode(districtCode);

        if (!hasText(normalizedDistrictCode)) {
            return List.of();
        }

        List<Form058> districtForms = nullSafeForms(forms).stream()
                .filter(Objects::nonNull)
                .filter(form -> belongsToDistrict(
                        form,
                        normalizedDistrictCode
                ))
                .toList();

        /*
         * Tashkilotlarni cityCode bo‘yicha emas,
         * createdOrg identifikatori bo‘yicha guruhlaymiz.
         *
         * Bitta tumanda bir nechta tashkilot bir xil cityCode
         * bilan saqlanishi mumkin.
         */
        Map<OrganizationKey, List<Form058>> formsByOrganization =
                districtForms.stream()
                        .filter(form -> form.getCreatedOrg() != null)
                        .collect(Collectors.groupingBy(
                                form -> organizationKey(
                                        form.getCreatedOrg()
                                ),
                                LinkedHashMap::new,
                                Collectors.toList()
                        ));

        return formsByOrganization.entrySet()
                .stream()
                .map(entry -> buildReport(
                        entry.getKey().displayName(),
                        entry.getValue()
                ))
                .sorted(
                        Comparator.comparing(
                                Form4_9ReportDto::getName,
                                Comparator.nullsLast(
                                        String.CASE_INSENSITIVE_ORDER
                                )
                        )
                )
                .toList();
    }

    // ============================================================
    // REPORT BUILDING
    // ============================================================

    private Form4_9ReportDto buildReport(
            String name,
            List<Form058> forms
    ) {
        CategoryStatistics statistics = calculateStatistics(forms);

        return Form4_9ReportDto.builder()
                .name(hasText(name) ? name : "-")
                .unorganized(statistics.unorganized())
                .organized(statistics.organized())
                .pupils(statistics.pupils())
                .secondaryInstitutions(
                        statistics.secondaryInstitutions()
                )
                .institutions(statistics.institutions())
                .employee(statistics.employee())
                .medEmployee(statistics.medEmployee())
                .unEmployee(statistics.unEmployee())
                .pensioners(statistics.pensioners())
                .homelessPeople(statistics.homelessPeople())
                .total(statistics.total())
                .staff(statistics.staff())
                .build();
    }

    /**
     * Barcha statistikani bir marotaba ro‘yxatdan o‘tib hisoblaydi.
     */
    private CategoryStatistics calculateStatistics(
            List<Form058> forms
    ) {
        long total = 0L;
        long unorganized = 0L;
        long organized = 0L;
        long pupils = 0L;
        long secondaryInstitutions = 0L;
        long institutions = 0L;
        long employee = 0L;
        long medEmployee = 0L;
        long unEmployee = 0L;
        long pensioners = 0L;
        long homelessPeople = 0L;
        long explicitStaff = 0L;

        for (Form058 form : nullSafeForms(forms)) {
            if (form == null) {
                continue;
            }

            /*
             * TOTAL — report filteridan o‘tgan barcha Form058 yozuvlari.
             *
             * Patient yoki categoryCode yo‘q bo‘lsa ham forma umumiy
             * totalga kiradi. Bu reportdagi real formalar sonini ko‘rsatadi.
             */
            total++;

            if (form.getPatient() == null) {
                continue;
            }

            String categoryCode = normalizeCategoryCode(
                    form.getPatient().getCategoryCode()
            );

            if (!hasText(categoryCode)) {
                continue;
            }

            switch (categoryCode) {
                case CATEGORY_NO_ORGANIZED,
                     CATEGORY_NOT_ORGANIZED -> unorganized++;

                case CATEGORY_ORGANIZED -> organized++;

                case CATEGORY_STUDENT_SCHOOL -> pupils++;

                case CATEGORY_MIDDLE_STUDENT,
                     CATEGORY_SECONDARY_STUDENT -> secondaryInstitutions++;

                case CATEGORY_STUDENT -> institutions++;

                case CATEGORY_WORKER -> employee++;

                case CATEGORY_MEDICAL_WORKER -> medEmployee++;

                case CATEGORY_NOT_EMPLOYED -> unEmployee++;

                case CATEGORY_PENSIONER -> pensioners++;

                /*
                 * Eski noto‘g‘ri yozilgan va to‘g‘ri variantning
                 * ikkalasini ham qo‘llab-quvvatlaydi.
                 */
                case CATEGORY_UNSHELTRED,
                     CATEGORY_UNSHELTERED -> homelessPeople++;

                case CATEGORY_STAFF -> explicitStaff++;

                default -> {
                    /*
                     * Noma’lum categoryCode TOTAL ichida qoladi,
                     * ammo maxsus ustunlardan biriga qo‘shilmaydi.
                     */
                }
            }
        }

        /*
         * STAFF — to‘g‘ridan-to‘g‘ri STAFF kategoriyasi,
         * oddiy xodimlar va tibbiyot xodimlari yig‘indisi.
         *
         * Har bir patientda bitta categoryCode bo‘lgani uchun
         * bu yerda takroriy hisoblash bo‘lmaydi.
         */
        long staff = explicitStaff + employee + medEmployee;

        return new CategoryStatistics(
                unorganized,
                organized,
                pupils,
                secondaryInstitutions,
                institutions,
                employee,
                medEmployee,
                unEmployee,
                pensioners,
                homelessPeople,
                total,
                staff
        );
    }

    // ============================================================
    // LOCATION FILTERING
    // ============================================================

    private List<Form058> filterByLocation(
            List<Form058> forms,
            String locationCode
    ) {
        String normalizedLocationCode = normalizeCode(locationCode);

        if (!hasText(normalizedLocationCode)) {
            return List.of();
        }

        return nullSafeForms(forms).stream()
                .filter(Objects::nonNull)
                .filter(form -> form.getCreatedOrg() != null)
                .filter(form -> isLocationMatch(
                        form.getCreatedOrg(),
                        normalizedLocationCode
                ))
                .toList();
    }

    private boolean matchesRegion(
            Form058 form,
            String regionCode
    ) {
        if (form == null
                || form.getCreatedOrg() == null
                || !hasText(regionCode)) {
            return false;
        }

        Organization organization = form.getCreatedOrg();

        String stateCode = normalizeCode(
                organization.getStateCode()
        );

        String cityCode = normalizeCode(
                organization.getCityCode()
        );

        if (regionCode.equals(stateCode)) {
            return true;
        }

        /*
         * Legacy yozuvlarda stateCode bo‘sh bo‘lsa,
         * cityCode prefiksi orqali region aniqlanadi.
         */
        return !hasText(stateCode)
                && hasText(cityCode)
                && cityCode.startsWith(regionCode);
    }

    private boolean matchesDistrict(
            Form058 form,
            String stateCode,
            String districtCode
    ) {
        if (form == null
                || form.getCreatedOrg() == null
                || !hasText(districtCode)) {
            return false;
        }

        Organization organization = form.getCreatedOrg();

        String organizationStateCode = normalizeCode(
                organization.getStateCode()
        );

        String organizationCityCode = normalizeCode(
                organization.getCityCode()
        );

        /*
         * Agar tashkilotda stateCode mavjud bo‘lsa,
         * boshqa viloyatdagi tashkilotlarni chiqarib tashlaymiz.
         */
        if (hasText(organizationStateCode)
                && hasText(stateCode)
                && !stateCode.equals(organizationStateCode)) {
            return false;
        }

        return hasText(organizationCityCode)
                && (
                organizationCityCode.equals(districtCode)
                        || organizationCityCode.startsWith(
                        districtCode
                )
        );
    }

    private boolean belongsToDistrict(
            Form058 form,
            String districtCode
    ) {
        if (form == null
                || form.getCreatedOrg() == null
                || !hasText(districtCode)) {
            return false;
        }

        String organizationCityCode = normalizeCode(
                form.getCreatedOrg().getCityCode()
        );

        return hasText(organizationCityCode)
                && (
                organizationCityCode.equals(districtCode)
                        || organizationCityCode.startsWith(
                        districtCode
                )
        );
    }

    private boolean isLocationMatch(
            Organization organization,
            String locationCode
    ) {
        if (organization == null || !hasText(locationCode)) {
            return false;
        }

        String stateCode = normalizeCode(
                organization.getStateCode()
        );

        String cityCode = normalizeCode(
                organization.getCityCode()
        );

        if (locationCode.equals(stateCode)) {
            return true;
        }

        if (locationCode.equals(cityCode)) {
            return true;
        }

        return hasText(cityCode)
                && cityCode.startsWith(locationCode);
    }

    // ============================================================
    // ORGANIZATION GROUPING
    // ============================================================

    private OrganizationKey organizationKey(
            Organization organization
    ) {
        String organizationName = hasText(organization.getName())
                ? organization.getName().trim()
                : "Noma’lum tashkilot";

        return new OrganizationKey(
                organization.getId(),
                organizationName,
                normalizeCode(organization.getCityCode())
        );
    }

    // ============================================================
    // LOCALIZATION
    // ============================================================

    private String getDisplay(Catalog catalog) {
        if (catalog == null) {
            return "-";
        }

        Locale locale = LocaleContextHolder.getLocale();

        String language = locale.getLanguage();
        String script = locale.getScript();

        String localizedName = null;

        if ("uz".equalsIgnoreCase(language)
                && "Cyrl".equalsIgnoreCase(script)) {
            localizedName = catalog.getNameUzCyril();
        } else if ("uz".equalsIgnoreCase(language)) {
            localizedName = catalog.getNameUz();
        } else if ("ru".equalsIgnoreCase(language)) {
            localizedName = catalog.getNameRu();
        } else if ("kaa".equalsIgnoreCase(language)) {
            localizedName = catalog.getNameKaa();
        }

        return firstNonBlank(
                localizedName,
                catalog.getNameUz(),
                catalog.getNameRu(),
                catalog.getNameUzCyril(),
                catalog.getNameKaa(),
                catalog.getCode(),
                "-"
        );
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return "-";
        }

        for (String value : values) {
            if (hasText(value)) {
                return value.trim();
            }
        }

        return "-";
    }

    // ============================================================
    // NORMALIZATION
    // ============================================================

    private String normalizeCode(String value) {
        if (!hasText(value)) {
            return "";
        }

        return value
                .trim()
                .toUpperCase(Locale.ROOT);
    }

    private String normalizeCategoryCode(String value) {
        if (!hasText(value)) {
            return "";
        }

        return value
                .trim()
                .toUpperCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');
    }

    private List<Form058> nullSafeForms(
            List<Form058> forms
    ) {
        return forms == null ? List.of() : forms;
    }

    private List<Catalog> nullSafeCatalogs(
            List<Catalog> catalogs
    ) {
        return catalogs == null ? List.of() : catalogs;
    }

    // ============================================================
    // INTERNAL MODELS
    // ============================================================

    private record OrganizationKey(
            Long organizationId,
            String displayName,
            String cityCode
    ) {
    }

    private record CategoryStatistics(
            long unorganized,
            long organized,
            long pupils,
            long secondaryInstitutions,
            long institutions,
            long employee,
            long medEmployee,
            long unEmployee,
            long pensioners,
            long homelessPeople,
            long total,
            long staff
    ) {
    }
}