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
     * Builds a {@link Predicate} for filtering by organization level,
     * considering both created organization and receiver organization.
     * This method ensures:
     * 1️⃣ Records created by organizations within the same city/state (depending on levelType)
     * 2️⃣ OR records received by the current organization (receiverOrganizationId)
     *
     * @param root          root entity (e.g. Form058)
     * @param cb            criteria builder
     * @param levelType     current org’s level
     * @param orgStateCode  current org’s state code
     * @param orgCityCode   current org’s city code
     * @param currentOrgId  current organization ID
     */
    public static <T> Predicate buildLevelPredicate(
            Root<T> root,
            CriteriaBuilder cb,
            LevelType levelType,
            String orgStateCode,
            String orgCityCode,
            Long currentOrgId
    ) {
        // 🔹 Join to createdOrg
        Join<T, ?> createdOrgJoin = root.join("createdOrg", JoinType.LEFT);

        // 🔹 Build base predicate by level
        Predicate levelPredicate = buildLevelPredicateInternal(
                createdOrgJoin, cb, levelType, orgStateCode, orgCityCode
        );

        // 🔹 Add receiverOrganizationId = currentOrgId
        Predicate receiverPredicate = cb.equal(root.get("receiverOrganizationId"), currentOrgId);

        // 🔹 Combine: createdOrg in scope OR received by me
        return cb.or(levelPredicate, receiverPredicate);
    }


    /**
     * Builds a {@link Predicate} for filtering by organization level
     * using a provided path (for nested joins).
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
     * Internal logic for level-based filtering.
     * Decides what organizations' data are visible depending on the user's level.
     */
    private static Predicate buildLevelPredicateInternal(
            Path<?> orgPath,
            CriteriaBuilder cb,
            LevelType levelType,
            String orgStateCode,
            String orgCityCode
    ) {
        Predicate regionFilter;
        Predicate levelFilter;

        switch (levelType) {
            case REPUBLICAN -> {
                regionFilter = cb.conjunction(); // sees all
                levelFilter = cb.conjunction();
            }
            case REGIONAL -> {
                regionFilter = cb.equal(orgPath.get("stateCode"), orgStateCode);
                levelFilter = cb.notEqual(orgPath.get("levelType"), LevelType.REPUBLICAN.name());
            }
            case URBAN -> {
                regionFilter = cb.equal(orgPath.get("cityCode"), orgCityCode);
                levelFilter = orgPath.get("levelType").in(
                        LevelType.URBAN.name(),
                        LevelType.DISTRICT.name(),
                        LevelType.NOT_DEFINED.name()
                );
            }
            case DISTRICT, AREA, INTERDISTRICT, NOT_DEFINED -> {
                regionFilter = cb.equal(orgPath.get("cityCode"), orgCityCode);
                levelFilter = orgPath.get("levelType").in(
                        LevelType.DISTRICT.name(),
                        LevelType.NOT_DEFINED.name(),
                        LevelType.AREA.name(),
                        LevelType.INTERDISTRICT.name()
                );
            }
            default -> {
                regionFilter = cb.conjunction();
                levelFilter = cb.conjunction();
            }
        }

        return cb.and(regionFilter, levelFilter);
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
