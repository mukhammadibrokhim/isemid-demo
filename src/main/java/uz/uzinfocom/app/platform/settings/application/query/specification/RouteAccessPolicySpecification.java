package uz.uzinfocom.app.platform.settings.application.query.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.platform.settings.application.query.dto.RouteAccessPolicyFilterRequest;
import uz.uzinfocom.app.platform.settings.domain.RouteAccessPolicy;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class RouteAccessPolicySpecification {

    private RouteAccessPolicySpecification() {
    }

    public static Specification<RouteAccessPolicy> byFilter(RouteAccessPolicyFilterRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(request.search())) {
                String search = "%" + request.search().trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.like(cb.lower(root.get("pattern")), search));
            }

            if (request.enabled() != null) {
                predicates.add(cb.equal(root.get("enabled"), request.enabled()));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
