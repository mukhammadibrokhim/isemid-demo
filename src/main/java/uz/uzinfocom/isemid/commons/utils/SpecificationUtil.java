package uz.uzinfocom.isemid.commons.utils;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.organization.models.Organization;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class SpecificationUtil {
    private SpecificationUtil() {
    }

    public static Predicate buildOrganizationLevelPredicate(
            Root<Form058> root,
            CriteriaBuilder cb,
            Organization org
    ) {
        if (org == null || org.getLevelType() == null) {
            return cb.conjunction();
        }

        var level = org.getLevelType();
        var stateCode = org.getStateCode();
        var cityCode = org.getCityCode();

        return switch (level) {
            case REPUBLICAN -> cb.conjunction();
            case REGIONAL -> cb.equal(root.get("createdOrg").get("stateCode"), stateCode);
            case URBAN -> {
                if ("UZ-TO".equals(stateCode)) {
                    yield cb.equal(root.get("createdOrg").get("stateCode"), stateCode);
                } else {
                    yield cb.equal(root.get("createdOrg").get("cityCode"), cityCode);
                }
            }
            case DISTRICT, AREA, INTERDISTRICT, NOT_DEFINED ->
                    cb.equal(root.get("createdOrg").get("cityCode"), cityCode);
        };
    }

    public static void fromToFilter(LocalDate fromDate, LocalDate toDate, Root<Form058> root, CriteriaBuilder cb,
                                    List<Predicate> predicates) {
        if (fromDate != null && toDate != null) {
            predicates.add(cb.between(
                    root.get("createdAt"),
                    Timestamp.valueOf(fromDate.atStartOfDay()),
                    Timestamp.valueOf(toDate.atTime(LocalTime.MAX))
            ));
        } else if (fromDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(
                    root.get("createdAt"),
                    Timestamp.valueOf(fromDate.atStartOfDay())
            ));
        } else if (toDate != null) {
            predicates.add(cb.lessThanOrEqualTo(
                    root.get("createdAt"),
                    Timestamp.valueOf(toDate.atTime(LocalTime.MAX))
            ));
        }
    }
}
