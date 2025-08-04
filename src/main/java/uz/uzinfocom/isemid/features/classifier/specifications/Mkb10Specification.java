package uz.uzinfocom.isemid.features.classifier.specifications;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import uz.uzinfocom.isemid.features.classifier.models.Mkb10;

import java.util.Objects;
import java.util.stream.Stream;

import static org.springframework.util.StringUtils.hasText;

public class Mkb10Specification {
    public static Specification<Mkb10> build(
            String mkbCode,
            Long parentId,
            String nameUz,
            String nameUzCyril,
            String nameRu,
            String nameEn
    ) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.and(
                Stream.of(
                                parentId == null ? criteriaBuilder.isNull(root.get("parentId"))
                                        : criteriaBuilder.equal(root.get("parentId"), parentId),
                                hasText(mkbCode) ? criteriaBuilder.equal(root.get("code"), mkbCode) : null,
                                hasText(nameUz) ? criteriaBuilder.like(criteriaBuilder.lower(root.get("nameUz")), "%" + nameUz.toLowerCase() + "%") : null,
                                hasText(nameUzCyril) ? criteriaBuilder.like(criteriaBuilder.lower(root.get("nameUzCyril")), "%" + nameUzCyril.toLowerCase() + "%") : null,
                                hasText(nameRu) ? criteriaBuilder.like(criteriaBuilder.lower(root.get("nameRu")), "%" + nameRu.toLowerCase() + "%") : null,
                                hasText(nameEn) ? criteriaBuilder.like(criteriaBuilder.lower(root.get("nameEn")), "%" + nameEn.toLowerCase() + "%") : null
                        )
                        .filter(Objects::nonNull)
                        .toArray(Predicate[]::new)
        );
    }
}
