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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class Form4_9ReportHelper {

    private static final String UNKNOWN_ORGANIZATION_NAME = "NOMA’LUM TASHKILOT";

    private final CatalogService catalogService;

    public Object buildByParentCode(String parentCode, List<Form058> forms) {
        Catalog parent = catalogService.findByCode(parentCode);

        if (parent == null || !hasText(parent.getType())) {
            return buildRegionReports(forms);
        }

        CatalogType type = resolveCatalogType(parent.getType());

        if (type == null) {
            return buildRegionReports(forms);
        }

        return switch (type) {
            case state -> buildDistrictReports(parentCode, forms);
            case city -> buildOrganizations(parentCode, forms);
            default -> buildRegionReports(forms);
        };
    }

    public Object buildRegionReports(List<Form058> forms) {
        List<Catalog> regions = catalogService.findAllByType(CatalogType.state);
        List<Catalog> categories = loadCategories();

        List<Form4_9ReportDto> result = new ArrayList<>();

        for (Catalog region : regions) {
            String regionCode = normalizeCode(region.getCode());
            String regionName = getDisplay(region);

            List<Form058> regionForms = filterByLocation(forms, regionCode);

            result.add(buildReport(regionName, regionForms, categories));
        }

        return result;
    }

    public Object buildDistrictReports(String stateCode, List<Form058> forms) {
        List<Catalog> districts = catalogService.findAllByParentCode(stateCode);
        List<Catalog> categories = loadCategories();

        List<Form4_9ReportDto> result = new ArrayList<>();

        for (Catalog district : districts) {
            String districtCode = normalizeCode(district.getCode());
            String districtName = getDisplay(district);

            List<Form058> districtForms = filterByLocation(forms, districtCode);

            result.add(buildReport(districtName, districtForms, categories));
        }

        return result;
    }

    public Object buildOrganizations(String cityCode, List<Form058> forms) {
        List<Catalog> categories = loadCategories();

        List<Form058> cityForms = filterByLocation(forms, cityCode);

        Map<String, List<Form058>> formsByOrganization = cityForms.stream()
                .filter(form -> form.getCreatedOrg() != null)
                .collect(Collectors.groupingBy(
                        form -> resolveOrganizationKey(form.getCreatedOrg()),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        return formsByOrganization.values()
                .stream()
                .map(organizationForms -> {

                    String organizationName = organizationForms.stream()
                            .map(Form058::getCreatedOrg)
                            .filter(Objects::nonNull)
                            .map(Organization::getName)
                            .filter(this::hasText)
                            .findFirst()
                            .orElse(UNKNOWN_ORGANIZATION_NAME);

                    return buildReport(
                            organizationName,
                            organizationForms,
                            categories
                    );
                })
                .sorted(Comparator.comparing(Form4_9ReportDto::getName))
                .toList();
    }

    private Form4_9ReportDto buildReport(
            String name,
            List<Form058> forms,
            List<Catalog> categories
    ) {
        Map<String, Long> stats = forms.stream()
                .filter(form -> form.getPatient() != null)
                .map(form -> form.getPatient().getCategoryCode())
                .filter(this::hasText)
                .map(this::normalizeCode)
                .collect(Collectors.groupingBy(
                        code -> code,
                        Collectors.counting()
                ));

        Form4_9ReportDto dto = Form4_9ReportDto.builder()
                .name(name)
                .build();

        /*
         * Category code faqat catalogdan olinadi.
         * Bu yerda Java ichida "PENSIONER", "WORKER" deb count qilinmaydi.
         */
        for (Catalog category : categories) {
            String categoryCode = normalizeCode(category.getCode());

            if (!hasText(categoryCode)) {
                continue;
            }

            long count = stats.getOrDefault(categoryCode, 0L);

            applyCategoryToDto(dto, categoryCode, count);
        }

        dto.setTotal(
                dto.getUnorganized()
                        + dto.getOrganized()
                        + dto.getPupils()
                        + dto.getSecondaryInstitutions()
                        + dto.getInstitutions()
                        + dto.getEmployee()
                        + dto.getMedEmployee()
                        + dto.getUnEmployee()
                        + dto.getPensioners()
                        + dto.getHomelessPeople()
                        + dto.getStaff()
        );

        return dto;
    }

    private List<Catalog> loadCategories() {
        return catalogService.findAllByType(CatalogType.category)
                .stream()
                .filter(category -> hasText(category.getCode()))
                .toList();
    }

    private void applyCategoryToDto(
            Form4_9ReportDto dto,
            String categoryCode,
            long count
    ) {
        switch (categoryCode) {
            case "NO_ORGANIZED" -> dto.setUnorganized(dto.getUnorganized() + count);
            case "ORGANIZED" -> dto.setOrganized(dto.getOrganized() + count);
            case "STUDENT_SCHOOL" -> dto.setPupils(dto.getPupils() + count);
            case "MIDDLE_STUDENT" -> dto.setSecondaryInstitutions(dto.getSecondaryInstitutions() + count);
            case "STUDENT" -> dto.setInstitutions(dto.getInstitutions() + count);
            case "WORKER" -> dto.setEmployee(dto.getEmployee() + count);
            case "MEDICAL_WORKER" -> dto.setMedEmployee(dto.getMedEmployee() + count);
            case "NOT_EMPLOYED" -> dto.setUnEmployee(dto.getUnEmployee() + count);
            case "PENSIONER" -> dto.setPensioners(dto.getPensioners() + count);
            case "UNSHELTRED" -> dto.setHomelessPeople(dto.getHomelessPeople() + count);

            /*
             * DTO da SEREVANTS va TEACHER uchun alohida field yo‘q.
             * Shuning uchun ikkalasi staff ga qo‘shiladi.
             */
            case "SEREVANTS", "TEACHER" -> dto.setStaff(dto.getStaff() + count);

            default -> {
                // DTO da bu category uchun alohida field yo‘q.
            }
        }
    }

    private List<Form058> filterByLocation(List<Form058> forms, String locationCode) {
        if (!hasText(locationCode)) {
            return List.of();
        }

        String code = normalizeCode(locationCode);

        return forms.stream()
                .filter(form -> form.getCreatedOrg() != null)
                .filter(form -> isLocationMatch(form.getCreatedOrg(), code))
                .toList();
    }

    private boolean isLocationMatch(Organization organization, String code) {
        if (organization == null || !hasText(code)) {
            return false;
        }

        String stateCode = normalizeCode(organization.getStateCode());
        String cityCode = normalizeCode(organization.getCityCode());

        if (code.equals(stateCode)) {
            return true;
        }

        if (code.equals(cityCode)) {
            return true;
        }

        return hasText(cityCode) && cityCode.startsWith(code);
    }

    private String resolveOrganizationKey(Organization organization) {
        if (organization == null) {
            return UNKNOWN_ORGANIZATION_NAME;
        }

        if (organization.getId() != null) {
            return String.valueOf(organization.getId());
        }

        if (hasText(organization.getName())) {
            return organization.getName().trim();
        }

        return UNKNOWN_ORGANIZATION_NAME;
    }

    private CatalogType resolveCatalogType(String type) {
        try {
            return CatalogType.valueOf(type.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private String getDisplay(Catalog catalog) {
        if (catalog == null) {
            return "-";
        }

        Locale locale = LocaleContextHolder.getLocale();
        String lang = locale.getLanguage();
        String script = locale.getScript();

        String value = null;

        if ("uz".equals(lang) && "Cyrl".equalsIgnoreCase(script)) {
            value = catalog.getNameUzCyril();
        } else if ("uz".equals(lang)) {
            value = catalog.getNameUz();
        } else if ("ru".equals(lang)) {
            value = catalog.getNameRu();
        } else if ("kaa".equals(lang)) {
            value = catalog.getNameKaa();
        }

        return Optional.ofNullable(value)
                .filter(this::hasText)
                .orElse("-");
    }

    private String normalizeCode(String code) {
        return code == null ? "" : code.trim().toUpperCase();
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}