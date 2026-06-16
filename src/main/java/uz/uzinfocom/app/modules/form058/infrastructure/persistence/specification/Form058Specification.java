package uz.uzinfocom.app.modules.form058.infrastructure.persistence.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.modules.form058.application.query.Form058Filter;
import uz.uzinfocom.app.modules.form058.domain.model.Form058;

import java.util.ArrayList;
import java.util.List;

public final class Form058Specification {

    private Form058Specification() {
    }

    public static Specification<Form058> table(Form058Filter filter, Long organizationId, boolean received) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(
                    root.get(received ? "receiverOrganizationId" : "senderOrganizationId"),
                    organizationId
            ));

            if (filter != null && filter.status() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filter.status()));
            }
            if (filter != null && filter.dateFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("visitDate"), filter.dateFrom()));
            }
            if (filter != null && filter.dateTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("visitDate"), filter.dateTo()));
            }
            if (filter != null && StringUtils.hasText(filter.search())) {
                String search = "%" + filter.search().trim().toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("patientFullName")), search),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("patientNnuzb")), search),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("mkb10Code")), search),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("mkb10Name")), search)
                ));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    public static Specification<Form058> visibleById(Long id, Long organizationId) {
        return visible(organizationId).and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("id"), id));
    }

    public static Specification<Form058> visibleByNnuzb(String nnuzb, Long organizationId) {
        return visible(organizationId)
                .and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("patientNnuzb"), nnuzb));
    }

    public static Specification<Form058> visibleByCard(Long cardId, Long organizationId) {
        return visible(organizationId)
                .and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("assignedCardId"), cardId));
    }

    private static Specification<Form058> visible(Long organizationId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.or(
                criteriaBuilder.equal(root.get("senderOrganizationId"), organizationId),
                criteriaBuilder.equal(root.get("receiverOrganizationId"), organizationId)
        );
    }
}
