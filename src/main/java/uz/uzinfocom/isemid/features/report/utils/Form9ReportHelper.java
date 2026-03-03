package uz.uzinfocom.isemid.features.report.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.classifier.eums.CatalogType;
import uz.uzinfocom.isemid.features.classifier.models.Catalog;
import uz.uzinfocom.isemid.features.classifier.services.CatalogService;
import uz.uzinfocom.isemid.features.classifier.utils.CatalogLocaleUtil;
import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.report.dtos.Form9ReportDto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
@Component
@RequiredArgsConstructor
public class Form9ReportHelper {

    private final CatalogService catalogService;
    private final MessageUtil messageUtil;


    /* =============================
         ENTRY POINT
    ============================= */
    public Object buildByParentCode(String parentCode, List<Form058> forms) {

        Catalog catalog = catalogService.findByCode(parentCode);

        if (catalog == null) return buildRegionReports(forms);

        return switch (CatalogType.valueOf(catalog.getType())) {
            case state -> buildDistrictReports(parentCode, forms);
            case city -> buildOrganizations(parentCode, forms);
            default -> buildRegionReports(forms);
        };
    }


    /* =============================
         REGION LEVEL
    ============================= */
    public Object buildRegionReports(List<Form058> forms) {

        List<Catalog> regions = catalogService.findAllByType(CatalogType.state);
        List<Form9ReportDto> result = new ArrayList<>();

        for (Catalog region : regions) {

            String code = region.getCode();
            String name = CatalogLocaleUtil.displayName(region);

            List<Form058> regionForms = forms.stream()
                    .filter(f -> f.getCreatedOrg() != null &&
                            code.equals(f.getCreatedOrg().getStateCode()))
                    .toList();

            result.add(buildSingle(code, name, regionForms));
        }

        return result;
    }


    /* =============================
         DISTRICT LEVEL
    ============================= */
    public Object buildDistrictReports(String stateCode, List<Form058> forms) {

        List<Catalog> districts = catalogService.findAllByParentCode(stateCode);
        List<Form9ReportDto> result = new ArrayList<>();

        for (Catalog district : districts) {

            String code = district.getCode();
            String name = CatalogLocaleUtil.displayName(district);

            List<Form058> districtForms = forms.stream()
                    .filter(f -> f.getCreatedOrg() != null &&
                            code.equals(f.getCreatedOrg().getCityCode()))
                    .toList();

            result.add(buildSingle(code, name, districtForms));
        }

        return result;
    }


    /* =============================
         ORGANIZATION LEVEL
    ============================= */
    public Object buildOrganizations(String cityCode, List<Form058> forms) {

        Set<String> orgIds = forms.stream()
                .filter(f -> f.getCreatedOrg() != null &&
                        cityCode.equals(f.getCreatedOrg().getCityCode()))
                .map(f -> f.getCreatedOrg().getUuid().toString())
                .collect(Collectors.toSet());

        List<Form9ReportDto> result = new ArrayList<>();

        for (String orgId : orgIds) {

            String orgName = forms.stream()
                    .filter(f -> f.getCreatedOrg() != null &&
                            orgId.equals(f.getCreatedOrg().getUuid().toString()))
                    .map(f -> f.getCreatedOrg().getName())
                    .findFirst()
                    .orElse("Noma'lum tashkilot");

            List<Form058> orgForms = forms.stream()
                    .filter(f -> f.getCreatedOrg() != null &&
                            orgId.equals(f.getCreatedOrg().getUuid().toString()))
                    .toList();

            result.add(buildSingle(orgId, orgName, orgForms));
        }

        return result;
    }


    /* =============================
         BUILD MONTHLY REPORT (12 MONTH)
    ============================= */
    private Form9ReportDto buildSingle(String code, String name, List<Form058> forms) {

        int currYear = LocalDate.now().getYear();
        int prevYear = currYear - 1;

        List<Form9ReportDto.Form9RowDto> rows = new ArrayList<>();

        long regCurrTotal = 0, regPrevTotal = 0;
        long hospCurrTotal = 0, hospPrevTotal = 0;

        for (int month = 1; month <= 12; month++) {

            long regCurr = countRegistered(forms, currYear, month);
            long regPrev = countRegistered(forms, prevYear, month);

            long hospCurr = countHospitalized(forms, currYear, month);
            long hospPrev = countHospitalized(forms, prevYear, month);

            regCurrTotal += regCurr;
            regPrevTotal += regPrev;
            hospCurrTotal += hospCurr;
            hospPrevTotal += hospPrev;

            rows.add(Form9ReportDto.Form9RowDto.builder()
                    .monthName(getMonthName(month))
                    .regCurr(regCurr)
                    .regPrev(regPrev)
                    .regDiff(regCurr - regPrev)
                    .hospCurr(hospCurr)
                    .hospPrev(hospPrev)
                    .hospDiff(hospCurr - hospPrev)
                    .build());
        }

        Form9ReportDto.Total yearly = Form9ReportDto.Total.builder()
                .regCurrTotal(regCurrTotal)
                .regPrevTotal(regPrevTotal)
                .regDiffTotal(regCurrTotal - regPrevTotal)
                .hospCurrTotal(hospCurrTotal)
                .hospPrevTotal(hospPrevTotal)
                .hospDiffTotal(hospCurrTotal - hospPrevTotal)
                .build();

        return Form9ReportDto.builder()
                .locationCode(code)
                .locationName(name)
                .total(yearly)
                .rows(rows)
                .build();
    }


    /* =============================
         COUNT REGISTERED
    ============================= */
    private long countRegistered(List<Form058> forms, int year, int month) {
        return forms.stream()
                .filter(f -> f.getCreatedAt() != null)
                .filter(f -> {
                    LocalDate date = f.getCreatedAt().toLocalDateTime().toLocalDate();
                    return date.getYear() == year &&
                            date.getMonthValue() == month;
                })
                .count();
    }


    /* =============================
         COUNT HOSPITALIZED
    ============================= */
    private long countHospitalized(List<Form058> forms, int year, int month) {
        return forms.stream()
                .filter(this::isHospitalized)
                .filter(f -> f.getCreatedAt() != null)
                .filter(f -> {
                    LocalDate date = f.getCreatedAt().toLocalDateTime().toLocalDate();
                    return date.getYear() == year &&
                            date.getMonthValue() == month;
                })
                .count();
    }


    /* =============================
          IS HOSPITALIZED
    ============================= */
    private boolean isHospitalized(Form058 f) {
        return f.getHospitalPlaceId() != null &&
                f.getAdmissionDate() != null;
    }

    /* =============================
          LOCALE MONTH NAME
    ============================= */
    private String getMonthName(int month) {
        return messageUtil.get("month." + month);
    }
}
