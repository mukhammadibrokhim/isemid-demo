package uz.uzinfocom.app.platform.devmonitoring.application.query.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.platform.devmonitoring.application.query.dto.DevErrorFilterRequest;
import uz.uzinfocom.app.platform.devmonitoring.domain.DevErrorLog;

import java.util.ArrayList;
import java.util.List;

public final class DevErrorLogSpecification {

    private DevErrorLogSpecification() {
    }

    public static Specification<DevErrorLog> byFilter(DevErrorFilterRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (request.status() != null) {
                predicates.add(cb.equal(root.get("status"), request.status()));
            }

            if (StringUtils.hasText(request.errorCode())) {
                predicates.add(cb.equal(root.get("errorCode"), request.errorCode()));
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
