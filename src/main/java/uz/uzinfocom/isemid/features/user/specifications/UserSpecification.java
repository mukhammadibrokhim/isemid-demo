package uz.uzinfocom.isemid.features.user.specifications;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.organization.models.enums.LevelType;
import uz.uzinfocom.isemid.features.organization.models.enums.MedicalType;
import uz.uzinfocom.isemid.features.user.dtos.requests.UserFilterRequest;
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
    private static final String ORGANIZATION_STATE_CODE = "organizationStateCode";
    private static final String ORGANIZATION_CITY_CODE = "organizationCityCode";
    private static final String USER_ORG_MEDICAL_TYPE = "userOrgMedicalType";

    private static final String ORGANIZATION_ID = "id";
    private static final String ORGANIZATION_SCOPE_STATE_CODE = "stateCode";
    private static final String ORGANIZATION_SCOPE_CITY_CODE = "cityCode";

    private static final String TASHKENT_STATE_CODE = "UZ-TK";

    private UserSpecification() {
    }

    public static Specification<User> build(
            UserFilterRequest filter,
            User currentUser
    ) {
        Objects.requireNonNull(filter, "UserFilterRequest must not be null");

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            /*
             * Shaxsga tegishli oddiy filterlar.
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
             * Viloyat bo‘yicha filter:
             * User.organizationStateCode.
             */
            addCodePredicate(
                    predicates,
                    root.get(ORGANIZATION_STATE_CODE),
                    filter.getOrganizationStateCode(),
                    cb
            );

            /*
             * Tuman/shahar bo‘yicha filter:
             * User.organizationCityCode.
             */
            addCodePredicate(
                    predicates,
                    root.get(ORGANIZATION_CITY_CODE),
                    filter.getOrganizationCityCode(),
                    cb
            );

            /*
             * Muassasa turi bo‘yicha filter:
             * User.userOrgMedicalType IN (...).
             */
            addMedicalTypePredicate(
                    predicates,
                    root.get(USER_ORG_MEDICAL_TYPE),
                    filter.getUserOrgMedicalTypes()
            );

            /*
             * Aktiv/noaktiv.
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
             * Joriy userning ko‘rish huquqi.
             *
             * Bu request filter emas, security scope.
             */
            if (currentUser != null && !isSuperAdmin(currentUser)) {
                predicates.add(
                        buildAccessScopePredicate(
                                root,
                                query,
                                cb,
                                currentUser
                        )
                );
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static Predicate buildAccessScopePredicate(
            jakarta.persistence.criteria.Root<User> root,
            jakarta.persistence.criteria.CriteriaQuery<?> query,
            jakarta.persistence.criteria.CriteriaBuilder cb,
            User currentUser
    ) {
        List<Organization> currentUserOrganizations =
                currentUser.getOrganizations();

        /*
         * Muhim security qoidasi:
         * super admin bo‘lmagan userda organization bo‘lmasa,
         * barcha userlarni ko‘rsatib yubormaslik kerak.
         */
        if (currentUserOrganizations == null
                || currentUserOrganizations.isEmpty()) {
            return cb.disjunction();
        }

        Join<User, Organization> organizationJoin =
                root.join(ORGANIZATIONS, JoinType.INNER);

        /*
         * Userda bir nechta organization bo‘lsa, SQL duplicate row
         * qaytarishi mumkin. Pagination buzilmasligi uchun distinct.
         */
        query.distinct(true);

        List<Predicate> scopePredicates = new ArrayList<>();

        for (Organization currentOrganization : currentUserOrganizations) {
            if (currentOrganization == null) {
                continue;
            }

            LevelType levelType = currentOrganization.getLevelType();

            if (levelType == null) {
                continue;
            }

            Predicate scopePredicate = switch (levelType) {
                /*
                 * Respublika darajasidagi tashkilot barcha userlarni ko‘radi.
                 */
                case REPUBLICAN -> cb.conjunction();

                /*
                 * Viloyat tashkiloti o‘z viloyatidagi organization userlarini
                 * ko‘radi.
                 */
                case REGIONAL -> buildRegionalScope(
                        organizationJoin,
                        currentOrganization,
                        cb
                );

                /*
                 * Toshkent shahri alohida:
                 * UZ-TK bo‘lsa stateCode bo‘yicha,
                 * boshqa URBAN bo‘lsa cityCode bo‘yicha.
                 */
                case URBAN -> buildUrbanScope(
                        organizationJoin,
                        currentOrganization,
                        cb
                );

                /*
                 * Quyi darajalar faqat o‘z organization userlarini ko‘radi.
                 */
                case DISTRICT, AREA, INTERDISTRICT -> cb.equal(
                        organizationJoin.get(ORGANIZATION_ID),
                        currentOrganization.getId()
                );

                /*
                 * Noma’lum level access bermaydi.
                 */
                default -> cb.disjunction();
            };

            scopePredicates.add(scopePredicate);
        }

        if (scopePredicates.isEmpty()) {
            return cb.disjunction();
        }

        return cb.or(scopePredicates.toArray(Predicate[]::new));
    }

    private static Predicate buildRegionalScope(
            Join<User, Organization> organizationJoin,
            Organization currentOrganization,
            jakarta.persistence.criteria.CriteriaBuilder cb
    ) {
        if (!hasText(currentOrganization.getStateCode())) {
            return cb.disjunction();
        }

        return cb.equal(
                cb.upper(
                        organizationJoin.get(ORGANIZATION_SCOPE_STATE_CODE)
                ),
                normalizeCode(currentOrganization.getStateCode())
        );
    }

    private static Predicate buildUrbanScope(
            Join<User, Organization> organizationJoin,
            Organization currentOrganization,
            jakarta.persistence.criteria.CriteriaBuilder cb
    ) {
        String stateCode = currentOrganization.getStateCode();

        if (TASHKENT_STATE_CODE.equalsIgnoreCase(stateCode)) {
            return cb.equal(
                    cb.upper(
                            organizationJoin.get(ORGANIZATION_SCOPE_STATE_CODE)
                    ),
                    TASHKENT_STATE_CODE
            );
        }

        if (!hasText(currentOrganization.getCityCode())) {
            return cb.disjunction();
        }

        return cb.equal(
                cb.upper(
                        organizationJoin.get(ORGANIZATION_SCOPE_CITY_CODE)
                ),
                normalizeCode(currentOrganization.getCityCode())
        );
    }

    private static void addMedicalTypePredicate(
            List<Predicate> predicates,
            Path<MedicalType> path,
            Set<MedicalType> medicalTypes
    ) {
        if (medicalTypes == null || medicalTypes.isEmpty()) {
            return;
        }

        predicates.add(path.in(medicalTypes));
    }

    private static void addExactPredicate(
            List<Predicate> predicates,
            Path<String> path,
            String value,
            jakarta.persistence.criteria.CriteriaBuilder cb
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

    private static void addCodePredicate(
            List<Predicate> predicates,
            Path<String> path,
            String value,
            jakarta.persistence.criteria.CriteriaBuilder cb
    ) {
        if (!hasText(value)) {
            return;
        }

        predicates.add(
                cb.equal(
                        cb.upper(path),
                        normalizeCode(value)
                )
        );
    }

    private static void addContainsIgnoreCasePredicate(
            List<Predicate> predicates,
            Path<String> path,
            String value,
            jakarta.persistence.criteria.CriteriaBuilder cb
    ) {
        if (!hasText(value)) {
            return;
        }

        String normalizedValue = escapeLike(
                value.trim().toLowerCase(Locale.ROOT)
        );

        predicates.add(
                cb.like(
                        cb.lower(path),
                        "%" + normalizedValue + "%",
                        '\\'
                )
        );
    }

    private static String normalizeCode(String value) {
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private static String escapeLike(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}