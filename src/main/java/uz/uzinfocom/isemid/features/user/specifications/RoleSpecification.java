package uz.uzinfocom.isemid.features.user.specifications;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import uz.uzinfocom.isemid.features.user.models.Role;

import java.util.Objects;
import java.util.stream.Stream;

import static org.springframework.util.StringUtils.hasText;

public class RoleSpecification {
    public static Specification<Role> getFilteredSpecification(
            final String name,
            final String descriptionUz,
            final String descriptionUzCyril,
            final String descriptionRu,
            final String descriptionKaa
    ) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.and(
                Stream.of(
                                hasText(name) ? criteriaBuilder.like(root.get("name"), "%" + name + "%") : null,
                                hasText(descriptionUz) ? criteriaBuilder.like(root.get("description"), "%" + descriptionUz + "%") : null,
                                hasText(descriptionUzCyril) ? criteriaBuilder.like(root.get("description"), "%" + descriptionUzCyril + "%") : null,
                                hasText(descriptionRu) ? criteriaBuilder.like(root.get("description"), "%" + descriptionRu + "%") : null,
                                hasText(descriptionKaa) ? criteriaBuilder.like(root.get("description"), "%" + descriptionKaa + "%") : null
                        )
                        .filter(Objects::nonNull)
                        .toArray(Predicate[]::new)
        );
    }
}
