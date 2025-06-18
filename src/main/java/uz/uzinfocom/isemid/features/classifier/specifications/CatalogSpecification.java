package uz.uzinfocom.isemid.features.classifier.specifications;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import uz.uzinfocom.isemid.features.classifier.models.Catalog;

import java.util.Objects;
import java.util.stream.Stream;

import static org.springframework.util.StringUtils.hasText;

public class CatalogSpecification {
    public static Specification<Catalog> getFilteredSpecification(
            String type,
            String code,
            String parentCode,
            String nameRu,
            String nameUz,
            String nameUzCyril,
            String nameKaa
    ) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.and(
                Stream.of(
                                hasText(type) ? criteriaBuilder.like(criteriaBuilder.lower(root.get("type")), "%" + type.toLowerCase() + "%") : null,
                                hasText(code) ? criteriaBuilder.like(criteriaBuilder.lower(root.get("code")), "%" + code.toLowerCase() + "%") : null,
                                hasText(parentCode) ? criteriaBuilder.like(criteriaBuilder.lower(root.get("parentCode")), "%" + parentCode.toLowerCase() + "%") : null,
                                hasText(nameRu) ? criteriaBuilder.like(criteriaBuilder.lower(root.get("nameRu")), "%" + nameRu.toLowerCase() + "%") : null,
                                hasText(nameUz) ? criteriaBuilder.like(criteriaBuilder.lower(root.get("nameUz")), "%" + nameUz.toLowerCase() + "%") : null,
                                hasText(nameUzCyril) ? criteriaBuilder.like(criteriaBuilder.lower(root.get("nameUzCyril")), "%" + nameUzCyril.toLowerCase() + "%") : null,
                                hasText(nameKaa) ? criteriaBuilder.like(criteriaBuilder.lower(root.get("nameKaa")), "%" + nameKaa.toLowerCase() + "%") : null
                        )
                        .filter(Objects::nonNull)
                        .toArray(Predicate[]::new)
        );
    }
}
