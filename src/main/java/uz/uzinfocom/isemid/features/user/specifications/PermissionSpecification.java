package uz.uzinfocom.isemid.features.user.specifications;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import uz.uzinfocom.isemid.features.user.models.Permission;

import java.util.Objects;
import java.util.stream.Stream;

import static org.springframework.util.StringUtils.hasText;

public class PermissionSpecification {

    public static Specification<Permission> getFilteredSpecification(
            String subject,
            String descriptionUz,
            String descriptionRu,
            String descriptionUzCyril,
            String descriptionKaa,
            Boolean active
    ) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.and(
                Stream.of(
                                hasText(subject) ? criteriaBuilder.like(root.get("subject"), "%" + subject + "%") : null,
                                hasText(descriptionUz) ? criteriaBuilder.like(root.get("descriptionUz"), "%" + descriptionUz + "%") : null,
                                hasText(descriptionRu) ? criteriaBuilder.like(root.get("descriptionRu"), "%" + descriptionRu + "%") : null,
                                hasText(descriptionUzCyril) ? criteriaBuilder.like(root.get("descriptionUzCyril"), "%" + descriptionUzCyril + "%") : null,
                                hasText(descriptionKaa) ? criteriaBuilder.like(root.get("descriptionKaa"), "%" + descriptionKaa + "%") : null,
                                active != null ? criteriaBuilder.equal(root.get("active"), active) : null
                        )
                        .filter(Objects::nonNull)
                        .toArray(Predicate[]::new)
        );
    }
}
