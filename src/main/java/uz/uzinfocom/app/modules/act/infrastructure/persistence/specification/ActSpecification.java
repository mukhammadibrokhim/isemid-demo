package uz.uzinfocom.app.modules.act.infrastructure.persistence.specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import uz.uzinfocom.app.modules.act.application.query.ActFilterRequest;
import uz.uzinfocom.app.modules.act.domain.model.Act;
import uz.uzinfocom.app.platform.iam.domain.User;

import java.util.ArrayList;
import java.util.List;

public final class ActSpecification {

    private ActSpecification() {
    }

    public static Specification<Act> byFilter(ActFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("deleteInfo").get("deleted")));

            if (filter == null) {
                return cb.and(predicates.toArray(Predicate[]::new));
            }

            if (filter.cardId() != null) {
                predicates.add(cb.equal(root.get("card").get("id"), filter.cardId()));
            }

            if (filter.status() != null) {
                predicates.add(cb.equal(root.get("actStatus"), filter.status()));
            }

            if (filter.assignedToUserId() != null) {
                Join<Act, User> users = root.join("users", JoinType.INNER);
                predicates.add(cb.equal(users.get("id"), filter.assignedToUserId()));
            }

            if (filter.assignedById() != null) {
                predicates.add(cb.equal(root.get("assignedById"), filter.assignedById()));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
