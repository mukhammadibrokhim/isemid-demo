package uz.uzinfocom.app.platform.devpanel.application.query.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.platform.devpanel.application.query.dto.DevPositionFilterRequest;
import uz.uzinfocom.app.platform.devpanel.domain.DevPosition;

import java.util.ArrayList;
import java.util.List;

public final class DevPositionSpecification {

    private DevPositionSpecification() {
    }

    public static Specification<DevPosition> byFilter(DevPositionFilterRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(request.name())) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + request.name().toLowerCase().trim() + "%"));
            }

            if (request.enabled() != null) {
                predicates.add(cb.equal(root.get("enabled"), request.enabled()));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
