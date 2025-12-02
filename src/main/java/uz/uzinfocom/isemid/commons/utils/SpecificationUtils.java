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
 * ✅ Universal Specification utility for dynamic query predicates.
 * Supports:
 * - Date range filtering
 * - Organization hierarchy filtering (state/city level)
 * - Sanepid access filtering
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
     */
    public static <T> Predicate buildLevelPredicate(
            Root<T> root,
            CriteriaBuilder cb,
            LevelType levelType,
            String orgStateCode,
            String orgCityCode,
            Long currentOrgId
    ) {
        Join<T, ?> createdOrgJoin = root.join("createdOrg", JoinType.LEFT);

        Predicate regionPredicate = buildLevelPredicateInternal(
                createdOrgJoin, cb, levelType, orgStateCode, orgCityCode
        );

        Predicate receiverPredicate = cb.and(
                cb.equal(root.get("receiverOrganizationId"), currentOrgId),
                buildLevelPredicateInternal(
                        createdOrgJoin, cb, levelType, orgStateCode, orgCityCode
                )
        );

        return cb.or(regionPredicate, receiverPredicate);
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
        Predicate predicate;

        switch (levelType) {

            case REGIONAL ->
                    predicate = cb.equal(orgPath.get("stateCode"), orgStateCode);

            case URBAN -> {
                if ("UZ-TK".equals(orgStateCode)) {
                    predicate = cb.equal(orgPath.get("stateCode"), orgStateCode);
                } else {
                    predicate = cb.and(
                            cb.equal(orgPath.get("cityCode"), orgCityCode),
                            cb.equal(orgPath.get("levelType"), LevelType.URBAN)
                    );
                }
            }

            case AREA ->
                    predicate = cb.and(
                            cb.equal(orgPath.get("cityCode"), orgCityCode),
                            cb.equal(orgPath.get("levelType"), LevelType.AREA)
                    );

            case DISTRICT, INTERDISTRICT, NOT_DEFINED ->
                    predicate = cb.and(
                            cb.equal(orgPath.get("cityCode"), orgCityCode),
                            orgPath.get("levelType").in(
                                    LevelType.DISTRICT,
                                    LevelType.INTERDISTRICT,
                                    LevelType.NOT_DEFINED
                            )
                    );

            default -> predicate = cb.conjunction();
        }

        return predicate;
    }

    /**
     * ✅ Adds an access-level predicate for Sanepid organizations
     * based on their hierarchy (state/city).
     * Works only if current org is of type {@link MedicalType#SANEPID_SERVICE}.
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

        Join<T, ?> createdOrgJoin = root.join("createdOrg", JoinType.LEFT);

        Predicate levelPredicate =
                buildLevelPredicate(createdOrgJoin, cb, levelType, stateCode, cityCode);

        predicates.add(levelPredicate);
    }
}
