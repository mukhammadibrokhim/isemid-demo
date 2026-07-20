package uz.uzinfocom.app.modules.form0581.infrastructure.persistence.specification;

import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.modules.form0581.application.query.Form0581Filter;
import uz.uzinfocom.app.modules.form0581.domain.model.Form0581;
import uz.uzinfocom.app.platform.scope.ResolvedOrganizationScope;
import uz.uzinfocom.app.platform.scope.jpa.CaseSpecificationSupport;
import uz.uzinfocom.app.platform.scope.jpa.SenderReceiverScopePredicateFactory;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class Form0581Specification {

    private static final String ID = "id";
    private static final String DELETED = "deleted";
    private static final String PATIENT = "patient";

    private static final String CREATED_AT = "createdAt";

    private static final String STATUS = "status";
    private static final String SOURCE = "source";

    private static final String DIAGNOSIS_INFO = "diagnosisInfo";
    private static final String MKB10_CODE = "mkb10Code";

    private static final String SENDER_ORGANIZATION_ID = "senderOrganizationId";
    private static final String RECEIVER_ORGANIZATION_ID = "receiverOrganizationId";

    private final SenderReceiverScopePredicateFactory scopePredicateFactory;
    private final CaseSpecificationSupport caseSpecificationSupport;

    public Specification<Form0581> table(
            Form0581Filter filter,
            ResolvedOrganizationScope scope,
            Boolean received
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.isFalse(root.get("deleteInfo").get(DELETED)));
            predicates.add(scopePredicateFactory.applyDirectionScope(root, cb, scope, received));

            applyFilters(predicates, root, query, cb, filter, received);

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    /**
     * True cross-organization view (no sender/receiver restriction at all).
     * Callers must gate this behind a super-admin authorization check —
     * this specification intentionally does not enforce any scope itself.
     */
    public Specification<Form0581> tableUnscoped(Form0581Filter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.isFalse(root.get("deleteInfo").get(DELETED)));

            applyFilters(predicates, root, query, cb, filter, null);

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    public Specification<Form0581> visibleById(
            Long id,
            ResolvedOrganizationScope scope
    ) {
        return visible(scope)
                .and((root, query, cb) -> cb.equal(root.get(ID), id));
    }

    public Specification<Form0581> visibleByDocumentValue(
            String documentValue,
            ResolvedOrganizationScope scope
    ) {
        return visible(scope)
                .and((root, query, cb) ->
                        caseSpecificationSupport.documentValueExists(root, query, cb, PATIENT, normalizeDocumentValue(documentValue))
                );
    }

    private Specification<Form0581> visible(ResolvedOrganizationScope scope) {
        return (root, query, cb) -> cb.and(
                cb.isFalse(root.get("deleteInfo").get(DELETED)),
                scopePredicateFactory.applyDirectionScope(root, cb, scope, null)
        );
    }

    private void applyFilters(
            List<Predicate> predicates,
            Root<Form0581> root,
            CriteriaQuery<?> query,
            CriteriaBuilder cb,
            Form0581Filter filter,
            Boolean received
    ) {
        if (filter == null) {
            return;
        }

        if (filter.id() != null) {
            predicates.add(cb.equal(root.get(ID), filter.id()));
        }

        if (filter.dateFrom() != null || filter.dateTo() != null) {
            caseSpecificationSupport.applyCreatedAtDateRangeFilter(
                    predicates,
                    root,
                    cb,
                    CREATED_AT,
                    filter.dateFrom(),
                    filter.dateTo()
            );
        }

        if (StringUtils.hasText(filter.documentValue())) {
            predicates.add(caseSpecificationSupport.documentValueExists(
                    root, query, cb, PATIENT, normalizeDocumentValue(filter.documentValue())
            ));
        }

        if (filter.status() != null) {
            predicates.add(cb.equal(root.get(STATUS), filter.status()));
        }

        if (StringUtils.hasText(filter.mkb10Code())) {
            predicates.add(cb.equal(root.get(DIAGNOSIS_INFO).get(MKB10_CODE), normalizeCode(filter.mkb10Code())));
        }

        if (filter.organizationId() != null) {
            predicates.add(caseSpecificationSupport.directionalOrganizationIdPredicate(
                    root, cb, received, SENDER_ORGANIZATION_ID, RECEIVER_ORGANIZATION_ID, filter.organizationId()
            ));
        }

        if (StringUtils.hasText(filter.regionCode()) || StringUtils.hasText(filter.districtCode())) {
            predicates.add(caseSpecificationSupport.organizationLocationPredicate(
                    root, cb, received, SENDER_ORGANIZATION_ID, RECEIVER_ORGANIZATION_ID,
                    filter.regionCode(), filter.districtCode()
            ));
        }

        if (StringUtils.hasText(filter.source())) {
            predicates.add(cb.equal(root.get(SOURCE), normalizeCode(filter.source())));
        }
    }

    private String normalizeCode(String value) {
        return caseSpecificationSupport.normalizeCode(value);
    }

    private String normalizeDocumentValue(String value) {
        return caseSpecificationSupport.normalizeCode(value);
    }
}
