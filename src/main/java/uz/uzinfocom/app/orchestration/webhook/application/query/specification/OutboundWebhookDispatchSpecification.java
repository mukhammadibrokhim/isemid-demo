package uz.uzinfocom.app.orchestration.webhook.application.query.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import uz.uzinfocom.app.orchestration.webhook.application.query.dto.OutboundWebhookDispatchFilterRequest;
import uz.uzinfocom.app.orchestration.webhook.domain.OutboundWebhookDispatch;

import java.util.ArrayList;
import java.util.List;

public class OutboundWebhookDispatchSpecification {

    public static Specification<OutboundWebhookDispatch> byFilter(OutboundWebhookDispatchFilterRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (request.integrationClientId() != null) {
                predicates.add(cb.equal(root.get("integrationClientId"), request.integrationClientId()));
            }

            if (request.entityType() != null) {
                predicates.add(cb.equal(root.get("entityType"), request.entityType()));
            }

            if (request.status() != null) {
                predicates.add(cb.equal(root.get("status"), request.status()));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
