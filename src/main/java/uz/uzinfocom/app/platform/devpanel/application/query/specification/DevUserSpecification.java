package uz.uzinfocom.app.platform.devpanel.application.query.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.platform.devpanel.application.query.dto.DevUserFilterRequest;
import uz.uzinfocom.app.platform.devpanel.domain.DevUser;

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

            if (request.role() != null) {
                predicates.add(cb.equal(root.get("role"), request.role()));
            }

            if (request.positionId() != null) {
                predicates.add(cb.equal(root.get("position").get("id"), request.positionId()));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
