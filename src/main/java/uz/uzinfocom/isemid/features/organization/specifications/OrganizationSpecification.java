package uz.uzinfocom.isemid.features.organization.specifications;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.user.models.User;

import java.util.Objects;
import java.util.stream.Stream;

import static org.springframework.util.StringUtils.hasText;
import static uz.uzinfocom.isemid.commons.configs.security.util.SecurityUtils.isSuperAdmin;
import static uz.uzinfocom.isemid.commons.utils.SpecificationUtils.buildLevelPredicate;

public class OrganizationSpecification {
    public static Specification<Organization> build(
            String name,
            String stateCode,
            String cityCode,
            User currentUser
    ) {
        return (root, query, criteriaBuilder) -> {
            var predicates = Stream.of(
                    criteriaBuilder.isNull(root.get("parent")),
                    hasText(name) ? criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase() + "%") : null,
                    hasText(stateCode) ? criteriaBuilder.equal(root.get("stateCode"), stateCode) : null,
                    hasText(cityCode) ? criteriaBuilder.equal(root.get("cityCode"), cityCode) : null
            ).filter(Objects::nonNull).collect(java.util.stream.Collectors.toList());

            if (currentUser != null && currentUser.getCurrentOrganization() != null) {
                Organization currOrgan = currentUser.getCurrentOrganization();
                if (isSuperAdmin(currentUser)) {
                    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                }
                predicates.add(buildLevelPredicate(root, criteriaBuilder, currOrgan.getLevelType(), currOrgan.getStateCode(), currOrgan.getCityCode(), currOrgan.getId()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
