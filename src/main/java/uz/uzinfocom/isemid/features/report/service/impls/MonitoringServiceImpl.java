package uz.uzinfocom.isemid.features.report.service.impls;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.utils.SpecificationUtils;
import uz.uzinfocom.isemid.features.classifier.eums.CatalogType;
import uz.uzinfocom.isemid.features.classifier.models.Catalog;
import uz.uzinfocom.isemid.features.classifier.models.Mkb10;
import uz.uzinfocom.isemid.features.classifier.repositories.Mkb10Repository;
import uz.uzinfocom.isemid.features.classifier.services.CatalogService;
import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.form058.models.enums.FormStatus;
import uz.uzinfocom.isemid.features.form058.reposiotries.Form058Repository;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.report.dtos.MonitoringResponse;
import uz.uzinfocom.isemid.features.report.service.MonitoringService;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.util.StringUtils.hasText;

@Slf4j
@Service
@RequiredArgsConstructor
public class MonitoringServiceImpl implements MonitoringService {

    private final Form058Repository form058Repository;
    private final Mkb10Repository mkb10Repository;
    private final CatalogService catalogService;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Override
    public MonitoringResponse getMonitoring(
            String fromDate,
            String toDate,
            String regionCode,
            String mkb10Code
    ) {
        LocalDate from = parseDate(fromDate);
        LocalDate to = parseDate(toDate);

        Specification<Form058> spec = filterByCreatedDate(from, to, regionCode, mkb10Code);
        List<Form058> forms = form058Repository.findAll(spec);

        log.info("Monitoring forms count = {}", forms.size());

        MonitoringResponse response = new MonitoringResponse();
        response.setToDate(to != null ? to : LocalDate.now());

        Map<String, MonitoringResponse.Item> byLocation = initLocationMap(regionCode);
        Map<String, String> mkbToBlockCache = new HashMap<>();

        LocalDate now = LocalDate.now();

        for (Form058 form : forms) {
            String locCode = resolveLocationCode(form, regionCode);

            MonitoringResponse.Item item = byLocation.computeIfAbsent(locCode, this::newItem);

            item.setTotalCases(item.getTotalCases() + 1);

            if (form.getStatus() == FormStatus.APPROVED) {
                item.setConfirmed(item.getConfirmed() + 1);
            } else {
                item.setUnconfirmed(item.getUnconfirmed() + 1);
            }

            if (form.getPatient() != null && hasText(form.getPatient().getGenderCode())) {
                String genderCode = form.getPatient().getGenderCode();

                if ("male".equalsIgnoreCase(genderCode)) {
                    item.setMale(item.getMale() + 1);
                } else if ("female".equalsIgnoreCase(genderCode)) {
                    item.setFemale(item.getFemale() + 1);
                }
            }

            if (form.getPatient() != null && form.getPatient().getBirthDate() != null) {
                int age = Period.between(form.getPatient().getBirthDate(), now).getYears();
                fillAgeGroups(item.getAgeGroups(), age);
            }

            if (hasText(form.getFinalMkb10Code())) {
                String mkbCode = form.getFinalMkb10Code().trim();

                String blockCode = mkbToBlockCache.computeIfAbsent(
                        mkbCode,
                        this::resolveBlockCodeOrSelf
                );

                if (hasText(blockCode) && !"-".equals(blockCode)) {
                    item.getDiseaseType().merge(blockCode, 1, Integer::sum);
                }
            }
        }

        response.setData(new ArrayList<>(byLocation.values()));
        return response;
    }

    private Map<String, MonitoringResponse.Item> initLocationMap(String regionCode) {
        Map<String, MonitoringResponse.Item> byLocation = new LinkedHashMap<>();

        if (hasText(regionCode)) {
            List<Catalog> districts = catalogService.findAllByTypeAndParentCode(CatalogType.city, regionCode);

            for (Catalog district : districts) {
                if (district != null && hasText(district.getCode())) {
                    byLocation.put(district.getCode(), newItem(district.getCode()));
                }
            }

            return byLocation;
        }

        List<Catalog> regions = catalogService.findAllByType(CatalogType.state);

        for (Catalog region : regions) {
            if (region != null && hasText(region.getCode())) {
                byLocation.put(region.getCode(), newItem(region.getCode()));
            }
        }

        return byLocation;
    }

