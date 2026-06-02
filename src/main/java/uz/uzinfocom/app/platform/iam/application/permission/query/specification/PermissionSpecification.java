package uz.uzinfocom.app.platform.iam.application.permission.query.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.platform.iam.application.permission.query.dto.PermissionFilterRequest;
import uz.uzinfocom.app.platform.iam.domain.Permission;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PermissionSpecification {

    public static Specification<Permission> byFilter(PermissionFilterRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("deleted"), false));

            if (StringUtils.hasText(request.subject())) {
                predicates.add(cb.like(
                        cb.lower(cb.coalesce(root.get("subject"), "")),
                        like(request.subject())
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
