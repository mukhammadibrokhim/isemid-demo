package uz.uzinfocom.app.platform.iam.application.user.query.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.platform.iam.domain.User;
import uz.uzinfocom.app.platform.iam.web.user.dto.request.UserFilterRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class UserSpecification {

    private UserSpecification() {
    }

    public static Specification<User> byFilter(UserFilterRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(request.firstName())) {
                predicates.add(cb.like(
                        cb.lower(cb.coalesce(root.get("firstName"), "")),
                        like(request.firstName())
                ));
            }

            if (StringUtils.hasText(request.lastName())) {
                predicates.add(cb.like(
                        cb.lower(cb.coalesce(root.get("lastName"), "")),
                        like(request.lastName())
                ));
            }

            if (StringUtils.hasText(request.middleName())) {
                predicates.add(cb.like(
                        cb.lower(cb.coalesce(root.get("middleName"), "")),
                        like(request.middleName())
                ));
            }

            if (StringUtils.hasText(request.nnuzb())) {
                predicates.add(cb.like(
                        cb.lower(cb.coalesce(root.get("nnuzb"), "")),
                        like(request.nnuzb())
                ));
            }

            if (StringUtils.hasText(request.phoneNumber())) {
                predicates.add(cb.like(
                        cb.lower(cb.coalesce(root.get("phoneNumber"), "")),
                        like(request.phoneNumber())
                ));
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