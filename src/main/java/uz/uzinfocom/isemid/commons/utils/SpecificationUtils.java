package uz.uzinfocom.isemid.commons.utils;


import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import uz.uzinfocom.isemid.features.organization.models.enums.LevelType;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class SpecificationUtils {

    /**
     * Adds a date range filter to the given list of predicates.
     * <p>
     * This method checks whether the given {@code fromDate} and {@code toDate} are present
     * and creates a {@link Predicate} accordingly:
     * <ul>
     *     <li>If both {@code fromDate} and {@code toDate} are provided, it adds a BETWEEN predicate.</li>
     *     <li>If only {@code fromDate} is provided, it adds a GREATER THAN OR EQUAL predicate.</li>
     *     <li>If only {@code toDate} is provided, it adds a LESS THAN OR EQUAL predicate.</li>
     * </ul>
     *
     * @param fromDate   the start date (inclusive), may be null
     * @param toDate     the end date (inclusive), may be null
     * @param root       the JPA root entity
     * @param cb         the {@link CriteriaBuilder} used to construct predicates
     * @param predicates the list of predicates to which the generated filter will be added
     * @param fieldName  the name of the field in the entity representing the date/time to filter by
     * @param <T>        the entity type
     */
    public static <T> void fromToDateFilter(LocalDate fromDate,
                                            LocalDate toDate,
                                            Root<T> root,
                                            CriteriaBuilder cb,
                                            List<Predicate> predicates,
                                            String fieldName) {

        // If both fromDate and toDate are provided → filter records between those dates
        if (fromDate != null && toDate != null) {
            predicates.add(cb.between(
                    root.get(fieldName),
                    Timestamp.valueOf(fromDate.atStartOfDay()),
                    Timestamp.valueOf(toDate.atTime(LocalTime.MAX))
            ));
        }
        // If only fromDate is provided → filter records created on or after this date
        else if (fromDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(
                    root.get(fieldName),
                    Timestamp.valueOf(fromDate.atStartOfDay())
            ));
        }
        // If only toDate is provided → filter records created on or before this date
        else if (toDate != null) {
            predicates.add(cb.lessThanOrEqualTo(
                    root.get(fieldName),
                    Timestamp.valueOf(toDate.atTime(LocalTime.MAX))
            ));
        }
    }


    /**
     * Builds a generic predicate for filtering by organization level.
     *
     * @param root         the JPA root entity
     * @param cb           the CriteriaBuilder instance
     * @param levelType    the level type (REPUBLICAN, REGIONAL, URBAN, DISTRICT, etc.)
     * @param orgStateCode the organization's state code
     * @param orgCityCode  the organization's city code
     * @param <T>          the entity type
     * @return a Predicate representing the organization-level filter
     */
    public static <T> Predicate buildLevelPredicate(Root<T> root,
                                                    CriteriaBuilder cb,
                                                    LevelType levelType,
                                                    String orgStateCode,
                                                    String orgCityCode) {

        return switch (levelType) {
            case REPUBLICAN -> cb.conjunction(); // no restriction, sees all
            case REGIONAL -> cb.equal(root.get("createdOrg").get("stateCode"), orgStateCode);
            case URBAN -> {
                if ("UZ-TK".equals(orgStateCode)) {
                    yield cb.equal(root.get("createdOrg").get("stateCode"), orgStateCode);
                } else {
                    yield cb.equal(root.get("createdOrg").get("cityCode"), orgCityCode);
                }
            }
            case DISTRICT, AREA, INTERDISTRICT, NOT_DEFINED ->
                    cb.equal(root.get("createdOrg").get("cityCode"), orgCityCode);
        };
    }


    // ✅ NEW: buildLevelPredicate for a custom path (like "addresses")
    public static Predicate buildLevelPredicate(Path<?> path,
                                                CriteriaBuilder cb,
                                                LevelType levelType,
                                                String orgStateCode,
                                                String orgCityCode) {
        return switch (levelType) {
            case REPUBLICAN -> cb.conjunction();
            case REGIONAL -> cb.equal(path.get("stateCode"), orgStateCode);
            case URBAN -> {
                if ("UZ-TK".equals(orgStateCode)) {
                    yield cb.equal(path.get("stateCode"), orgStateCode);
                } else {
                    yield cb.equal(path.get("cityCode"), orgCityCode);
                }
            }
            case DISTRICT, AREA, INTERDISTRICT, NOT_DEFINED ->
                    cb.equal(path.get("cityCode"), orgCityCode);
        };
    }

    /**
     * Adds a level-based access predicate for Sanepid organizations to the given predicates list.
     *
     * @param root       the JPA root entity
     * @param cb         the CriteriaBuilder
     * @param userOrg    the current user's organization
     * @param predicates the list to which the predicate will be added
     * @param <T>        the entity type
     */
    public static <T> void addSanepidAccessPredicate(Root<T> root,
                                                     CriteriaBuilder cb,
                                                     uz.uzinfocom.isemid.features.organization.models.Organization userOrg,
                                                     List<Predicate> predicates) {
        if (userOrg == null) return;

        // Only apply for Sanepid organizations
        if (userOrg.getMedicalType() != uz.uzinfocom.isemid.features.organization.models.enums.MedicalType.SANEPID_SERVICE)
            return;

        String stateCode = userOrg.getState() != null ? userOrg.getState().getCode() : null;
        String cityCode = userOrg.getCity() != null ? userOrg.getCity().getCode() : null;
        var levelType = userOrg.getLevelType();

        // Build level predicate
        Predicate levelPredicate = buildLevelPredicate(root, cb, levelType, stateCode, cityCode);
        predicates.add(levelPredicate);
    }

}
