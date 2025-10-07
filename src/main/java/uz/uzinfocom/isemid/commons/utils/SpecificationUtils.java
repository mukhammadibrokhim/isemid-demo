package uz.uzinfocom.isemid.commons.utils;

import jakarta.persistence.criteria.*;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.organization.models.enums.LevelType;
import uz.uzinfocom.isemid.features.organization.models.enums.MedicalType;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Utility class providing reusable JPA Specification helpers
 * for building dynamic query predicates (date range filters,
 * organization-level filters, etc.).
 */
public class SpecificationUtils {

    /**
     * Adds a date range filter to a predicate list.
     *
     * <ul>
     *   <li>Both {@code fromDate} and {@code toDate} present → {@code BETWEEN}</li>
     *   <li>Only {@code fromDate} present → {@code >= fromDate}</li>
     *   <li>Only {@code toDate} present → {@code <= toDate}</li>
     * </ul>
     */
    public static <T> void fromToDateFilter(LocalDate fromDate,
                                            LocalDate toDate,
                                            Root<T> root,
                                            CriteriaBuilder cb,
                                            List<Predicate> predicates,
                                            String fieldName) {
        if (fromDate != null && toDate != null) {
            predicates.add(cb.between(
                    root.get(fieldName),
                    Timestamp.valueOf(fromDate.atStartOfDay()),
                    Timestamp.valueOf(toDate.atTime(LocalTime.MAX))
            ));
        } else if (fromDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(
                    root.get(fieldName),
                    Timestamp.valueOf(fromDate.atStartOfDay())
            ));
        } else if (toDate != null) {
            predicates.add(cb.lessThanOrEqualTo(
                    root.get(fieldName),
                    Timestamp.valueOf(toDate.atTime(LocalTime.MAX))
            ));
        }
    }

    /**
     * Builds a {@link Predicate} for filtering by organization level.
     * This variant is used when the target entity contains a
     * {@code createdOrg} reference.
     *
     * @param root         root entity
     * @param cb           criteria builder
     * @param levelType    level of current organization
     * @param orgStateCode state code of current organization
     * @param orgCityCode  city code of current organization
     */
    public static <T> Predicate buildLevelPredicate(
            Root<T> root,
            CriteriaBuilder cb,
            LevelType levelType,
            String orgStateCode,
            String orgCityCode
    ) {
        Join<T, ?> createdOrgJoin = root.join("createdOrg", JoinType.LEFT);
        return buildLevelPredicateInternal(createdOrgJoin, cb, levelType, orgStateCode, orgCityCode);
    }

    /**
     * Builds a {@link Predicate} for filtering by organization level
     * using a provided path (e.g. for nested relations such as Address).
     *
     * @param path         path to filter
     * @param cb           criteria builder
     * @param levelType    level of current organization
     * @param orgStateCode state code of current organization
     * @param orgCityCode  city code of current organization
     */
    public static Predicate buildLevelPredicate(
            Path<?> path,
            CriteriaBuilder cb,
            LevelType levelType,
            String orgStateCode,
            String orgCityCode
    ) {
        return buildLevelPredicateInternal(path, cb, levelType, orgStateCode, orgCityCode);
    }

    /**
     * Internal method that implements the shared logic for
     * organization-level filtering.
     */
    private static Predicate buildLevelPredicateInternal(
            Path<?> orgPath,
            CriteriaBuilder cb,
            LevelType levelType,
            String orgStateCode,
            String orgCityCode
    ) {
        return switch (levelType) {
            case REPUBLICAN ->
                    cb.conjunction(); // No restriction (see all)

            case REGIONAL ->
                    cb.equal(orgPath.get("stateCode"), orgStateCode);

            case URBAN -> {
                if ("UZ-TK".equals(orgStateCode)) {
                    // Tashkent City: filter by state only
                    yield cb.equal(orgPath.get("stateCode"), orgStateCode);
                } else {
//                    // Other cities: filter by city, exclude regional orgs
//                    Predicate sameCity = cb.equal(orgPath.get("cityCode"), orgCityCode);
//                    Predicate notRegional = cb.notEqual(orgPath.get("levelType"), LevelType.REGIONAL.name());
//                    yield cb.and(sameCity, notRegional);

                    yield cb.equal(orgPath.get("cityCode"), orgCityCode);

                }
            }

            case DISTRICT, AREA, INTERDISTRICT, NOT_DEFINED ->
                    cb.equal(orgPath.get("cityCode"), orgCityCode);
        };
    }

    /**
     * Adds an access-level predicate for Sanepid organizations
     * based on their hierarchy (state/city).
     */
    public static <T> void addSanepidAccessPredicate(
            Root<T> root,
            CriteriaBuilder cb,
            Organization userOrg,
            List<Predicate> predicates
    ) {
        if (userOrg == null) return;
        if (userOrg.getMedicalType() != MedicalType.SANEPID_SERVICE) return;

        String stateCode = userOrg.getStateCode();
        String cityCode = userOrg.getCityCode();
        LevelType levelType = userOrg.getLevelType();

        Predicate levelPredicate =
                buildLevelPredicate(root, cb, levelType, stateCode, cityCode);
        predicates.add(levelPredicate);
    }
}
