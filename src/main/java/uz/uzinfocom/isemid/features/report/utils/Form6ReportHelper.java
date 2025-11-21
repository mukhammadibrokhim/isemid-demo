package uz.uzinfocom.isemid.features.report.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
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

@Component
@RequiredArgsConstructor
public class Form6ReportHelper {
    private final CatalogService catalogService;

    public Object buildByParentCode(String trim, List<Form058> forms) {
        return null;
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

            Form6ReportDto dto = buildSingleRegion(regionCode, regionName, regionForms);

            result.add(dto);
        }

        return result;
    }

    public Object buildDistrictReports(String stateCode, List<Form058> forms) {
        return null;
    }

    public Object buildOrganizations(String cityCode, List<Form058> forms) {
        return null;
    }

    private Form6ReportDto buildSingleRegion(String locationCode,
                                             String locationName,
                                             List<Form058> forms) {

        int previousTotal = 0;
        int currentTotal = 0;

        List<Form6ReportDto.AgeGroupRow> rows = new ArrayList<>();

        rows.add(buildAgeRow("новорожденные (до 1 года)", forms, 0, 1));
        rows.add(buildAgeRow("1–2 года", forms, 1, 2));
        rows.add(buildAgeRow("3–5 лет", forms, 3, 5));
        rows.add(buildAgeRow("6–14 лет", forms, 6, 14));
        rows.add(buildAgeRow("15–17 лет", forms, 15, 17));
        rows.add(buildAgeRow("дети до 18 лет", forms, 0, 17));
        rows.add(buildAgeRow("19–25 лет", forms, 19, 25));
        rows.add(buildAgeRow("26–40 лет", forms, 26, 40));
        rows.add(buildAgeRow("41–55 лет", forms, 41, 55));
        rows.add(buildAgeRow("56–70 лет", forms, 56, 70));
        rows.add(buildAgeRow("Старше 70 лет", forms, 70, 200));

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
            if (f.getDiseaseDate() == null) continue;

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

}
