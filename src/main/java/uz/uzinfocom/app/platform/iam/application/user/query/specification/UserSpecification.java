package uz.uzinfocom.app.platform.iam.application.user.query.specification;

import jakarta.persistence.criteria.Path;
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

    public static Specification<User> byFilter(UserFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            addLike(predicates, root.get("firstName"), filter.firstName(), cb);
            addLike(predicates, root.get("lastName"), filter.lastName(), cb);
            addLike(predicates, root.get("middleName"), filter.middleName(), cb);

            if (StringUtils.hasText(filter.nnuzb())) {
                predicates.add(
                        cb.equal(
                                root.<String>get("nnuzb"),
                                filter.nnuzb().trim()
                        )
                );
            }

            if (StringUtils.hasText(filter.phoneNumber())) {
                predicates.add(
                        cb.like(
                                root.get("phoneNumber"),
                                "%" + filter.phoneNumber().trim() + "%"
                        )
                );
            }

            if (filter.active() != null) {
                predicates.add(
                        cb.equal(root.get("active"), filter.active())
                );
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static void addLike(
            List<Predicate> predicates,
            Path<String> field,
            String value,
            jakarta.persistence.criteria.CriteriaBuilder cb
    ) {
        if (!StringUtils.hasText(value)) {
            return;
        }

        predicates.add(
                cb.like(
                        cb.lower(field),
                        "%" + value.trim().toLowerCase(Locale.ROOT) + "%"
                )
        );
    }
}