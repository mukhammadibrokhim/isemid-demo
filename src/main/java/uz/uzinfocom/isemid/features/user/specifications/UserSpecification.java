package uz.uzinfocom.isemid.features.user.specifications;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.organization.models.enums.LevelType;
import uz.uzinfocom.isemid.features.organization.models.enums.MedicalType;
import uz.uzinfocom.isemid.features.user.dtos.requests.UserFilterRequest;
import uz.uzinfocom.isemid.features.user.models.Role;
import uz.uzinfocom.isemid.features.user.models.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import static org.springframework.util.StringUtils.hasText;
import static uz.uzinfocom.isemid.commons.configs.security.util.SecurityUtils.isSuperAdmin;

public final class UserSpecification {

    private static final String NNUZB = "nnuzb";
    private static final String FIRST_NAME = "firstName";
    private static final String LAST_NAME = "lastName";
    private static final String MIDDLE_NAME = "middleName";
    private static final String PHONE_NUMBER = "phoneNumber";
    private static final String ACTIVE = "active";

    private static final String ORGANIZATIONS = "organizations";
    private static final String ROLES = "roles";

    private static final String ID = "id";
    private static final String STATE_CODE = "stateCode";
    private static final String CITY_CODE = "cityCode";
    private static final String MEDICAL_TYPE = "medicalType";

    private static final String TASHKENT_STATE_CODE = "UZ-TK";

    private UserSpecification() {
    }

