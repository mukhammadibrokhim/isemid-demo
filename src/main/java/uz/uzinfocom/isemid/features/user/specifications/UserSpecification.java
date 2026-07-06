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

    private static final String ACCESS_LEVEL = "accessLevel";
    private static final String ACCESS_LEVEL_CODE = "code";

    private static final String ORGANIZATIONS = "organizations";

    /*
     * User entity ichidagi denormalized organization fieldlar.
     */
    private static final String ORGANIZATION_STATE_CODE =
            "organizationStateCode";

    private static final String ORGANIZATION_CITY_CODE =
            "organizationCityCode";

    private static final String USER_ORG_MEDICAL_TYPE =
            "userOrgMedicalType";

    /*
     * Organization entity fieldlari.
     */
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
             * Quyidagi barcha request filterlar faqat field qiymati
             * mavjud bo‘lgandagina queryga qo‘shiladi.
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
             * Viloyat bo‘yicha filter.
             * Null yoki blank bo‘lsa ishlamaydi.
             */
            addCodePredicate(
                    predicates,
                    root.get(ORGANIZATION_STATE_CODE),
                    filter.getOrganizationStateCode(),
                    cb
            );

            /*
             * Tuman/shahar bo‘yicha filter.
             * Null yoki blank bo‘lsa ishlamaydi.
             */
            addCodePredicate(
                    predicates,
                    root.get(ORGANIZATION_CITY_CODE),
                    filter.getOrganizationCityCode(),
                    cb
            );

            /*
             * Muassasa turi bo‘yicha filter.
             * Null yoki empty bo‘lsa ishlamaydi.
             */
            addMedicalTypePredicate(
                    predicates,
                    root.get(USER_ORG_MEDICAL_TYPE),
                    filter.getUserOrgMedicalTypes()
            );

            /*
             * Aktiv/noaktiv filter.
             *
             * active == true  -> faqat aktiv
             * active == false -> faqat noaktiv
             * active == null  -> filter qo‘shilmaydi
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
             * LevelType bo‘yicha access scope request filter emas.
             *
             * Non-super-admin user uchun bu har doim ishlaydi.
             * Super admin uchun scope cheklovi qo‘shilmaydi.
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
     * Joriy user organization leveliga qarab qaysi userlarni
     * ko‘rishi mumkinligini aniqlaydi.
     *
     * REPUBLICAN:
     * - barcha userlar
     *
     * REGIONAL:
     * - o‘z stateCode hududidagi organization userlari
     *
     * URBAN:
     * - Toshkent shahri bo‘lsa stateCode = UZ-TK
     * - boshqa holatda cityCode bo‘yicha
     *
     * DISTRICT, AREA, INTERDISTRICT:
     * - faqat aynan o‘z organization userlari
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
         * organization mavjud bo‘lmasa, hech qanday user ko‘rsatilmaydi.
         */
        if (currentUserOrganizations == null
                || currentUserOrganizations.isEmpty()) {
            return cb.disjunction();
        }

        /*
         * Current user birorta REPUBLICAN organizationga tegishli bo‘lsa,
         * barcha userlarni ko‘rishi mumkin.
         */
        boolean hasRepublicanScope = currentUserOrganizations.stream()
                .filter(Objects::nonNull)
                .map(Organization::getLevelType)
                .filter(Objects::nonNull)
                .anyMatch(LevelType.REPUBLICAN::equals);

        if (hasRepublicanScope) {
            return cb.conjunction();
        }

        /*
         * EXISTS ishlatilmoqda:
         *
         * - asosiy queryda collection JOIN hosil qilmaydi;
         * - duplicate User qaytarmaydi;
         * - query.distinct(true) talab qilmaydi;
         * - pagination stabil ishlaydi.
         */
        Subquery<Integer> organizationExists =
                query.subquery(Integer.class);

        Root<User> correlatedUser =
                organizationExists.correlate(root);

        Join<User, Organization> targetOrganization =
                correlatedUser.join(
                        ORGANIZATIONS,
                        JoinType.INNER
                );

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

            scopePredicates.add(scopePredicate);
        }

        /*
         * Birorta valid level scope topilmasa, hech narsa ko‘rsatilmaydi.
         */
        if (scopePredicates.isEmpty()) {
            return cb.disjunction();
        }

        organizationExists
                .select(cb.literal(1))
                .where(
                        cb.or(
                                scopePredicates.toArray(Predicate[]::new)
                        )
                );

        return cb.exists(organizationExists);
    }

    private static Predicate buildRegionalScope(
            Join<User, Organization> targetOrganization,
            Organization currentOrganization,
            CriteriaBuilder cb
    ) {
        String stateCode = currentOrganization.getStateCode();

        if (!hasText(stateCode)) {
            return cb.disjunction();
        }

        return cb.equal(
                cb.upper(
                        targetOrganization.<String>get(
                                ORGANIZATION_SCOPE_STATE_CODE
                        )
                ),
                normalizeCode(stateCode)
        );
    }

    private static Predicate buildUrbanScope(
            Join<User, Organization> targetOrganization,
            Organization currentOrganization,
            CriteriaBuilder cb
    ) {
        String stateCode = currentOrganization.getStateCode();

        /*
         * Toshkent shahri uchun barcha tumanlar stateCode orqali olinadi.
         */
        if (TASHKENT_STATE_CODE.equalsIgnoreCase(stateCode)) {
            return cb.equal(
                    cb.upper(
                            targetOrganization.<String>get(
                                    ORGANIZATION_SCOPE_STATE_CODE
                            )
                    ),
                    TASHKENT_STATE_CODE
            );
        }

        String cityCode = currentOrganization.getCityCode();

        if (!hasText(cityCode)) {
            return cb.disjunction();
        }

        return cb.equal(
                cb.upper(
                        targetOrganization.<String>get(
                                ORGANIZATION_SCOPE_CITY_CODE
                        )
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
                targetOrganization.get(ORGANIZATION_ID),
                currentOrganization.getId()
        );
    }

    private static void addAccessLevelPredicate(
            List<Predicate> predicates,
            Root<User> root,
            String accessLevelCode,
            CriteriaBuilder cb
    ) {
        if (!hasText(accessLevelCode)) {
            return;
        }

        Join<Object, Object> accessLevelJoin =
                root.join(
                        ACCESS_LEVEL,
                        JoinType.INNER
                );

        predicates.add(
                cb.equal(
                        cb.upper(
                                accessLevelJoin.<String>get(
                                        ACCESS_LEVEL_CODE
                                )
                        ),
                        normalizeCode(accessLevelCode)
                )
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

        predicates.add(
                path.in(medicalTypes)
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

    private static void addCodePredicate(
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
                        cb.upper(path),
                        normalizeCode(value)
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