package uz.uzinfocom.isemid.features.report.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.uzinfocom.isemid.commons.models.AbsEntity;
import uz.uzinfocom.isemid.features.classifier.eums.CatalogType;
import uz.uzinfocom.isemid.features.classifier.models.Catalog;
import uz.uzinfocom.isemid.features.classifier.services.CatalogService;
import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.report.dtos.FormReport10Dto;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FormReport10Helper {

    private final CatalogService catalogService;

    /* =========================================================
       ENTRY POINTS
       ========================================================= */

    public Object buildByParentCode(String parentCode, List<Form058> forms) {

        if (parentCode == null || parentCode.isBlank()) {
            return buildRegionReports(forms);
        }

        // agar parentCode viloyat bo‘lsa → tumanlar
        return buildDistrictReports(parentCode, forms);
    }

    public Object buildRegionReports(List<Form058> forms) {

        List<Catalog> regions = catalogService.findAllByType(CatalogType.state);

        return regions.stream()
                .map(region -> {
                    String code = region.getCode();
                    List<Form058> regionForms = filterByState(forms, code);

                    return buildDto(code, region.getNameUz(), regionForms);
                })
                .toList();
    }

    public Object buildDistrictReports(String stateCode, List<Form058> forms) {

        List<Catalog> districts = catalogService.findAllByParentCode(stateCode);
        if (districts.isEmpty()) return List.of();

        return districts.stream()
                .map(dist -> {
                    String code = dist.getCode();
                    List<Form058> districtForms = filterByDistrict(forms, stateCode, code);

                    return buildDto(code, dist.getNameUz(), districtForms);
                })
                .toList();
    }

    public Object buildOrganizations(String cityCode, List<Form058> forms) {

        Map<String, List<Form058>> byOrg =
                forms.stream()
                        .filter(f -> f.getCreatedOrg() != null)
                        .filter(f -> cityCode.equals(f.getCreatedOrg().getCityCode()))
                        .collect(Collectors.groupingBy(
                                f -> safe(f.getCreatedOrg().getCityCode())
                        ));

        return byOrg.entrySet().stream()
                .map(e -> {
                    String orgCode = e.getKey();
                    List<Form058> orgForms = e.getValue();

                    String orgName = orgForms.stream()
                            .map(AbsEntity::getCreatedOrg)
                            .filter(Objects::nonNull)
                            .map(Organization::getName)
                            .findFirst()
                            .orElse(orgCode);

                    return buildDto(orgCode, orgName, orgForms);
                })
                .toList();
    }

    /* =========================================================
       CORE BUILDER
       ========================================================= */

    private FormReport10Dto buildDto(String code, String name, List<Form058> forms) {

        int currentYear = LocalDate.now().getYear();
        int previousYear = currentYear - 1;
        int month = LocalDate.now().getMonthValue();

        // ===== CURRENT MONTH =====
        Stat cmPrevTotal = calcMonth(forms, previousYear, month, false);
        Stat cmCurrTotal = calcMonth(forms, currentYear, month, false);

        Stat cmPrevChild = calcMonth(forms, previousYear, month, true);
        Stat cmCurrChild = calcMonth(forms, currentYear, month, true);

        // ===== CUMULATIVE =====
        Stat cumPrevTotal = calcCumulative(forms, previousYear, month, false);
        Stat cumCurrTotal = calcCumulative(forms, currentYear, month, false);

        Stat cumPrevChild = calcCumulative(forms, previousYear, month, true);
        Stat cumCurrChild = calcCumulative(forms, currentYear, month, true);

        return FormReport10Dto.builder()
                .locationCode(code)
                .locationName(name)

                .currentMonth(buildPeriod(cmPrevTotal, cmCurrTotal, cmPrevChild, cmCurrChild))
                .cumulative(buildPeriod(cumPrevTotal, cumCurrTotal, cumPrevChild, cumCurrChild))

                .build();
    }

    private FormReport10Dto.PeriodStat buildPeriod(
            Stat prevTotal, Stat currTotal,
            Stat prevChild, Stat currChild
    ) {

        return FormReport10Dto.PeriodStat.builder()
                .total(buildMetric(prevTotal, currTotal))
                .childrenUnder14(buildMetric(prevChild, currChild))
                .build();
    }

    private FormReport10Dto.Metric buildMetric(Stat prev, Stat curr) {

        return FormReport10Dto.Metric.builder()
                .absolute(buildCompare(prev.abs, curr.abs))
                .intensive(buildCompare(prev.intensive, curr.intensive))
                .build();
    }

    private <T extends Number> FormReport10Dto.YearCompare<T> buildCompare(T prev, T curr) {

        return FormReport10Dto.YearCompare.<T>builder()
                .previousYear(prev)
                .currentYear(curr)
                .growthPercent(calcGrowth(curr, prev))
                .build();
    }

    /* =========================================================
       CALCULATION
       ========================================================= */

    private Stat calcMonth(List<Form058> forms, int year, int month, boolean children) {

        YearMonth ym = YearMonth.of(year, month);
        LocalDate from = ym.atDay(1);
        LocalDate to = ym.atEndOfMonth();

        return calc(forms, from, to, children);
    }

    private Stat calcCumulative(List<Form058> forms, int year, int month, boolean children) {

        LocalDate from = LocalDate.of(year, 1, 1);
        LocalDate to = YearMonth.of(year, month).atEndOfMonth();

        return calc(forms, from, to, children);
    }

    private Stat calc(List<Form058> forms, LocalDate from, LocalDate to, boolean children) {

        long abs = forms.stream()
                .filter(f -> f.getCreatedAt() != null)
                .filter(f -> {
                    LocalDate d = f.getCreatedAt().toLocalDateTime().toLocalDate();
                    return !d.isBefore(from) && !d.isAfter(to);
                })
                .filter(f -> isChildren(f, children))
                .count();

        // TODO: population servisidan haqiqiy intensiv hisoblash
        double intensive = abs == 0 ? 0d : abs * 100_000d;

        return new Stat((int) abs, intensive);
    }

    /* =========================================================
       FILTERS
       ========================================================= */

    private List<Form058> filterByState(List<Form058> forms, String stateCode) {

        return forms.stream()
                .filter(f -> f.getCreatedOrg() != null)
                .filter(f -> stateCode.equals(f.getCreatedOrg().getStateCode()))
                .toList();
    }

    private List<Form058> filterByDistrict(List<Form058> forms, String stateCode, String cityCode) {

        return forms.stream()
                .filter(f -> f.getCreatedOrg() != null)
                .filter(f -> stateCode.equals(f.getCreatedOrg().getStateCode()))
                .filter(f -> cityCode.equals(f.getCreatedOrg().getCityCode()))
                .toList();
    }

    /* =========================================================
       HELPERS
       ========================================================= */

    private boolean isChildren(Form058 f, boolean children) {

        if (!children) return true;

        if (f.getPatient() == null || f.getPatient().getBirthDate() == null) return false;

        LocalDate birth = f.getPatient().getBirthDate();
        LocalDate created = f.getCreatedAt().toLocalDateTime().toLocalDate();

        return java.time.Period.between(birth, created).getYears() < 14;
    }

    private Double calcGrowth(Number curr, Number prev) {

        if (prev == null || prev.doubleValue() == 0d) return null;

        return ((curr.doubleValue() - prev.doubleValue()) * 100d) / prev.doubleValue();
    }

    private String safe(String v) {
        return v == null ? "" : v.trim();
    }

    /* =========================================================
       INTERNAL HOLDER
       ========================================================= */

    private record Stat(Integer abs, double intensive) {}
}
