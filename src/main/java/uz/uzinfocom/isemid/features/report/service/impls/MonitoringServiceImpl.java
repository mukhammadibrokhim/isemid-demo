package uz.uzinfocom.isemid.features.report.service.impls;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.utils.SpecificationUtils;
import uz.uzinfocom.isemid.features.classifier.models.Mkb10;
import uz.uzinfocom.isemid.features.classifier.repositories.Mkb10Repository;
import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.form058.models.enums.FormStatus;
import uz.uzinfocom.isemid.features.form058.reposiotries.Form058Repository;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.report.dtos.MonitoringResponse;
import uz.uzinfocom.isemid.features.report.service.MonitoringService;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.springframework.util.StringUtils.hasText;

@Service
@RequiredArgsConstructor
public class MonitoringServiceImpl implements MonitoringService {

    private final Form058Repository form058Repository;
    private final Mkb10Repository mkb10Repository;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Override
    public MonitoringResponse getMonitoring(
            String fromDate,
            String toDate,
            String regionCode,
            String districtCode,
            String mkb10Code
    ) {
        LocalDate from = parseDate(fromDate);
        LocalDate to = parseDate(toDate);

        Specification<Form058> spec =
                filterByCreatedDate(from, to, regionCode, districtCode, mkb10Code);

        List<Form058> forms = form058Repository.findAll(spec);

        MonitoringResponse response = new MonitoringResponse();
        response.setToDate(to != null ? to : LocalDate.now());

        Map<String, MonitoringResponse.Item> byLocation = new LinkedHashMap<>();

        Map<String, String> mkbToBlockCache = new HashMap<>();
        LocalDate now = LocalDate.now();

        for (Form058 f : forms) {
            String locCode = resolveLocationCode(f, districtCode);

            MonitoringResponse.Item item = byLocation.computeIfAbsent(locCode, code -> {
                MonitoringResponse.Item it = new MonitoringResponse.Item();
                it.setLocationCode(code);
                it.setAgeGroups(new MonitoringResponse.AgeGroupCount());
                it.setDiseaseType(new LinkedHashMap<>());
                return it;
            });

            item.setTotalCases(item.getTotalCases() + 1);

            if (f.getStatus() == FormStatus.APPROVED) item.setConfirmed(item.getConfirmed() + 1);
            else item.setUnconfirmed(item.getUnconfirmed() + 1);

            if (f.getPatient() != null && hasText(f.getPatient().getGenderCode())) {
                if ("male".equalsIgnoreCase(f.getPatient().getGenderCode())) item.setMale(item.getMale() + 1);
                else item.setFemale(item.getFemale() + 1);
            }

            if (f.getPatient() != null && f.getPatient().getBirthDate() != null) {
                int age = Period.between(f.getPatient().getBirthDate(), now).getYears();
                MonitoringResponse.AgeGroupCount ag = item.getAgeGroups();

                if (age <= 3) ag.setAge_0_3(ag.getAge_0_3() + 1);
                else if (age <= 6) ag.setAge_3_6(ag.getAge_3_6() + 1);
                else if (age <= 10) ag.setAge_6_10(ag.getAge_6_10() + 1);
                else if (age <= 17) ag.setAge_10_17(ag.getAge_10_17() + 1);
                else if (age <= 25) ag.setAge_18_25(ag.getAge_18_25() + 1);
                else if (age <= 45) ag.setAge_25_45(ag.getAge_25_45() + 1);
                else if (age <= 65) ag.setAge_45_65(ag.getAge_45_65() + 1);
                else ag.setAge_65_plus(ag.getAge_65_plus() + 1);
            }

            if (hasText(f.getFinalMkb10Code())) {
                String mkbCode = f.getFinalMkb10Code();
                String blockCode = mkbToBlockCache.computeIfAbsent(mkbCode, this::resolveBlockCodeOrSelf);
                item.getDiseaseType().merge(blockCode, 1, Integer::sum);
            }
        }

        response.setData(new ArrayList<>(byLocation.values()));
        return response;
    }

    private String resolveLocationCode(Form058 f, String districtCode) {
        if (f.getCreatedOrg() == null) return "-";

        if (hasText(districtCode)) {
            return hasText(f.getCreatedOrg().getCityCode()) ? f.getCreatedOrg().getCityCode() : "-";
        }

        return hasText(f.getCreatedOrg().getStateCode()) ? f.getCreatedOrg().getStateCode() : "-";
    }

    private String resolveBlockCodeOrSelf(String mkbCode) {
        if (!hasText(mkbCode)) return "-";

        Optional<Mkb10> opt = mkb10Repository.findByCode(mkbCode);
        if (opt.isEmpty()) return mkbCode;

        Mkb10 cur = opt.get();
        int guard = 0;

        while (cur != null && cur.getParentId() != null && guard++ < 10) {
            Optional<Mkb10> parentOpt = mkb10Repository.findById(cur.getParentId());
            if (parentOpt.isEmpty()) break;

            Mkb10 parent = parentOpt.get();

            if (hasText(parent.getCode()) && parent.getCode().contains("-")) {
                return parent.getCode();
            }
            cur = parent;
        }

        return mkbCode;
    }

    public static Specification<Form058> filterByCreatedDate(
            LocalDate fromDate,
            LocalDate toDate,
            String regionCode,
            String districtCode,
            String mkb10Code
    ) {
        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            SpecificationUtils.fromToDateFilter(
                    fromDate, toDate, root, cb, predicates, "createdAt"
            );

            if (hasText(regionCode) || hasText(districtCode)) {
                Join<Form058, Organization> createdOrgJoin =
                        root.join("createdOrg", JoinType.LEFT);

                if (hasText(regionCode)) {
                    predicates.add(cb.equal(createdOrgJoin.get("stateCode"), regionCode));
                }
                if (hasText(districtCode)) {
                    predicates.add(cb.equal(createdOrgJoin.get("cityCode"), districtCode));
                }
            }

            if (hasText(mkb10Code)) {
                predicates.add(cb.equal(root.get("finalMkb10Code"), mkb10Code));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private LocalDate parseDate(String date) {
        if (!hasText(date)) return null;
        return LocalDate.parse(date, FORMATTER);
    }
}
