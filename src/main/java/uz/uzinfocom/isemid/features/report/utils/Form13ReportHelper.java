package uz.uzinfocom.isemid.features.report.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.uzinfocom.isemid.features.classifier.eums.CatalogType;
import uz.uzinfocom.isemid.features.classifier.models.Catalog;
import uz.uzinfocom.isemid.features.classifier.models.ManualReport;
import uz.uzinfocom.isemid.features.classifier.models.enums.ReportType;
import uz.uzinfocom.isemid.features.classifier.services.CatalogService;
import uz.uzinfocom.isemid.features.classifier.services.ManualReportService;
import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.report.dtos.Form13ReportDto;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class Form13ReportHelper {

    private final ManualReportService manualReportService;
    private final CatalogService catalogService;

    // ================================
    // Public Entry Points
    // ================================
    public Object buildByParentCode(String parentCode, List<Form058> forms) {

        List<Catalog> children = catalogService.findAllByParentCode(parentCode);

//        if (children == null || children.isEmpty()) {
//            return buildOrganizations(parentCode, forms);
//        }

        boolean hasCityLevelChildren = children.stream()
                .map(Catalog::getType)
                .anyMatch(type -> type.equals(CatalogType.neighborhood.name()));

        if (hasCityLevelChildren) {
            return buildOrganizations(parentCode, forms);
        }


        List<ManualReport> reports = getReports();
        List<Form13ReportDto> result = new ArrayList<>();

        for (Catalog child : children) {
            String childCode = safe(child.getCode());
            String childName = getDisplay(child);

            List<Form058> filtered = filter(forms, childCode);
            List<Form13ReportDto.DiseaseStatGroupDto> groups = buildGroups(reports, filtered);

            result.add(new Form13ReportDto(childCode, childName, groups));
        }

        return result;
    }

    public Object buildRegionReports(List<Form058> forms) {

        List<Catalog> regions = catalogService.findAllByType(CatalogType.state);
        List<ManualReport> reports = getReports();

        return regions.stream()
                .map(region -> {
                    String code = region.getCode();
                    List<Form058> regionForms = filter(forms, code);

                    return new Form13ReportDto(
                            code,
                            getDisplay(region),
                            buildGroups(reports, regionForms)
                    );
                })
                .toList();
    }

    public Object buildDistrictReports(String stateCode, List<Form058> forms) {

        List<Catalog> districts = catalogService.findAllByParentCode(stateCode);
        if (districts.isEmpty()) return List.of(); // no districts → empty list

        List<ManualReport> reports = getReports();
        List<Form13ReportDto> result = new ArrayList<>();

        for (Catalog dist : districts) {

            String districtCode = safe(dist.getCode());   // e.g. "AN-408"
            String districtName = getDisplay(dist);

            List<Form058> districtForms = forms.stream()
                    .filter(f -> f.getCreatedOrg() != null)
                    .filter(f -> {
                        String city = safe(f.getCreatedOrg().getCityCode());  // AN-408
                        String state = safe(f.getCreatedOrg().getStateCode()); // UZ-AN

                        return
                                districtCode.equals(city) ||             // exact match
                                        city.startsWith(districtCode) ||         // child organizations
                                        (city.isBlank() && state.equals(stateCode)); // fallback only if org doesn't have cityCode
                    })
                    .toList();

            List<Form13ReportDto.DiseaseStatGroupDto> groups = buildGroups(reports, districtForms);

            result.add(new Form13ReportDto(
                    districtCode,
                    districtName,
                    groups
            ));
        }

        return result;
    }


    public Object buildOrganizations(String districtCode, List<Form058> forms) {

        Set<String> orgCodes = forms.stream()
                .map(f -> safe(f.getCreatedOrg()).getCityCode())
                .filter(Objects::nonNull)
                .filter(code -> code.startsWith(districtCode))
                .collect(Collectors.toSet());

        if (orgCodes.isEmpty()) return List.of();

        List<ManualReport> reports = getReports();
        List<Form13ReportDto> result = new ArrayList<>();

        for (String orgCode : orgCodes) {
            List<Form058> orgForms = filter(forms, orgCode);
            String orgName = orgForms.isEmpty()
                    ? orgCode
                    : safe(orgForms.getFirst().getCreatedOrg()).getName();

            result.add(new Form13ReportDto(orgCode, orgName, buildGroups(reports, orgForms)));
        }

        return result;
    }

    // ================================
    // Statistic Factory
    // ================================
    private List<Form13ReportDto.DiseaseStatGroupDto> buildGroups(List<ManualReport> reports, List<Form058> forms) {
        return reports.stream()
                .map(rep -> new Form13ReportDto.DiseaseStatGroupDto(
                        rep.getNameUz(),
                        buildDiseaseStats(rep.getMkb10Codes(), forms)
                ))
                .toList();
    }

    private Form13ReportDto.Form13DiseaseStatDto buildDiseaseStats(Set<String> mkb10Codes, List<Form058> forms) {

        if (mkb10Codes == null || mkb10Codes.isEmpty() || forms.isEmpty())
            return emptyStat();

        int currentYear = LocalDate.now().getYear();
        int prevYear = currentYear - 1;

        long prevAdults = count(forms, mkb10Codes, prevYear, true);
        long prevChildren = count(forms, mkb10Codes, prevYear, false);

        long currAdults = count(forms, mkb10Codes, currentYear, true);
        long currChildren = count(forms, mkb10Codes, currentYear, false);

        return new Form13ReportDto.Form13DiseaseStatDto(
                prevAdults, prevChildren, prevAdults + prevChildren,
                currAdults, currChildren, currAdults + currChildren
        );
    }

    private long count(List<Form058> forms, Set<String> mkb10Codes, int year, boolean adults) {

        if (forms == null || forms.isEmpty() || mkb10Codes == null || mkb10Codes.isEmpty()) {
            return 0;
        }

        List<String> normalizedCodes = mkb10Codes.stream()
                .filter(Objects::nonNull)
                .map(this::safeCode)
                .toList();

        return forms.stream()
                .filter(f -> f != null && f.getCreatedAt() != null && f.getPatient() != null && f.getPatient().getBirthDate() != null)
                .filter(f -> {
                    try {
                        int createdYear = f.getCreatedAt().toLocalDateTime().getYear();
                        return createdYear == year;
                    } catch (Exception e) {
                        return false;
                    }
                })
                .filter(f -> {
                    String mkb = safeCode(f.getFinalMkb10Code());
                    return normalizedCodes.stream().anyMatch(mkb::startsWith);
                })
                .filter(f -> isAgeGroup(f.getPatient().getBirthDate(), f.getCreatedAt().toLocalDateTime().toLocalDate(), adults))
                .count();
    }

    private boolean isAgeGroup(LocalDate birthDate, LocalDate createdDate, boolean adults) {
        if (birthDate == null || createdDate == null) return false;

        int ageYears = java.time.Period.between(birthDate, createdDate).getYears();

        return adults == (ageYears >= 18);
    }


    // ================================
    // Filter
    // ================================
    private List<Form058> filter(List<Form058> forms, String locCode) {

        if (locCode == null || locCode.isBlank()) return List.of();

        String normalized = safe(locCode);

        return forms.stream()
                .filter(f -> f.getCreatedOrg() != null)
                .filter(f -> isLocationMatch(f.getCreatedOrg(), normalized))
                .toList();
    }

    private boolean isLocationMatch(Organization org, String code) {

        String state = safe(org.getStateCode());
        String city = safe(org.getCityCode());

        if (code.equals(state)) return true;

        if (code.equals(city)) return true;

        return !city.isBlank() && city.startsWith(code);
    }


    private String getDisplay(Catalog c) {
        return Optional.ofNullable(c.getNameUz()).filter(s -> !s.isBlank()).orElse("-");
    }

    private List<ManualReport> getReports() {
        return manualReportService.getManualReportsForForm(ReportType.FORM_13.name());
    }

    private String safe(String v) { return v == null ? "" : v.trim(); }
    private String safeCode(String v) { return safe(v).toUpperCase(); }
    private <T> T safe(T v) { return Objects.requireNonNullElse(v, null); }

    private Form13ReportDto.Form13DiseaseStatDto emptyStat() {
        return new Form13ReportDto.Form13DiseaseStatDto(0,0,0,0,0,0);
    }
}
