package uz.uzinfocom.isemid.features.report.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.uzinfocom.isemid.features.classifier.eums.CatalogType;
import uz.uzinfocom.isemid.features.classifier.models.Catalog;
import uz.uzinfocom.isemid.features.classifier.services.CatalogService;
import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.report.dtos.Form4_9ReportDto;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class Form4_9ReportHelper {

    private final CatalogService catalogService;

    // ===============================
    // GENERAL ENTRY POINTS
    // ===============================
    public Object buildByParentCode(String parentCode, List<Form058> forms) {

        List<Catalog> children = catalogService.findAllByParentCode(parentCode);

        boolean hasCityLevelChildren = children.stream()
                .map(Catalog::getType)
                .anyMatch(t -> t.equals(CatalogType.neighborhood.name()));

        if (hasCityLevelChildren) {
            return buildOrganizations(parentCode, forms);
        }

        List<Form4_9ReportDto> result = new ArrayList<>();

        for (Catalog child : children) {
            String code = safe(child.getCode());
            String name = getDisplay(child);
            List<Form058> filtered = filter(forms, code);

            result.add(buildReport(name, filtered));
        }

        return result;
    }


    public Object buildRegionReports(List<Form058> forms) {

        List<Catalog> regions = catalogService.findAllByType(CatalogType.state);

        return regions.stream()
                .map(region -> {
                    String code = region.getCode();
                    List<Form058> regionForms = filter(forms, code);

                    return buildReport(getDisplay(region), regionForms);
                })
                .toList();
    }


    public Object buildDistrictReports(String stateCode, List<Form058> forms) {

        List<Catalog> districts = catalogService.findAllByParentCode(stateCode);
        List<Form4_9ReportDto> result = new ArrayList<>();

        for (Catalog dist : districts) {

            String code = safe(dist.getCode());
            String name = getDisplay(dist);

            List<Form058> districtForms = forms.stream()
                    .filter(f -> f.getCreatedOrg() != null)
                    .filter(f -> {
                        String city = safe(f.getCreatedOrg().getCityCode());
                        String state = safe(f.getCreatedOrg().getStateCode());

                        return
                                code.equals(city) ||
                                city.startsWith(code) ||
                                (city.isBlank() && state.equals(stateCode));
                    })
                    .toList();

            result.add(buildReport(name, districtForms));
        }

        return result;
    }


    public Object buildOrganizations(String districtCode, List<Form058> forms) {

        Set<String> orgCodes = forms.stream()
                .map(f -> safe(f.getCreatedOrg()).getCityCode())
                .filter(Objects::nonNull)
                .filter(c -> c.startsWith(districtCode))
                .collect(Collectors.toSet());

        List<Form4_9ReportDto> result = new ArrayList<>();

        for (String code : orgCodes) {
            List<Form058> orgForms = filter(forms, code);

            String name = orgForms.isEmpty()
                    ? code
                    : safe(orgForms.getFirst().getCreatedOrg()).getName();

            result.add(buildReport(name, orgForms));
        }

        return result;
    }

    // ===============================
    // REPORT BUILDER FOR FORM 4_9
    // ===============================
    private Form4_9ReportDto buildReport(String name, List<Form058> forms) {

        Map<String, Long> stats = countByCategory(forms);

        return Form4_9ReportDto.builder()
                .name(name)
                .unorganized(stats.getOrDefault("NO_ORGANIZED", 0L))
                .organized(stats.getOrDefault("ORGANIZED", 0L))
                .pupils(stats.getOrDefault("STUDENT_SCHOOL", 0L))
                .secondaryInstitutions(stats.getOrDefault("MIDDLE_STUDENT", 0L))
                .institutions(stats.getOrDefault("STUDENT", 0L))
                .employee(stats.getOrDefault("WORKER", 0L))
                .medEmployee(stats.getOrDefault("MEDICAL_WORKER", 0L))
                .unEmployee(stats.getOrDefault("NOT_EMPLOYED", 0L))
                .pensioners(stats.getOrDefault("PENSIONER", 0L))
                .homelessPeople(stats.getOrDefault("UNSHELTRED", 0L))
                .build();
    }

    private Map<String, Long> countByCategory(List<Form058> forms) {
        return forms.stream()
                .filter(f -> f.getPatient() != null)
                .collect(Collectors.groupingBy(
                        f -> f.getPatient().getCategoryCode(),
                        Collectors.counting()
                ));
    }




    // ===============================
    // FILTERS (COPY FROM FORM-13)
    // ===============================
    private List<Form058> filter(List<Form058> forms, String locationCode) {

        if (locationCode == null || locationCode.isBlank()) return List.of();

        String normalized = safe(locationCode);

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
        return Optional.ofNullable(c.getNameUz())
                .filter(s -> !s.isBlank())
                .orElse("-");
    }

    private String safe(String v) { return v == null ? "" : v.trim(); }
    private <T> T safe(T v) { return Objects.requireNonNullElse(v, null); }
}
