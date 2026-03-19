package uz.uzinfocom.isemid.features.report.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.uzinfocom.isemid.commons.models.AbsEntity;
import uz.uzinfocom.isemid.features.classifier.eums.CatalogType;
import uz.uzinfocom.isemid.features.classifier.models.Catalog;
import uz.uzinfocom.isemid.features.classifier.services.CatalogService;
import uz.uzinfocom.isemid.features.classifier.utils.CatalogLocaleUtil;
import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.report.dtos.FormReport10Dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FormReport10Helper {

    private final CatalogService catalogService;

    public Object buildByParentCode(String parentCode, List<Form058> forms, LocalDate startDate, LocalDate endDate) {
        if (parentCode == null || parentCode.isBlank()) {
            return buildRegionReports(forms, startDate, endDate);
        }
        return buildDistrictReports(parentCode, forms, startDate, endDate);
    }

    public Object buildRegionReports(List<Form058> forms, LocalDate startDate, LocalDate endDate) {
        List<Catalog> regions = catalogService.findAllByType(CatalogType.state);

        return regions.stream()
                .map(region -> {
                    String code = region.getCode();
                    List<Form058> regionForms = filterByState(forms, code);
                    return buildDto(code, CatalogLocaleUtil.displayName(region), regionForms, startDate, endDate);
                })
                .toList();
    }

    public Object buildDistrictReports(String stateCode, List<Form058> forms, LocalDate startDate, LocalDate endDate) {
        List<Catalog> districts = catalogService.findAllByParentCode(stateCode);
        if (districts.isEmpty()) return List.of();

        return districts.stream()
                .map(dist -> {
                    String code = dist.getCode();
                    List<Form058> districtForms = filterByDistrict(forms, stateCode, code);
                    return buildDto(code, CatalogLocaleUtil.displayName(dist), districtForms, startDate, endDate);
                })
                .toList();
    }

    public Object buildOrganizations(String cityCode, List<Form058> forms, LocalDate startDate, LocalDate endDate) {
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

                    return buildDto(orgCode, orgName, orgForms, startDate, endDate);
                })
                .toList();
    }

    private FormReport10Dto buildDto(String code, String name, List<Form058> forms, LocalDate startDate, LocalDate endDate) {

        LocalDate prevStartDate = startDate.minusYears(1);
        LocalDate prevEndDate = endDate.minusYears(1);

        // current month / selected period
        Stat cmPrevTotal = calc(forms, prevStartDate, prevEndDate, false);
        Stat cmCurrTotal = calc(forms, startDate, endDate, false);

        Stat cmPrevChild = calc(forms, prevStartDate, prevEndDate, true);
        Stat cmCurrChild = calc(forms, startDate, endDate, true);

        // cumulative: year boshidan selected endDate gacha
        LocalDate currYearStart = LocalDate.of(endDate.getYear(), 1, 1);
        LocalDate prevYearStart = LocalDate.of(endDate.minusYears(1).getYear(), 1, 1);

        Stat cumPrevTotal = calc(forms, prevYearStart, prevEndDate, false);
        Stat cumCurrTotal = calc(forms, currYearStart, endDate, false);

        Stat cumPrevChild = calc(forms, prevYearStart, prevEndDate, true);
        Stat cumCurrChild = calc(forms, currYearStart, endDate, true);

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

    private Stat calc(List<Form058> forms, LocalDate from, LocalDate to, boolean children) {
        long abs = forms.stream()
                .filter(f -> f.getCreatedAt() != null)
                .filter(f -> {
                    LocalDate d = f.getCreatedAt().toLocalDateTime().toLocalDate();
                    return !d.isBefore(from) && !d.isAfter(to);
                })
                .filter(f -> isChildren(f, children))
                .count();

        double intensive = abs == 0 ? 0d : abs * 100_000d;

        return new Stat((int) abs, intensive);
    }

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

    private record Stat(Integer abs, double intensive) {}
}