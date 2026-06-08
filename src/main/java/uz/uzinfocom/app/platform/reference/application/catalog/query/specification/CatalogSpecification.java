package uz.uzinfocom.app.platform.reference.application.catalog.query.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.platform.reference.application.catalog.query.dto.CatalogFilterRequest;
import uz.uzinfocom.app.platform.reference.domain.Catalog;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class CatalogSpecification {

    private CatalogSpecification() {
    }

    public static Specification<Catalog> byFilter(CatalogFilterRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("deleted"), false));

            String type = request.type();
            if (type != null) {
                predicates.add(cb.equal(root.get("type"), type));
            }

            if (StringUtils.hasText(request.code())) {
                predicates.add(cb.equal(root.get("code"), request.code().trim().toUpperCase(Locale.ROOT)));
            }

            if (StringUtils.hasText(request.parentCode())) {
                predicates.add(cb.equal(root.get("parentCode"), request.parentCode().trim().toUpperCase(Locale.ROOT)));
            }

            if (StringUtils.hasText(request.search())) {
                String search = like(request.search());
                predicates.add(cb.or(
                        cb.like(cb.lower(cb.coalesce(root.get("code"), "")), search),
                        cb.like(cb.lower(cb.coalesce(root.get("nameUz"), "")), search),
                        cb.like(cb.lower(cb.coalesce(root.get("nameUzCyril"), "")), search),
                        cb.like(cb.lower(cb.coalesce(root.get("nameRu"), "")), search),
                        cb.like(cb.lower(cb.coalesce(root.get("nameKaa"), "")), search)
                ));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static String like(String value) {
        return "%" + value.trim().toLowerCase(Locale.ROOT) + "%";
    }
}
