package uz.uzinfocom.isemid.features.act.specifications;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import uz.uzinfocom.isemid.features.act.models.Act;

import java.util.Objects;
import java.util.stream.Stream;

public class ActSpecification {

    public static Specification<Act> build(Long cardId) {
        return (root, query, cb) -> cb.and(
                Stream.of(
                                cardId != null ? cb.equal(root.get("cardId"), cardId) : null
                        )
                        .filter(Objects::nonNull)
                        .toArray(Predicate[]::new)
        );
    }
}
