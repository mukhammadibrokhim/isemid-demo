package uz.uzinfocom.app.modules.form058.infrastructure.persistence.specification;

import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.modules.form058.application.query.Form058Filter;
import uz.uzinfocom.app.modules.form058.domain.model.Form058;
import uz.uzinfocom.app.modules.patient.domain.enums.AffiliationType;
import uz.uzinfocom.app.modules.patient.domain.model.PatientAffiliation;
import uz.uzinfocom.app.platform.scope.ResolvedOrganizationScope;
import uz.uzinfocom.app.platform.scope.jpa.CaseSpecificationSupport;
import uz.uzinfocom.app.platform.scope.jpa.SenderReceiverScopePredicateFactory;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class Form058Specification {

    private static final String ID = "id";
    private static final String DELETED = "deleted";
    private static final String PATIENT = "patient";

    private static final String CREATED_AT = "createdAt";

    private static final String STATUS = "status";
    private static final String SOURCE = "source";
    private static final String HAS_LINKED_CARDS = "hasLinkedCards";

    private static final String DIAGNOSIS_INFO = "diagnosisInfo";
    private static final String MKB10_CODE = "mkb10Code";

    private static final String SENDER_ORGANIZATION_ID = "senderOrganizationId";
    private static final String RECEIVER_ORGANIZATION_ID = "receiverOrganizationId";

    private final SenderReceiverScopePredicateFactory scopePredicateFactory;
    private final CaseSpecificationSupport caseSpecificationSupport;

    public Specification<Form058> table(
            Form058Filter filter,
            ResolvedOrganizationScope scope,
            Boolean received
    ) {
        return (root, query, cb) -> {
            fetchPatientForTableQuery(root, query);

            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.isFalse(root.get("deleteInfo").get(DELETED)));
            predicates.add(accessScopePredicate(root, query, cb, filter, scope, received));

            applyFilters(predicates, root, query, cb, filter, received);

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    /**
     * True cross-organization view (no sender/receiver restriction at all).
     * Callers must gate this behind a super-admin authorization check —
     * this specification intentionally does not enforce any scope itself.
     */
    public Specification<Form058> tableUnscoped(
            Form058Filter filter,
            ResolvedOrganizationScope scope
    ) {
        return (root, query, cb) -> {
            fetchPatientForTableQuery(root, query);

            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.isFalse(root.get("deleteInfo").get(DELETED)));

            if (filter != null && filter.isAffiliationFilterEnabled()) {
                predicates.add(patientAffiliationExists(root, query, cb, scope.organizationId()));
            }

            applyFilters(predicates, root, query, cb, filter, null);

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    /**
     * Eagerly joins {@code patient} (a {@code @ManyToOne}, so safe alongside pagination -
     * unlike a {@code @OneToMany} fetch, it can't multiply/duplicate rows) so the table
     * projection's {@code patient.firstName/lastName/middleName} no longer triggers a lazy
     * load per row. Skipped for the COUNT(*) query Spring Data issues for pagination, where a
     * fetch is meaningless and Hibernate rejects it outright.
     */
    private void fetchPatientForTableQuery(Root<Form058> root, CriteriaQuery<?> query) {
        if (Form058.class.equals(query.getResultType())) {
            root.fetch(PATIENT, JoinType.LEFT);
        }
    }

    public Specification<Form058> visibleById(
            Long id,
            ResolvedOrganizationScope scope
    ) {
        return visible(scope)
                .and((root, query, cb) -> cb.equal(root.get(ID), id));
    }

    public Specification<Form058> visibleByNnuzb(
            String nnuzb,
            ResolvedOrganizationScope scope
    ) {
        return visible(scope)
                .and((root, query, cb) ->
                        caseSpecificationSupport.documentValueExists(root, query, cb, PATIENT, normalizeDocumentValue(nnuzb))
                );
    }

    private Specification<Form058> visible(ResolvedOrganizationScope scope) {
        return (root, query, cb) -> cb.and(
                cb.isFalse(root.get("deleteInfo").get(DELETED)),
                scopePredicateFactory.applyDirectionScope(root, cb, scope, null)
        );
    }

    /**
     * Access scope:
     * <p>
     * Default:
     * - OUTGOING -> scope by senderOrganizationId
     * - INCOMING -> scope by receiverOrganizationId
     * - ALL -> scope by sender or receiver
     * <p>
     * Affiliation mode:
     * - visible only by patient affiliation
     */
    private Predicate accessScopePredicate(
            Root<Form058> root,
            CriteriaQuery<?> query,
            CriteriaBuilder cb,
            Form058Filter filter,
            ResolvedOrganizationScope scope,
            Boolean received
    ) {
        if (filter != null && filter.isAffiliationFilterEnabled()) {
            return patientAffiliationExists(root, query, cb, scope.organizationId());
        }

        return scopePredicateFactory.applyDirectionScope(root, cb, scope, received);
    }

    private void applyFilters(
            List<Predicate> predicates,
            Root<Form058> root,
            CriteriaQuery<?> query,
            CriteriaBuilder cb,
            Form058Filter filter,
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

        if (filter.hasLinkedCards() != null) {
            predicates.add(cb.equal(root.get(HAS_LINKED_CARDS), filter.hasLinkedCards()));
        }
    }

    private Predicate patientAffiliationExists(
            Root<Form058> root,
            CriteriaQuery<?> query,
            CriteriaBuilder cb,
            Long currentOrganizationId
    ) {
        if (currentOrganizationId == null) {
            return cb.disjunction();
        }

        Subquery<Long> subquery = query.subquery(Long.class);
        Root<PatientAffiliation> affiliation = subquery.from(PatientAffiliation.class);

        subquery.select(cb.literal(1L));
        subquery.where(
                cb.equal(affiliation.get(PATIENT).get(ID), root.get(PATIENT).get(ID)),
                cb.equal(affiliation.get("organizationId"), currentOrganizationId),
                affiliation.get("type").in(AffiliationType.WORKPLACE, AffiliationType.EDUCATIONAL)
        );

        return cb.exists(subquery);
    }

    private String normalizeCode(String value) {
        return caseSpecificationSupport.normalizeCode(value);
    }

    private String normalizeDocumentValue(String value) {
        return caseSpecificationSupport.normalizeCode(value);
    }
}
