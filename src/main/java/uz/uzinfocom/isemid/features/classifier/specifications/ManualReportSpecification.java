package uz.uzinfocom.isemid.features.classifier.specifications;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import uz.uzinfocom.isemid.features.classifier.models.ManualReport;

import java.util.Objects;
import java.util.stream.Stream;

import static org.springframework.util.StringUtils.hasText;

public class ManualReportSpecification {

    public static Specification<ManualReport> build(
            String shortName,
            String nameUz,
            String nameUzCyril,
            String nameRu,
            String nameKaa,
            String code,
            Boolean includeTotal
    ) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.and(
                Stream.of(
                                hasText(shortName) ? criteriaBuilder.like(criteriaBuilder.lower(root.get("shortName")), "%" + shortName.toLowerCase() + "%") : null,
                                hasText(nameUz) ? criteriaBuilder.like(criteriaBuilder.lower(root.get("nameUz")), "%" + nameUz.toLowerCase() + "%") : null,
                                hasText(nameUzCyril) ? criteriaBuilder.like(criteriaBuilder.lower(root.get("nameUzCyril")), "%" + nameUzCyril.toLowerCase() + "%") : null,
                                hasText(nameRu) ? criteriaBuilder.like(criteriaBuilder.lower(root.get("nameRu")), "%" + nameRu.toLowerCase() + "%") : null,
                                hasText(nameKaa) ? criteriaBuilder.like(criteriaBuilder.lower(root.get("nameKaa")), "%" + nameKaa.toLowerCase() + "%") : null,
                                hasText(code) ? criteriaBuilder.like(criteriaBuilder.lower(root.get("code")), "%" + code.toLowerCase() + "%") : null,
                                includeTotal != null ? criteriaBuilder.equal(root.get("includeTotal"), includeTotal) : null
                        )
                        .filter(Objects::nonNull)
                        .toArray(Predicate[]::new)
        );
    }
}
