package uz.uzinfocom.isemid.features.report.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.uzinfocom.isemid.features.classifier.eums.CatalogType;
import uz.uzinfocom.isemid.features.classifier.models.Catalog;
import uz.uzinfocom.isemid.features.classifier.services.CatalogService;
import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.report.dtos.Form11ReportDto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class Form11ReportHelper {
    private final CatalogService catalogService;

    // -------------------- ENTRY BY PARENT CODE --------------------

    public Object buildByParentCode(String parentCode,
                                    List<Form058> forms,
                                    Long koef,
                                    Long population) {

        Catalog catalog = catalogService.findByCode(parentCode);

        if (catalog == null) {
            return buildRegionReports(forms, koef, population);
        }

        CatalogType type = CatalogType.valueOf(catalog.getType());

        return switch (type) {
            case state -> buildDistrictReports(parentCode, forms, koef, population);
            case city -> buildOrganizations(parentCode, forms, koef, population);
            default -> buildRegionReports(forms, koef, population);
        };
    }

    // -------------------- REGION LEVEL --------------------

    public Object buildRegionReports(List<Form058> forms,
                                     Long koef,
                                     Long population) {

        List<Catalog> regions = catalogService.findAllByType(CatalogType.state);

        List<Form11ReportDto> result = new ArrayList<>();

        for (Catalog region : regions) {

            String regionCode = region.getCode();
            String regionName = region.getNameUz();

            List<Form058> regionForms = forms.stream()
                    .filter(f -> f.getCreatedOrg() != null &&
                            regionCode.equals(f.getCreatedOrg().getStateCode()))
                    .toList();

            result.add(buildSingle(regionCode, regionName, regionForms, koef, population));
        }

        return result;
    }

    // -------------------- DISTRICT LEVEL --------------------

    public Object buildDistrictReports(String stateCode,
                                       List<Form058> forms,
                                       Long koef,
                                       Long population) {

        List<Catalog> districts = catalogService.findAllByParentCode(stateCode);

        List<Form11ReportDto> result = new ArrayList<>();

        for (Catalog district : districts) {

            String districtCode = district.getCode();
            String districtName = district.getNameUz();

            List<Form058> districtForms = forms.stream()
                    .filter(f -> f.getCreatedOrg() != null &&
                            districtCode.equals(f.getCreatedOrg().getCityCode()))
                    .toList();

            result.add(buildSingle(districtCode, districtName, districtForms, koef, population));
        }

        return result;
    }

    // -------------------- ORGANIZATION LEVEL --------------------

    public Object buildOrganizations(String cityCode,
                                     List<Form058> forms,
                                     Long koef,
                                     Long population) {

        Set<String> orgUuids = forms.stream()
                .filter(f -> f.getCreatedOrg() != null &&
                        cityCode.equals(f.getCreatedOrg().getCityCode()))
                .map(f -> f.getCreatedOrg().getUuid().toString())
                .collect(Collectors.toSet());

        List<Form11ReportDto> result = new ArrayList<>();

        for (String orgUuid : orgUuids) {

            String orgName = forms.stream()
                    .filter(f -> f.getCreatedOrg() != null &&
                            orgUuid.equals(f.getCreatedOrg().getUuid().toString()))
                    .map(f -> f.getCreatedOrg().getName())
                    .findFirst()
                    .orElse("NOMA’LUM TASHKILOT");

            List<Form058> orgForms = forms.stream()
                    .filter(f -> f.getCreatedOrg() != null &&
                            orgUuid.equals(f.getCreatedOrg().getUuid().toString()))
                    .toList();

            result.add(buildSingle(orgUuid, orgName, orgForms, koef, population));
        }

        return result;
    }

    // -------------------- CORE: BIR HUDUD UCHUN 11-FORMA --------------------

    private Form11ReportDto buildSingle(
            String locationCode,
            String locationName,
            List<Form058> forms,
            Long koef,
            Long population
    ) {
        int currentYear = LocalDate.now().getYear();
        int prevYear = currentYear - 1;

        long absPrev = forms.stream()
                .filter(f -> yearOf(f) == prevYear)
                .count();

        long absCurr = forms.stream()
                .filter(f -> yearOf(f) == currentYear)
                .count();

        long cityAbs = forms.stream().filter(this::isCity).count();
        long ruralAbs = forms.stream().filter(this::isRural).count();
        long childAbs = forms.stream().filter(this::isChild).count();

        return Form11ReportDto.builder()
                .locationCode(locationCode)
                .locationName(locationName)

                // Абсолют пок
                .absPrev(absPrev)
                .absCurr(absCurr)

                // Интенсив пок = count * koef / population
                .intPrev(intensity(absPrev, population, koef))
                .intCurr(intensity(absCurr, population, koef))

                // Рост/Снижение (+/-)
                .growth((double) (absCurr - absPrev))

                // Шахар ахолиси
                .cityAbs(cityAbs)
                .cityInt(intensity(cityAbs, population, koef))
                .cityPercent(percent(absCurr, cityAbs))

                // Қишлоқ аҳолиси
                .ruralAbs(ruralAbs)
                .ruralInt(intensity(ruralAbs, population, koef))
                .ruralPercent(percent(absCurr, ruralAbs))

                // 18 ёшгача болалар
                .childAbs(childAbs)
                .childInt(intensity(childAbs, population, koef))

                .build();
    }

    // -------------------- HELPERS: CITY / RURAL / CHILD --------------------

    private boolean isCity(Form058 f) {
        if (f.getPatient() == null) return false;
        String code = f.getPatient().getPopulationTypeCode();
        return code != null && code.equalsIgnoreCase("CITY_RESIDENT");
    }

    private boolean isRural(Form058 f) {
        if (f.getPatient() == null) return false;
        String code = f.getPatient().getPopulationTypeCode();
        return code != null && code.equalsIgnoreCase("VILLAGE_RESIDENT");
    }

    private boolean isChild(Form058 f) {
        if (f.getPatient() == null || f.getPatient().getBirthDate() == null) return false;
        int age = calculateAge(f.getPatient().getBirthDate(), f.getCreatedAt());
        return age >= 0 && age < 18;
    }

    private int calculateAge(LocalDate birthDate, Timestamp atDate) {
        LocalDate date = atDate.toLocalDateTime().toLocalDate();
        return Period.between(birthDate, date).getYears();
    }

    private int yearOf(Form058 f) {
        if (f.getCreatedAt() != null) {
            LocalDateTime dt = f.getCreatedAt().toLocalDateTime();
            return dt.getYear();
        }
        return -1;
    }

    // -------------------- MATH HELPERS --------------------

    private double intensity(long count, long population, long koef) {
        if (population == 0) return 0d;
        return (count * (double) koef) / population;
    }

    private double percent(long total, long part) {
        if (total == 0) return 0d;

        double value = (part * 100.0) / total;

        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

}
