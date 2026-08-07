package uz.uzinfocom.app.platform.devmonitoring.application.query.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.platform.devmonitoring.application.query.dto.DevUserFilterRequest;
import uz.uzinfocom.app.platform.devmonitoring.domain.DevUser;

import java.util.ArrayList;
import java.util.List;

public final class DevUserSpecification {

    private DevUserSpecification() {
    }

    public static Specification<DevUser> byFilter(DevUserFilterRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(request.username())) {
                predicates.add(cb.like(cb.lower(root.get("username")), "%" + request.username().toLowerCase().trim() + "%"));
            }

            if (request.enabled() != null) {
                predicates.add(cb.equal(root.get("enabled"), request.enabled()));
            }

            if (request.root() != null) {
                predicates.add(cb.equal(root.get("root"), request.root()));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
