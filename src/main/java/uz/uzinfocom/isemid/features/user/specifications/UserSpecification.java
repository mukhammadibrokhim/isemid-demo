package uz.uzinfocom.isemid.features.user.specifications;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import uz.uzinfocom.isemid.features.user.models.User;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.util.StringUtils.hasText;
import static uz.uzinfocom.isemid.commons.configs.security.util.SecurityUtils.isSuperAdmin;
import static uz.uzinfocom.isemid.commons.utils.SpecificationUtils.buildLevelPredicate;

public class UserSpecification {
    public static Specification<User> build(
            final String nnuzb,
            final String firstName,
            final String lastName,
            final String middleName,
            final String phoneNumber,
            final String accessLevelCode,
            final Boolean active,
            User currentUser
    ) {
        return (root, query, criteriaBuilder) -> {
            var predicates = Stream.of(
                    hasText(nnuzb) ? criteriaBuilder.equal(root.get("nnuzb"), nnuzb) : null,
                    hasText(firstName) ? criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), "%" + firstName.toLowerCase() + "%") : null,
                    hasText(lastName) ? criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), "%" + lastName.toLowerCase() + "%") : null,
                    hasText(middleName) ? criteriaBuilder.like(criteriaBuilder.lower(root.get("middleName")), "%" + middleName.toLowerCase() + "%") : null,
                    hasText(phoneNumber) ? criteriaBuilder.equal(root.get("phoneNumber"), phoneNumber) : null,
                    hasText(accessLevelCode) ? criteriaBuilder.equal(root.join("accessLevel", JoinType.LEFT).get("code"), accessLevelCode) : null,
                    active != null ? criteriaBuilder.equal(root.get("active"), active) : null
            ).filter(Objects::nonNull).collect(Collectors.toList());

            if (currentUser != null && currentUser.getCurrentOrganization() != null) {
                var currentOrg = currentUser.getCurrentOrganization();
                var level = currentOrg.getLevelType();
                if (isSuperAdmin(currentUser)) {
                    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                }
                predicates.add(buildLevelPredicate(root, criteriaBuilder, level, currentOrg.getStateCode(), currentOrg.getCityCode(), currentOrg.getId()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}