    private MonitoringResponse.Item newItem(String locationCode) {
        MonitoringResponse.Item item = new MonitoringResponse.Item();
        item.setLocationCode(locationCode);
        item.setTotalCases(0);
        item.setConfirmed(0);
        item.setUnconfirmed(0);
        item.setMale(0);
        item.setFemale(0);
        item.setAgeGroups(new MonitoringResponse.AgeGroupCount());
        item.setDiseaseType(new LinkedHashMap<>());
        return item;
    }

    private void fillAgeGroups(MonitoringResponse.AgeGroupCount ag, int age) {
        if (age <= 3) ag.setAge_0_3(ag.getAge_0_3() + 1);
        else if (age <= 6) ag.setAge_3_6(ag.getAge_3_6() + 1);
        else if (age <= 10) ag.setAge_6_10(ag.getAge_6_10() + 1);
        else if (age <= 17) ag.setAge_10_17(ag.getAge_10_17() + 1);
        else if (age <= 25) ag.setAge_18_25(ag.getAge_18_25() + 1);
        else if (age <= 45) ag.setAge_25_45(ag.getAge_25_45() + 1);
        else if (age <= 65) ag.setAge_45_65(ag.getAge_45_65() + 1);
        else ag.setAge_65_plus(ag.getAge_65_plus() + 1);
    }

    private String resolveLocationCode(Form058 form, String regionCode) {
        if (form.getCreatedOrg() == null) {
            return "-";
        }

        Organization org = form.getCreatedOrg();

        if (hasText(regionCode)) {
            return hasText(org.getCityCode()) ? org.getCityCode() : "-";
        }

        return hasText(org.getStateCode()) ? org.getStateCode() : "-";
    }

    private String resolveBlockCodeOrSelf(String mkbCode) {
        if (!hasText(mkbCode)) {
            return "-";
        }

        String cleanedCode = mkbCode.trim();

        if ("string".equalsIgnoreCase(cleanedCode)) {
            return "-";
        }

        Optional<Mkb10> opt = mkb10Repository.findByCode(cleanedCode);
        if (opt.isEmpty()) {
            return formatMkbCode(cleanedCode);
        }

        Mkb10 current = opt.get();
        int guard = 0;

        while (guard++ < 20) {
            String currentCode = current.getCode();

            if (hasText(currentCode)) {
                String trimmedCurrentCode = currentCode.trim();

                if (trimmedCurrentCode.contains("-")) {
                    return formatMkbCode(trimmedCurrentCode);
                }
            }

            if (current.getParentId() == null) {
                break;
            }

            Optional<Mkb10> parentOpt = mkb10Repository.findById(current.getParentId());
            if (parentOpt.isEmpty()) {
                break;
            }

            current = parentOpt.get();
        }

        return formatMkbCode(cleanedCode);
    }

    private String formatMkbCode(String code) {
        if (!hasText(code)) {
            return "-";
        }

        String trimmed = code.trim();

        if ("string".equalsIgnoreCase(trimmed)) {
            return "-";
        }

        if (trimmed.startsWith("(") && trimmed.endsWith(")")) {
            return trimmed;
        }

        return "(" + trimmed + ")";
    }

    public static Specification<Form058> filterByCreatedDate(
            LocalDate fromDate,
            LocalDate toDate,
            String regionCode,
            String mkb10Code
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            SpecificationUtils.fromToDateFilter(
                    fromDate, toDate, root, cb, predicates, "createdAt"
            );

            Join<Form058, Organization> createdOrgJoin = root.join("createdOrg", JoinType.LEFT);

            if (hasText(regionCode)) {
                predicates.add(cb.equal(createdOrgJoin.get("stateCode"), regionCode));
            }

            if (hasText(mkb10Code)) {
                predicates.add(cb.equal(root.get("finalMkb10Code"), mkb10Code));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private LocalDate parseDate(String date) {
        if (!hasText(date)) {
            return null;
        }
        return LocalDate.parse(date, FORMATTER);
    }
}