    public static Specification<User> build(
            UserFilterRequest filter,
            User currentUser
    ) {
        Objects.requireNonNull(
                filter,
                "UserFilterRequest must not be null"
        );

        Objects.requireNonNull(
                currentUser,
                "Current user must not be null"
        );

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            /*
             * Shaxsga tegishli filterlar.
             * Qiymat null yoki blank bo‘lsa predicate qo‘shilmaydi.
             */
            addExactPredicate(
                    predicates,
                    root.get(NNUZB),
                    filter.getNnuzb(),
                    cb
            );

            addContainsIgnoreCasePredicate(
                    predicates,
                    root.get(FIRST_NAME),
                    filter.getFirstName(),
                    cb
            );

            addContainsIgnoreCasePredicate(
                    predicates,
                    root.get(LAST_NAME),
                    filter.getLastName(),
                    cb
            );

            addContainsIgnoreCasePredicate(
                    predicates,
                    root.get(MIDDLE_NAME),
                    filter.getMiddleName(),
                    cb
            );

            addExactPredicate(
                    predicates,
                    root.get(PHONE_NUMBER),
                    filter.getPhoneNumber(),
                    cb
            );

            /*
             * Organization bo‘yicha request filter:
             *
             * - stateCode
             * - cityCode
             * - medicalType
             *
             * Barcha shartlar bitta Organization ichida mos kelishi kerak.
             */
            addOrganizationFilterPredicate(
                    predicates,
                    root,
                    query,
                    cb,
                    filter.getOrganizationStateCode(),
                    filter.getOrganizationCityCode(),
                    filter.getUserOrgMedicalTypes()
            );

            /*
             * Role ID bo‘yicha filter.
             */
            addRoleIdsPredicate(
                    predicates,
                    root,
                    query,
                    cb,
                    filter.getRoleIds()
            );

            /*
             * Aktiv/noaktiv filter.
             */
            if (filter.getActive() != null) {
                predicates.add(
                        cb.equal(
                                root.get(ACTIVE),
                                filter.getActive()
                        )
                );
            }

            /*
             * Bu request filter emas.
             * Non-super-admin uchun security scope doim ishlaydi.
             */
            if (!isSuperAdmin(currentUser)) {
                predicates.add(
                        buildAccessScopePredicate(
                                root,
                                query,
                                cb,
                                currentUser
                        )
                );
            }

            return cb.and(
                    predicates.toArray(Predicate[]::new)
            );
        };
    }

    /**
     * Hudud va muassasa turi bo‘yicha filter.
     *
     * EXISTS ishlatiladi, chunki User ichida stateCode yoki medicalType yo‘q.
     * Ular User.organizations relationidagi Organization entity ichida.
     */
    private static void addOrganizationFilterPredicate(
            List<Predicate> predicates,
            Root<User> root,
            CriteriaQuery<?> query,
            CriteriaBuilder cb,
            String organizationStateCode,
            String organizationCityCode,
            Set<MedicalType> medicalTypes
    ) {
        boolean hasStateCode = hasText(organizationStateCode);
        boolean hasCityCode = hasText(organizationCityCode);
        boolean hasMedicalTypes =
                medicalTypes != null && !medicalTypes.isEmpty();

        /*
         * Organization filterlaridan birortasi ham kelmagan bo‘lsa
         * subquery yaratilmaydi.
         */
        if (!hasStateCode && !hasCityCode && !hasMedicalTypes) {
            return;
        }

        Subquery<Integer> organizationExists =
                query.subquery(Integer.class);

        Root<User> correlatedUser =
                organizationExists.correlate(root);

        Join<User, Organization> organizationJoin =
                correlatedUser.join(
                        ORGANIZATIONS,
                        JoinType.INNER
                );

        List<Predicate> organizationPredicates =
                new ArrayList<>();

        if (hasStateCode) {
            organizationPredicates.add(
                    cb.equal(
                            cb.upper(
                                    organizationJoin.<String>get(STATE_CODE)
                            ),
                            normalizeCode(organizationStateCode)
                    )
            );
        }

        if (hasCityCode) {
            organizationPredicates.add(
                    cb.equal(
                            cb.upper(
                                    organizationJoin.<String>get(CITY_CODE)
                            ),
                            normalizeCode(organizationCityCode)
                    )
            );
        }

        if (hasMedicalTypes) {
            organizationPredicates.add(
                    organizationJoin
                            .get(MEDICAL_TYPE)
                            .in(medicalTypes)
            );
        }

        organizationExists
                .select(cb.literal(1))
                .where(
                        cb.and(
                                organizationPredicates.toArray(
                                        Predicate[]::new
                                )
                        )
                );

        predicates.add(
                cb.exists(organizationExists)
        );
    }

    /**
     * User.roles.id bo‘yicha ANY role filter.
     *
     * Masalan roleIds=[2, 5] bo‘lsa,
     * userda 2 yoki 5 role mavjud bo‘lsa natijaga kiradi.
     */
    private static void addRoleIdsPredicate(
            List<Predicate> predicates,
            Root<User> root,
            CriteriaQuery<?> query,
            CriteriaBuilder cb,
            Set<Long> roleIds
    ) {
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }

        Subquery<Integer> roleExists =
                query.subquery(Integer.class);

        Root<User> correlatedUser =
                roleExists.correlate(root);

        Join<User, Role> roleJoin =
                correlatedUser.join(
                        ROLES,
                        JoinType.INNER
                );

        roleExists
                .select(cb.literal(1))
                .where(
                        roleJoin.get(ID).in(roleIds)
                );

        predicates.add(
                cb.exists(roleExists)
        );
    }

    /**
     * Current user organization leveliga qarab access scope.
     */
    private static Predicate buildAccessScopePredicate(
            Root<User> root,
            CriteriaQuery<?> query,
            CriteriaBuilder cb,
            User currentUser
    ) {
        List<Organization> currentUserOrganizations =
                currentUser.getOrganizations();

        /*
         * Fail-closed:
         * non-super-admin organizationga biriktirilmagan bo‘lsa
         * hech qanday userni ko‘rmaydi.
         */
        if (currentUserOrganizations == null
                || currentUserOrganizations.isEmpty()) {
            return cb.disjunction();
        }

        /*
         * Birorta REPUBLICAN organization mavjud bo‘lsa
         * hududiy scope cheklanmaydi.
         */
        boolean hasRepublicanScope =
                currentUserOrganizations.stream()
                        .filter(Objects::nonNull)
                        .map(Organization::getLevelType)
                        .filter(Objects::nonNull)
                        .anyMatch(LevelType.REPUBLICAN::equals);

        if (hasRepublicanScope) {
            return cb.conjunction();
        }

        Subquery<Integer> organizationExists =
                query.subquery(Integer.class);

        Root<User> correlatedUser =
                organizationExists.correlate(root);

        Join<User, Organization> targetOrganization =
                correlatedUser.join(
                        ORGANIZATIONS,
                        JoinType.INNER
                );

        List<Predicate> scopePredicates =
                new ArrayList<>();

        for (Organization currentOrganization :
                currentUserOrganizations) {

            if (currentOrganization == null) {
                continue;
            }

            LevelType levelType =
                    currentOrganization.getLevelType();

            if (levelType == null) {
                continue;
            }

            Predicate levelPredicate = switch (levelType) {
                case REPUBLICAN -> cb.conjunction();

                case REGIONAL -> buildRegionalScope(
                        targetOrganization,
                        currentOrganization,
                        cb
                );

                case URBAN -> buildUrbanScope(
                        targetOrganization,
                        currentOrganization,
                        cb
                );

                case DISTRICT, AREA, INTERDISTRICT ->
                        buildOrganizationScope(
                                targetOrganization,
                                currentOrganization,
                                cb
                        );

                default -> cb.disjunction();
            };

            scopePredicates.add(levelPredicate);
        }

        /*
         * Valid organization level topilmasa access berilmaydi.
         */
        if (scopePredicates.isEmpty()) {
            return cb.disjunction();
        }

        organizationExists
                .select(cb.literal(1))
                .where(
                        cb.or(
                                scopePredicates.toArray(
                                        Predicate[]::new
                                )
                        )
                );

        return cb.exists(organizationExists);
    }

    private static Predicate buildRegionalScope(
            Join<User, Organization> targetOrganization,
            Organization currentOrganization,
            CriteriaBuilder cb
    ) {
        String stateCode =
                currentOrganization.getStateCode();

        if (!hasText(stateCode)) {
            return cb.disjunction();
        }

        return cb.equal(
                cb.upper(
                        targetOrganization.<String>get(STATE_CODE)
                ),
                normalizeCode(stateCode)
        );
    }

    private static Predicate buildUrbanScope(
            Join<User, Organization> targetOrganization,
            Organization currentOrganization,
            CriteriaBuilder cb
    ) {
        String stateCode =
                currentOrganization.getStateCode();

        /*
         * Toshkent shahri uchun barcha tumanlar stateCode orqali.
         */
        if (TASHKENT_STATE_CODE.equalsIgnoreCase(stateCode)) {
            return cb.equal(
                    cb.upper(
                            targetOrganization.<String>get(STATE_CODE)
                    ),
                    TASHKENT_STATE_CODE
            );
        }

        String cityCode =
                currentOrganization.getCityCode();

        if (!hasText(cityCode)) {
            return cb.disjunction();
        }

        return cb.equal(
                cb.upper(
                        targetOrganization.<String>get(CITY_CODE)
                ),
                normalizeCode(cityCode)
        );
    }

    private static Predicate buildOrganizationScope(
            Join<User, Organization> targetOrganization,
            Organization currentOrganization,
            CriteriaBuilder cb
    ) {
        if (currentOrganization.getId() == null) {
            return cb.disjunction();
        }

        return cb.equal(
                targetOrganization.get(ID),
                currentOrganization.getId()
        );
    }

    private static void addExactPredicate(
            List<Predicate> predicates,
            Path<String> path,
            String value,
            CriteriaBuilder cb
    ) {
        if (!hasText(value)) {
            return;
        }

        predicates.add(
                cb.equal(
                        path,
                        value.trim()
                )
        );
    }

    private static void addContainsIgnoreCasePredicate(
            List<Predicate> predicates,
            Path<String> path,
            String value,
            CriteriaBuilder cb
    ) {
        if (!hasText(value)) {
            return;
        }

        String normalizedValue = value
                .trim()
                .toLowerCase(Locale.ROOT);

        predicates.add(
                cb.like(
                        cb.lower(path),
                        "%" + escapeLike(normalizedValue) + "%",
                        '\\'
                )
        );
    }

    private static String normalizeCode(String value) {
        return value
                .trim()
                .toUpperCase(Locale.ROOT);
    }

    private static String escapeLike(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}