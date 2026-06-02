package uz.uzinfocom.app.platform.iam.application.role.query.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.platform.iam.application.role.query.dto.RoleFilterRequest;
import uz.uzinfocom.app.platform.iam.domain.Role;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class RoleSpecification {

    private RoleSpecification() {
    }

    public static Specification<Role> byFilter(RoleFilterRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("deleted"), false));

            if (StringUtils.hasText(request.name())) {
                predicates.add(likeIgnoreCase(cb, root.get("name"), request.name()));
            }

            if (StringUtils.hasText(request.descriptionUz())) {
                predicates.add(likeIgnoreCase(cb, root.get("descriptionUz"), request.descriptionUz()));
            }

            if (StringUtils.hasText(request.descriptionUzCyril())) {
                predicates.add(likeIgnoreCase(cb, root.get("descriptionUzCyril"), request.descriptionUzCyril()));
            }

            if (StringUtils.hasText(request.descriptionRu())) {
                predicates.add(likeIgnoreCase(cb, root.get("descriptionRu"), request.descriptionRu()));
            }

            if (StringUtils.hasText(request.descriptionKaa())) {
                predicates.add(likeIgnoreCase(cb, root.get("descriptionKaa"), request.descriptionKaa()));
            }

            if (request.active() != null) {
                predicates.add(cb.equal(root.get("active"), request.active()));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static Predicate likeIgnoreCase(
            CriteriaBuilder cb,
            Expression<String> field,
            String value
    ) {
        String pattern = "%" + value.trim().toLowerCase(Locale.ROOT) + "%";
        return cb.like(cb.lower(field), pattern);
    }
}
