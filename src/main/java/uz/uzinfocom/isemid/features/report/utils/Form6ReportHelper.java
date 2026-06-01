package uz.uzinfocom.isemid.features.report.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.classifier.eums.CatalogType;
import uz.uzinfocom.isemid.features.classifier.models.Catalog;
import uz.uzinfocom.isemid.features.classifier.services.CatalogService;
import uz.uzinfocom.isemid.features.classifier.utils.CatalogLocaleUtil;
import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.report.dtos.Form6ReportDto;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class Form6ReportHelper {

    private static final String UNKNOWN_ORGANIZATION_NAME = "NOMA’LUM TASHKILOT";

    private final CatalogService catalogService;
    private final MessageUtil messageUtil;

    public Object buildByParentCode(String parentCode, List<Form058> forms) {
        Catalog catalog = catalogService.findByCode(parentCode);

        if (catalog == null) {
            return buildRegionReports(forms);
        }

        CatalogType type = CatalogType.valueOf(catalog.getType());

        return switch (type) {
            case state -> buildDistrictReports(parentCode, forms);
            case city -> buildOrganizations(parentCode, forms);
            default -> buildRegionReports(forms);
        };
    }

    public Object buildRegionReports(List<Form058> forms) {
        List<Catalog> regions = catalogService.findAllByType(CatalogType.state);
        List<Form6ReportDto> result = new ArrayList<>();

        for (Catalog region : regions) {
            String regionCode = region.getCode();
            String regionName = CatalogLocaleUtil.displayName(region);

            List<Form058> regionForms = forms.stream()
                    .filter(f -> f.getCreatedOrg() != null)
                    .filter(f -> regionCode.equals(f.getCreatedOrg().getStateCode()))
                    .toList();

            result.add(buildSingle(regionCode, regionName, regionForms));
        }

        return result;
    }

    public Object buildDistrictReports(String stateCode, List<Form058> forms) {
        List<Catalog> districts = catalogService.findAllByParentCode(stateCode);
        List<Form6ReportDto> result = new ArrayList<>();

        for (Catalog district : districts) {
            String districtCode = district.getCode();
            String districtName = CatalogLocaleUtil.displayName(district);

            List<Form058> districtForms = forms.stream()
                    .filter(f -> f.getCreatedOrg() != null)
                    .filter(f -> districtCode.equals(f.getCreatedOrg().getCityCode()))
                    .toList();

            result.add(buildSingle(districtCode, districtName, districtForms));
        }

        return result;
    }

    public Object buildOrganizations(String cityCode, List<Form058> forms) {
        List<Form058> cityForms = forms.stream()
                .filter(f -> f.getCreatedOrg() != null)
                .filter(f -> cityCode.equals(f.getCreatedOrg().getCityCode()))
                .toList();

        Map<Long, List<Form058>> formsByOrganization = cityForms.stream()
                .filter(f -> f.getCreatedOrg().getId() != null)
                .collect(Collectors.groupingBy(f -> f.getCreatedOrg().getId()));

        return formsByOrganization.entrySet()
                .stream()
                .map(entry -> {
                    Long organizationId = entry.getKey();
                    List<Form058> organizationForms = entry.getValue();

                    String organizationName = organizationForms.stream()
                            .map(Form058::getCreatedOrg)
                            .filter(Objects::nonNull)
                            .map(org -> org.getName() != null ? org.getName() : UNKNOWN_ORGANIZATION_NAME)
                            .findFirst()
                            .orElse(UNKNOWN_ORGANIZATION_NAME);

                    return buildSingle(
                            String.valueOf(organizationId),
                            organizationName,
                            organizationForms
                    );
                })
                .sorted(Comparator.comparing(Form6ReportDto::getLocationName))
                .toList();
    }

    private Form6ReportDto buildSingle(
            String locationCode,
            String locationName,
            List<Form058> forms
    ) {
        int previousTotal = 0;
        int currentTotal = 0;

        List<Form6ReportDto.AgeGroupRow> rows = new ArrayList<>();

        List<AgeGroupSpec> ageGroups = List.of(
                new AgeGroupSpec(messageUtil.get("form6.age.0_1"), 0, 0, true),
                new AgeGroupSpec(messageUtil.get("form6.age.1_2"), 1, 2, true),
                new AgeGroupSpec(messageUtil.get("form6.age.3_5"), 3, 5, true),
                new AgeGroupSpec(messageUtil.get("form6.age.6_14"), 6, 14, true),
                new AgeGroupSpec(messageUtil.get("form6.age.15_17"), 15, 17, true),

                // Subtotal row. It must not be included in total again.
                new AgeGroupSpec(messageUtil.get("form6.age.0_17"), 0, 17, false),

                new AgeGroupSpec(messageUtil.get("form6.age.19_25"), 19, 25, true),
                new AgeGroupSpec(messageUtil.get("form6.age.26_40"), 26, 40, true),
                new AgeGroupSpec(messageUtil.get("form6.age.41_55"), 41, 55, true),
                new AgeGroupSpec(messageUtil.get("form6.age.56_70"), 56, 70, true),
                new AgeGroupSpec(messageUtil.get("form6.age.70_200"), 71, 200, true)
        );

        for (AgeGroupSpec ageGroup : ageGroups) {
            Form6ReportDto.AgeGroupRow row = buildAgeRow(
                    ageGroup.name(),
                    forms,
                    ageGroup.minAge(),
                    ageGroup.maxAge()
            );

            rows.add(row);

            if (ageGroup.includeInTotal()) {
                previousTotal += row.getPreviousYear();
                currentTotal += row.getCurrentYear();
            }
        }

        return Form6ReportDto.builder()
                .locationCode(locationCode)
                .locationName(locationName)
                .previousYearTotal(previousTotal)
                .currentYearTotal(currentTotal)
                .growthTotal(currentTotal - previousTotal)
                .rows(rows)
                .build();
    }

    private Form6ReportDto.AgeGroupRow buildAgeRow(
            String name,
            List<Form058> forms,
            int minAge,
            int maxAge
    ) {
        int previousYearCount = 0;
        int currentYearCount = 0;

        int previousYear = LocalDate.now().minusYears(1).getYear();
        int currentYear = LocalDate.now().getYear();

        for (Form058 form : forms) {
            if (form == null) {
                continue;
            }

            if (form.getPatient() == null || form.getPatient().getBirthDate() == null) {
                continue;
            }

            if (form.getCreatedAt() == null) {
                continue;
            }

            LocalDate createdDate = toLocalDate(form.getCreatedAt());

            int age = calculateAge(
                    form.getPatient().getBirthDate(),
                    createdDate
            );

            if (age < minAge || age > maxAge) {
                continue;
            }

            int reportYear = createdDate.getYear();

            if (reportYear == previousYear) {
                previousYearCount++;
            }

            if (reportYear == currentYear) {
                currentYearCount++;
            }
        }

        return Form6ReportDto.AgeGroupRow.builder()
                .ageGroupName(name)
                .previousYear(previousYearCount)
                .currentYear(currentYearCount)
                .growth(currentYearCount - previousYearCount)
                .build();
    }

    private int calculateAge(LocalDate birthDate, LocalDate reportDate) {
        if (birthDate == null || reportDate == null) {
            return -1;
        }

        if (birthDate.isAfter(reportDate)) {
            return -1;
        }

        return Period.between(birthDate, reportDate).getYears();
    }

    private LocalDate toLocalDate(Timestamp timestamp) {
        return timestamp.toLocalDateTime().toLocalDate();
    }

    private record AgeGroupSpec(
            String name,
            int minAge,
            int maxAge,
            boolean includeInTotal
    ) {
    }
}