package uz.uzinfocom.isemid.features.report.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.classifier.eums.CatalogType;
import uz.uzinfocom.isemid.features.classifier.models.Catalog;
import uz.uzinfocom.isemid.features.classifier.services.CatalogService;
import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.report.dtos.Form6ReportDto;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class Form6ReportHelper {
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
            String regionName = region.getNameUz();

            List<Form058> regionForms = forms.stream()
                    .filter(f -> f.getCreatedOrg() != null
                            && regionCode.equals(f.getCreatedOrg().getStateCode()))
                    .toList();

            Form6ReportDto dto = buildSingle(regionCode, regionName, regionForms);

            result.add(dto);
        }

        return result;
    }

    public Object buildDistrictReports(String stateCode, List<Form058> forms) {

        List<Catalog> districts =
                catalogService.findAllByParentCode(stateCode);

        List<Form6ReportDto> result = new ArrayList<>();

        for (Catalog district : districts) {

            String districtCode = district.getCode();
            String districtName = district.getNameUz();

            List<Form058> districtForms = forms.stream()
                    .filter(f ->
                            f.getCreatedOrg() != null &&
                                    districtCode.equals(f.getCreatedOrg().getCityCode())
                    )
                    .toList();

            Form6ReportDto dto = buildSingle(districtCode, districtName, districtForms);

            result.add(dto);
        }

        return result;
    }

    public Object buildOrganizations(String cityCode, List<Form058> forms) {

        Set<String> orgCodes = forms.stream()
                .filter(f -> f.getCreatedOrg() != null &&
                        cityCode.equals(f.getCreatedOrg().getCityCode()))
                .map(f -> f.getCreatedOrg().getCityCode())
                .collect(Collectors.toSet());

        List<Form6ReportDto> result = new ArrayList<>();

        for (String orgCode : orgCodes) {

            String orgName = forms.stream()
                    .filter(f -> f.getCreatedOrg() != null &&
                            orgCode.equals(f.getCreatedOrg().getCityCode()))
                    .map(f -> f.getCreatedOrg().getName())
                    .findFirst()
                    .orElse("NOMA’LUM TASHKILOT");

            List<Form058> orgForms = forms.stream()
                    .filter(f -> f.getCreatedOrg() != null &&
                            orgCode.equals(f.getCreatedOrg().getCityCode()))
                    .toList();

            // Form6 hisob
            Form6ReportDto dto = buildSingle(orgCode, orgName, orgForms);

            result.add(dto);
        }

        return result;
    }


    private Form6ReportDto.AgeGroupRow buildAgeRow(
            String name,
            List<Form058> forms,
            int minAge,
            int maxAge
    ) {

        int prev = 0;
        int curr = 0;

        int prevYear = LocalDate.now().minusYears(1).getYear();
        int currYear = LocalDate.now().getYear();

        for (Form058 f : forms) {

            if (f.getPatient() == null || f.getPatient().getBirthDate() == null) continue;

            int age = calculateAge(f.getPatient().getBirthDate(), f.getCreatedAt());
            if (age < 0) continue;

            if (age < minAge || age > maxAge) continue;

            int year = f.getDiseaseDate().getYear();

            if (year == prevYear) prev++;
            if (year == currYear) curr++;
        }

        return Form6ReportDto.AgeGroupRow.builder()
                .ageGroupName(name)
                .previousYear(prev)
                .currentYear(curr)
                .growth(curr - prev)
                .build();
    }

    private int calculateAge(LocalDate birthDate, Timestamp atDate) {
        if (birthDate == null || atDate == null) return -1;

        LocalDate date = atDate.toLocalDateTime().toLocalDate();

        return Period.between(birthDate, date).getYears();
    }

    private Form6ReportDto buildSingle(
            String locationCode,
            String locationName,
            List<Form058> forms
    ) {
        int previousTotal = 0;
        int currentTotal = 0;

        List<Form6ReportDto.AgeGroupRow> rows = new ArrayList<>();

        rows.add(buildAgeRow(messageUtil.get("form6.age.0_1"), forms, 0, 1));
        rows.add(buildAgeRow(messageUtil.get("form6.age.1_2"), forms, 1, 2));
        rows.add(buildAgeRow(messageUtil.get("form6.age.3_5"), forms, 3, 5));
        rows.add(buildAgeRow(messageUtil.get("form6.age.6_14"), forms, 6, 14));
        rows.add(buildAgeRow(messageUtil.get("form6.age.15_17"), forms, 15, 17));
        rows.add(buildAgeRow(messageUtil.get("form6.age.0_17"), forms, 0, 17));
        rows.add(buildAgeRow(messageUtil.get("form6.age.19_25"), forms, 19, 25));
        rows.add(buildAgeRow(messageUtil.get("form6.age.26_40"), forms, 26, 40));
        rows.add(buildAgeRow(messageUtil.get("form6.age.41_55"), forms, 41, 55));
        rows.add(buildAgeRow(messageUtil.get("form6.age.56_70"), forms, 56, 70));
        rows.add(buildAgeRow(messageUtil.get("form6.age.70_200"), forms, 70, 200));

        for (var r : rows) {
            previousTotal += r.getPreviousYear();
            currentTotal += r.getCurrentYear();
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


}
