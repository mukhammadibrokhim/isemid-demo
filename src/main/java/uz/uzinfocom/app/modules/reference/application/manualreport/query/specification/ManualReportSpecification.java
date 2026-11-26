package uz.uzinfocom.app.modules.reference.application.manualreport.query.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.modules.reference.application.manualreport.query.dto.ManualReportFilterRequest;
import uz.uzinfocom.app.modules.reference.domain.ManualReport;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ManualReportSpecification {

    private ManualReportSpecification() {
    }

    public static Specification<ManualReport> byFilter(ManualReportFilterRequest request) {
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

            if (StringUtils.hasText(request.name())) {
                String name = like(request.name());
                predicates.add(cb.or(
                        cb.like(cb.lower(cb.coalesce(root.get("nameUz"), "")), name),
                        cb.like(cb.lower(cb.coalesce(root.get("nameUzCyril"), "")), name),
                        cb.like(cb.lower(cb.coalesce(root.get("nameRu"), "")), name),
                        cb.like(cb.lower(cb.coalesce(root.get("nameKaa"), "")), name)
                ));
            }

            if (StringUtils.hasText(request.reportType())) {
                predicates.add(hasElement(root, query, cb, "reportTypes", request.reportType()));
            }

            if (StringUtils.hasText(request.icd10Code())) {
                predicates.add(hasElement(root, query, cb, "icd10Codes", request.icd10Code()));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    // EXISTS rather than a join on the outer query: no duplicate rows, so paging and count stay correct.
    private static Predicate hasElement(
            Root<ManualReport> root,
            CriteriaQuery<?> query,
            CriteriaBuilder cb,
            String collection,
            String value
    ) {
        Subquery<Integer> subquery = query.subquery(Integer.class);
        Join<ManualReport, String> element = subquery.correlate(root).join(collection);

        subquery.select(cb.literal(1))
                .where(cb.equal(cb.upper(element), value.trim().toUpperCase(Locale.ROOT)));

        return cb.exists(subquery);
    }

    private static String like(String value) {
        return "%" + value.trim().toLowerCase(Locale.ROOT) + "%";
    }
}
