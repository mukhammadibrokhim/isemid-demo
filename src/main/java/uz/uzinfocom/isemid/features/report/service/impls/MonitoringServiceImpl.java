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
        response.setRequestDate(LocalDate.now());

        // districtCode -> Item
        Map<String, MonitoringResponse.Item> byDistrict = new LinkedHashMap<>();

        // mkb cache: finalMkb10Code -> blockCode ("A00-A09" ko'rinish)
        Map<String, String> mkbToBlockCache = new HashMap<>();

        LocalDate now = LocalDate.now();

        for (Form058 f : forms) {
            String dCode = resolveDistrictCode(f); // createdOrg.cityCode
            MonitoringResponse.Item item = byDistrict.computeIfAbsent(dCode, dc -> {
                MonitoringResponse.Item it = new MonitoringResponse.Item();
                it.setDistrictCode(dc);
                it.setAgeGroups(new MonitoringResponse.AgeGroupCount());
                it.setDiseaseType(new LinkedHashMap<>());
                return it;
            });

            // total
            item.setTotalCases(item.getTotalCases() + 1);

            // status
            if (f.getStatus() == FormStatus.APPROVED) item.setConfirmed(item.getConfirmed() + 1);
            else item.setUnconfirmed(item.getUnconfirmed() + 1);

            // gender
            if (f.getPatient() != null && hasText(f.getPatient().getGenderCode())) {
                if ("male".equalsIgnoreCase(f.getPatient().getGenderCode())) item.setMale(item.getMale() + 1);
                else item.setFemale(item.getFemale() + 1);
            }

            // age groups
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

            // diseaseType (block range bo‘yicha)
            if (hasText(f.getFinalMkb10Code())) {
                String mkbCode = f.getFinalMkb10Code();

                // 1) Agar request mkb10Code bilan kelgan bo‘lsa, sizda spec allaqachon filter qiladi,
                //    lekin rasmga mos bo‘lishi uchun baribir mapga qo‘yamiz.
                // 2) Agar mkb10Code bo‘lmasa (ya’ni hamma kasalliklar), block bo‘yicha sanaymiz.
                String blockCode = mkbToBlockCache.computeIfAbsent(mkbCode, this::resolveBlockCodeOrSelf);

                item.getDiseaseType().merge(blockCode, 1, Integer::sum);
            }
        }

        response.setData(new ArrayList<>(byDistrict.values()));
        return response;
    }

    private String resolveDistrictCode(Form058 f) {
        if (f.getCreatedOrg() == null) return "-";
        if (hasText(f.getCreatedOrg().getCityCode())) return f.getCreatedOrg().getCityCode();
        return "-";
    }

    /**
     * finalMkb10Code -> block range ("A00-A09") ni topadi.
     * Topolmasa, o‘sha mkbCode’ni qaytaradi.
     */
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

            // block code odatda "A00-A09" kabi range bo‘ladi
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

            // period → createdAt
            SpecificationUtils.fromToDateFilter(
                    fromDate, toDate, root, cb, predicates, "createdAt"
            );

            // region / district
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

            // mkb10 (agar kelgan bo‘lsa, shu mkb10 bo‘yicha filtrlab beradi)
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
