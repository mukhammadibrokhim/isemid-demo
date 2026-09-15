package uz.uzinfocom.app.platform.devpanel.application.query.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.platform.devpanel.application.query.dto.DevLoginHistoryFilterRequest;
import uz.uzinfocom.app.platform.devpanel.domain.DevLoginHistory;

import java.util.ArrayList;
import java.util.List;

public final class DevLoginHistorySpecification {

    private DevLoginHistorySpecification() {
    }

    public static Specification<DevLoginHistory> byFilter(DevLoginHistoryFilterRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(request.username())) {
                predicates.add(cb.equal(root.get("username"), request.username()));
            }

            if (StringUtils.hasText(request.provider())) {
                predicates.add(cb.equal(root.get("provider"), request.provider()));
            }

            if (request.success() != null) {
                predicates.add(cb.equal(root.get("success"), request.success()));
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
