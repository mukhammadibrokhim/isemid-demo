package uz.uzinfocom.app.platform.iam.application.user.query.specification;

import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.iam.domain.Role;
import uz.uzinfocom.app.platform.iam.domain.User;
import uz.uzinfocom.app.platform.iam.domain.enums.MedicalType;
import uz.uzinfocom.app.platform.iam.web.user.dto.request.UserFilterRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class UserSpecification {

    private UserSpecification() {
    }

    public static Specification<User> byFilter(UserFilterRequest filter) {
        Objects.requireNonNull(filter, "UserFilterRequest must not be null");

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            addLike(predicates, root.get("firstName"), filter.firstName(), cb);
            addLike(predicates, root.get("lastName"), filter.lastName(), cb);
            addLike(predicates, root.get("middleName"), filter.middleName(), cb);

            if (StringUtils.hasText(filter.nnuzb())) {
                predicates.add(
                        cb.equal(
                                root.<String>get("nnuzb"),
                                filter.nnuzb().trim()
                        )
                );
            }

            if (StringUtils.hasText(filter.phoneNumber())) {
                predicates.add(
                        cb.like(
                                root.get("phoneNumber"),
                                "%" + filter.phoneNumber().trim() + "%"
                        )
                );
            }

            if (filter.active() != null) {
                predicates.add(
                        cb.equal(
                                root.get("active"),
                                filter.active()
                        )
                );
            }

            addOrganizationFilter(
                    predicates,
                    root,
                    query,
                    cb,
                    filter
            );

            addRoleFilter(
                    predicates,
                    root,
                    query,
                    cb,
                    filter.roleIds()
            );

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static void addOrganizationFilter(
            List<Predicate> predicates,
            Root<User> root,
            CriteriaQuery<?> query,
            CriteriaBuilder cb,
            UserFilterRequest filter
    ) {
        boolean hasRegionCode =
                StringUtils.hasText(filter.organizationRegionCode());

        boolean hasDistrictCode =
                StringUtils.hasText(filter.organizationDistrictCode());

        List<MedicalType> medicalTypes =
                normalizeMedicalTypes(filter.organizationMedicalTypes());

        boolean hasMedicalTypes = !medicalTypes.isEmpty();

        if (!hasRegionCode && !hasDistrictCode && !hasMedicalTypes) {
            return;
        }

        Subquery<Integer> subquery = query.subquery(Integer.class);

        Root<User> correlatedUser = subquery.correlate(root);

        Join<User, Organization> organization =
                correlatedUser.join("organizations", JoinType.INNER);

        List<Predicate> organizationPredicates = new ArrayList<>();

        if (hasRegionCode) {
            organizationPredicates.add(
                    cb.equal(
                            cb.lower(
                                    organization.<String>get("regionCode")
                            ),
                            normalize(filter.organizationRegionCode())
                    )
            );
        }

        if (hasDistrictCode) {
            organizationPredicates.add(
                    cb.equal(
                            cb.lower(
                                    organization.<String>get("districtCode")
                            ),
                            normalize(filter.organizationDistrictCode())
                    )
            );
        }

        if (hasMedicalTypes) {
            organizationPredicates.add(
                    organization
                            .<MedicalType>get("medicalType")
                            .in(medicalTypes)
            );
        }

        subquery
                .select(cb.literal(1))
                .where(
                        cb.and(
                                organizationPredicates.toArray(Predicate[]::new)
                        )
                );

        predicates.add(cb.exists(subquery));
    }

    private static void addRoleFilter(
            List<Predicate> predicates,
            Root<User> root,
            CriteriaQuery<?> query,
            CriteriaBuilder cb,
            List<Long> roleIds
    ) {
        List<Long> normalizedRoleIds = normalizeRoleIds(roleIds);

        if (normalizedRoleIds.isEmpty()) {
            return;
        }

        Subquery<Integer> subquery = query.subquery(Integer.class);

        Root<User> correlatedUser = subquery.correlate(root);

        Join<User, Role> role =
                correlatedUser.join("roles", JoinType.INNER);

        subquery
                .select(cb.literal(1))
                .where(
                        role.<Long>get("id").in(normalizedRoleIds)
                );

        predicates.add(cb.exists(subquery));
    }

    private static void addLike(
            List<Predicate> predicates,
            Path<String> field,
            String value,
            CriteriaBuilder cb
    ) {
        if (!StringUtils.hasText(value)) {
            return;
        }

        predicates.add(
                cb.like(
                        cb.lower(field),
                        "%" + normalize(value) + "%"
                )
        );
    }

    private static List<Long> normalizeRoleIds(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return List.of();
        }

        return roleIds.stream()
                .filter(Objects::nonNull)
                .filter(id -> id > 0)
                .distinct()
                .toList();
    }

    private static List<MedicalType> normalizeMedicalTypes(
            List<MedicalType> medicalTypes
    ) {
        if (medicalTypes == null || medicalTypes.isEmpty()) {
            return List.of();
        }

        return medicalTypes.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private static String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }
}