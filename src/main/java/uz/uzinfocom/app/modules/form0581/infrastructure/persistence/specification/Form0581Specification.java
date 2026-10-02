package uz.uzinfocom.app.modules.form0581.infrastructure.persistence.specification;

import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.modules.form0581.application.query.Form0581AffiliatedFilter;
import uz.uzinfocom.app.modules.form0581.application.query.Form0581Filter;
import uz.uzinfocom.app.modules.form0581.application.query.Form0581FilterFields;
import uz.uzinfocom.app.modules.form0581.domain.model.Form0581;
import uz.uzinfocom.app.modules.patient.domain.enums.AffiliationType;
import uz.uzinfocom.app.modules.patient.domain.model.PatientAffiliation;
import uz.uzinfocom.app.orchestration.scope.ResolvedOrganizationScope;
import uz.uzinfocom.app.orchestration.scope.jpa.CaseSpecificationSupport;
import uz.uzinfocom.app.orchestration.scope.jpa.SenderReceiverScopePredicateFactory;

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
    private static final String ICD10_CODE = "icd10Code";

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

            applyFilters(predicates, root, query, cb, filter);
            applySenderReceiverFilters(predicates, root, cb, filter, received);

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

            applyFilters(predicates, root, query, cb, filter);
            applySenderReceiverFilters(predicates, root, cb, filter, null);

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    /**
     * {@code GET /v1/form-058-1/affiliated} - forms visible to {@code
     * organizationId} solely because the patient's workplace or place of
     * study is that organization, independent of sender/receiver. Unlike
     * {@link #table}/{@link #tableUnscoped}, the affiliation predicate here
     * is unconditional (this specification exists only for that mode), and
     * {@code organizationId}/region-district filtering — both meaningless
     * once access isn't sender/receiver-scoped — simply aren't offered by
     * {@link Form0581AffiliatedFilter} in the first place. Mirrors {@code
     * Form058Specification#affiliatedTable}.
     */
    public Specification<Form0581> affiliatedTable(
            Form0581AffiliatedFilter filter,
            Long organizationId
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.isFalse(root.get("deleteInfo").get(DELETED)));
            predicates.add(patientAffiliationExists(root, query, cb, organizationId));

            applyFilters(predicates, root, query, cb, filter);

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

    /**
     * For outbound-integration lookups (e.g. an integration client asking
     * "what's the latest form058-1 for this identifier"): visible only if
     * the caller's own organization was sender or receiver on it - the same
     * either-side rule {@link #visible} applies via
     * {@code applyDirectionScope(..., received=null)}, just against a
     * single organization id already resolved by the caller rather than a
     * full {@link ResolvedOrganizationScope}.
     * <p>
     * Matches on identifier value only, like {@link #visibleByDocumentValue} -
     * patient records are not deduplicated across separate submissions in
     * this system (the same real person's PINFL can be attached to several
     * distinct {@code Patient} rows), so resolving one "canonical" patient
     * first and then looking up their forms would silently miss forms
     * attached to any other duplicate. Matching the form directly by
     * identifier value, the same way the existing document-value lookups
     * already do, finds every form regardless of which patient row it
     * happens to reference.
     */
    public Specification<Form0581> visibleByDocumentValueAndOrganization(
            String documentValue,
            Long organizationId
    ) {
        return (root, query, cb) -> cb.and(
                cb.isFalse(root.get("deleteInfo").get(DELETED)),
                caseSpecificationSupport.documentValueExists(root, query, cb, PATIENT, normalizeDocumentValue(documentValue)),
                caseSpecificationSupport.directionalOrganizationIdPredicate(
                        root, cb, null, SENDER_ORGANIZATION_ID, RECEIVER_ORGANIZATION_ID, organizationId
                )
        );
    }

    private Specification<Form0581> visible(ResolvedOrganizationScope scope) {
        return (root, query, cb) -> cb.and(
                cb.isFalse(root.get("deleteInfo").get(DELETED)),
                scopePredicateFactory.applyDirectionScope(root, cb, scope, null)
        );
    }

    /**
     * Common part shared by every Form0581 listing, regardless of how its
     * access scope is determined — see {@link Form0581FilterFields}.
     */
    private void applyFilters(
            List<Predicate> predicates,
            Root<Form0581> root,
            CriteriaQuery<?> query,
            CriteriaBuilder cb,
            Form0581FilterFields filter
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

        if (StringUtils.hasText(filter.icd10Code())) {
            predicates.add(cb.equal(root.get(DIAGNOSIS_INFO).get(ICD10_CODE), normalizeCode(filter.icd10Code())));
        }

        if (StringUtils.hasText(filter.source())) {
            predicates.add(cb.equal(root.get(SOURCE), normalizeCode(filter.source())));
        }
    }

    /**
     * {@code organizationId}/region-district filtering — both mean "sender
     * or receiver", so they only apply to the direction-scoped listings
     * ({@link #table}/{@link #tableUnscoped}), never to {@link
     * #affiliatedTable}.
     */
    private void applySenderReceiverFilters(
            List<Predicate> predicates,
            Root<Form0581> root,
            CriteriaBuilder cb,
            Form0581Filter filter,
            Boolean received
    ) {
        if (filter == null) {
            return;
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
    }

    private Predicate patientAffiliationExists(
            Root<Form0581> root,
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
