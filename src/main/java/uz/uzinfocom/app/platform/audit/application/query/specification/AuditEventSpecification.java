package uz.uzinfocom.app.platform.audit.application.query.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import uz.uzinfocom.app.platform.audit.application.query.dto.AuditEventFilterRequest;
import uz.uzinfocom.app.platform.audit.domain.AuditEvent;

import java.util.ArrayList;
import java.util.List;

public final class AuditEventSpecification {

    private AuditEventSpecification() {
    }

    public static Specification<AuditEvent> byFilter(AuditEventFilterRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (request.entityType() != null) {
                predicates.add(cb.equal(root.get("entityType"), request.entityType()));
            }

            if (request.entityId() != null) {
                predicates.add(cb.equal(root.get("entityId"), request.entityId()));
            }

            if (request.eventType() != null) {
                predicates.add(cb.equal(root.get("eventType"), request.eventType()));
            }

            if (request.organizationId() != null) {
                predicates.add(cb.equal(root.get("newOrgId"), request.organizationId()));
            }

            if (request.from() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("occurredAt"), request.from()));
            }

            if (request.to() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("occurredAt"), request.to()));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
