package uz.uzinfocom.isemid.features.organization.specifications;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import uz.uzinfocom.isemid.features.organization.models.Organization;

import java.util.Objects;
import java.util.stream.Stream;

import static org.springframework.util.StringUtils.hasText;

public class OrganizationSpecification {
    public static Specification<Organization> getFilteredSpecification(
            String name,
            String stateCode,
            String cityCode
    ) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.and(
                Stream.of(
                                criteriaBuilder.isNull(root.get("parent")),
                                hasText(name) ? criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase() + "%") : null,
                                hasText(stateCode) ? criteriaBuilder.equal(root.get("stateCode"), stateCode) : null,
                                hasText(cityCode) ? criteriaBuilder.equal(root.get("cityCode"), cityCode) : null
                        )
                        .filter(Objects::nonNull)
                        .toArray(Predicate[]::new)
        );
    }
}
