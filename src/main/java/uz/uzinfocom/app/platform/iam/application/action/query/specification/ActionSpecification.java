package uz.uzinfocom.app.platform.iam.application.action.query.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.platform.iam.application.action.query.dto.ActionFilterRequest;
import uz.uzinfocom.app.platform.iam.domain.Action;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ActionSpecification {

    public static Specification<Action> byFilter(ActionFilterRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("deleted"), false));

            if (StringUtils.hasText(request.code())) {
                predicates.add(cb.like(
                        cb.lower(cb.coalesce(root.get("code"), "")),
                        like(request.code())
                ));
            }

            if (request.active() != null) {
                predicates.add(cb.equal(root.get("active"), request.active()));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static String like(String value) {
        return "%" + value.trim().toLowerCase(Locale.ROOT) + "%";
    }
}
