package uz.uzinfocom.app.modules.card.infrastructure.persistence.specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import uz.uzinfocom.app.modules.card.application.query.CardFilterRequest;
import uz.uzinfocom.app.modules.card.domain.model.Card;
import uz.uzinfocom.app.platform.iam.domain.User;

import java.util.ArrayList;
import java.util.List;

public final class CardSpecification {

    private CardSpecification() {
    }

    public static Specification<Card> byFilter(CardFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("deleteInfo").get("deleted")));

            if (filter == null) {
                return cb.and(predicates.toArray(Predicate[]::new));
            }

            if (filter.formId() != null) {
                predicates.add(cb.equal(root.get("form058").get("id"), filter.formId()));
            }

            if (filter.cardType() != null) {
                predicates.add(cb.equal(root.get("cardType"), filter.cardType()));
            }

            if (filter.status() != null) {
                predicates.add(cb.equal(root.get("status"), filter.status()));
            }

            if (filter.assignedToUserId() != null) {
                Join<Card, User> users = root.join("users", JoinType.INNER);
                predicates.add(cb.equal(users.get("id"), filter.assignedToUserId()));
            }

            if (filter.assignedById() != null) {
                predicates.add(cb.equal(root.get("assignedById"), filter.assignedById()));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
