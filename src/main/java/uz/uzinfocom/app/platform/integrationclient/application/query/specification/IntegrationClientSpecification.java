package uz.uzinfocom.app.platform.integrationclient.application.query.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.platform.integrationclient.application.query.dto.IntegrationClientFilterRequest;
import uz.uzinfocom.app.platform.integrationclient.domain.IntegrationClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class IntegrationClientSpecification {

    public static Specification<IntegrationClient> byFilter(IntegrationClientFilterRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(request.name())) {
                predicates.add(cb.like(
                        cb.lower(cb.coalesce(root.get("name"), "")),
                        like(request.name())
                ));
            }

            if (StringUtils.hasText(request.clientId())) {
                predicates.add(cb.like(
                        cb.lower(cb.coalesce(root.get("clientId"), "")),
                        like(request.clientId())
                ));
            }

            if (request.organizationId() != null) {
                predicates.add(cb.equal(root.get("organizationId"), request.organizationId()));
            }

            if (request.authType() != null) {
                predicates.add(cb.equal(root.get("authType"), request.authType()));
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
