package uz.uzinfocom.app.platform.devmonitoring.application.query.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.platform.devmonitoring.application.query.dto.DevRequestLogFilterRequest;
import uz.uzinfocom.app.platform.devmonitoring.domain.DevRequestLog;

import java.util.ArrayList;
import java.util.List;

public final class DevRequestLogSpecification {

    private DevRequestLogSpecification() {
    }

    public static Specification<DevRequestLog> byFilter(DevRequestLogFilterRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(request.principal())) {
                predicates.add(cb.equal(root.get("principal"), request.principal()));
            }

            if (StringUtils.hasText(request.method())) {
                predicates.add(cb.equal(root.get("method"), request.method()));
            }

            if (StringUtils.hasText(request.path())) {
                predicates.add(cb.like(root.get("path"), "%" + request.path() + "%"));
            }

            if (request.status() != null) {
                predicates.add(cb.equal(root.get("httpStatus"), request.status()));
            }

            if (StringUtils.hasText(request.outcome())) {
                predicates.add(cb.equal(root.get("outcome"), request.outcome()));
            }

            if (StringUtils.hasText(request.organizationId())) {
                predicates.add(cb.equal(root.get("organizationId"), request.organizationId()));
            }

            if (StringUtils.hasText(request.traceId())) {
                predicates.add(cb.equal(root.get("traceId"), request.traceId()));
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
