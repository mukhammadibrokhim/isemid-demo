package uz.uzinfocom.app.modules.form129.infrastructure.persistence.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.modules.form129.application.query.Form129Filter;
import uz.uzinfocom.app.modules.form129.domain.model.Form129;
import uz.uzinfocom.app.orchestration.scope.ResolvedOrganizationScope;
import uz.uzinfocom.app.orchestration.scope.jpa.CaseSpecificationSupport;
import uz.uzinfocom.app.orchestration.scope.jpa.SenderReceiverScopePredicateFactory;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class Form129Specification {

    private static final String ID = "id";
    private static final String PATIENT = "patient";
    private static final String CREATED_AT = "createdAt";
    private static final String STATUS = "status";
    private static final String SOURCE = "source";
    private static final String SENDER_ORGANIZATION_ID = "senderOrganizationId";
    private static final String RECEIVER_ORGANIZATION_ID = "receiverOrganizationId";

    private final SenderReceiverScopePredicateFactory scopePredicateFactory;
    private final CaseSpecificationSupport caseSpecificationSupport;

    public Specification<Form129> table(
            Form129Filter filter,
            ResolvedOrganizationScope scope,
            Boolean received
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(scopePredicateFactory.applyDirectionScope(root, cb, scope, received));

            applyFilters(predicates, root, query, cb, filter);
            applySenderReceiverFilters(predicates, root, cb, filter, received);

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    /**
     * True cross-organization view (no sender/receiver restriction). Callers
     * must gate this behind a super-admin authorization check themselves —
     * this specification intentionally does not enforce any scope.
     */
    public Specification<Form129> tableUnscoped(Form129Filter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            applyFilters(predicates, root, query, cb, filter);
            applySenderReceiverFilters(predicates, root, cb, filter, null);

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    public Specification<Form129> visibleById(Long id, ResolvedOrganizationScope scope) {
        return (root, query, cb) -> cb.and(
                scopePredicateFactory.applyDirectionScope(root, cb, scope, null),
                cb.equal(root.get(ID), id)
        );
    }

    private void applyFilters(
            List<Predicate> predicates,
            Root<Form129> root,
            CriteriaQuery<?> query,
            CriteriaBuilder cb,
            Form129Filter filter
    ) {
        if (filter == null) {
            return;
        }

        if (filter.id() != null) {
            predicates.add(cb.equal(root.get(ID), filter.id()));
        }

        if (filter.dateFrom() != null || filter.dateTo() != null) {
            caseSpecificationSupport.applyCreatedAtDateRangeFilter(
                    predicates, root, cb, CREATED_AT, filter.dateFrom(), filter.dateTo()
            );
        }

        if (StringUtils.hasText(filter.documentValue())) {
            predicates.add(caseSpecificationSupport.documentValueExists(
                    root, query, cb, PATIENT, caseSpecificationSupport.normalizeCode(filter.documentValue())
            ));
        }

        if (filter.status() != null) {
            predicates.add(cb.equal(root.get(STATUS), filter.status()));
        }

        if (StringUtils.hasText(filter.source())) {
            predicates.add(cb.equal(root.get(SOURCE), caseSpecificationSupport.normalizeCode(filter.source())));
        }
    }

    private void applySenderReceiverFilters(
            List<Predicate> predicates,
            Root<Form129> root,
            CriteriaBuilder cb,
            Form129Filter filter,
            Boolean received
    ) {
        if (filter == null || filter.organizationId() == null) {
            return;
        }

        predicates.add(caseSpecificationSupport.directionalOrganizationIdPredicate(
                root, cb, received, SENDER_ORGANIZATION_ID, RECEIVER_ORGANIZATION_ID, filter.organizationId()
        ));
    }
}
