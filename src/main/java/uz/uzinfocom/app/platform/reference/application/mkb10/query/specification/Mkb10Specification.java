package uz.uzinfocom.app.platform.reference.application.mkb10.query.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.platform.reference.application.mkb10.query.dto.Mkb10FilterRequest;
import uz.uzinfocom.app.platform.reference.domain.Mkb10;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class Mkb10Specification {

    private Mkb10Specification() {
    }

    public static Specification<Mkb10> byFilter(Mkb10FilterRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("deleted"), false));

            if (request == null) {
                return cb.and(predicates.toArray(Predicate[]::new));
            }

            if (StringUtils.hasText(request.code())) {
                predicates.add(cb.equal(
                        cb.upper(root.get("code")),
                        request.code().trim().toUpperCase(Locale.ROOT)
                ));
            }

            if (request.parentId() != null) {
                predicates.add(cb.equal(root.get("parentId"), request.parentId()));
            }

            if (request.level() != null) {
                predicates.add(cb.equal(root.get("level"), request.level()));
            }

            if (StringUtils.hasText(request.name())) {
                String name = like(request.name());
                predicates.add(cb.or(
                        cb.like(cb.lower(cb.coalesce(root.get("nameUz"), "")), name),
                        cb.like(cb.lower(cb.coalesce(root.get("nameUzCyril"), "")), name),
                        cb.like(cb.lower(cb.coalesce(root.get("nameRu"), "")), name),
                        cb.like(cb.lower(cb.coalesce(root.get("nameKaa"), "")), name)
                ));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static String like(String value) {
        return "%" + value.trim().toLowerCase(Locale.ROOT) + "%";
    }
}
