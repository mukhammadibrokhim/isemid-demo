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
import uz.uzinfocom.app.modules.patient.domain.model.PatientIdentifier;
import uz.uzinfocom.app.platform.scope.ResolvedOrganizationScope;
import uz.uzinfocom.app.platform.scope.jpa.OrganizationScopeOrganizationIdResolver;
import uz.uzinfocom.app.platform.scope.jpa.SenderReceiverScopePredicateFactory;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class Form058Specification {

    private static final String ID = "id";
    private static final String DELETED = "deleted";
    private static final String PATIENT = "patient";

    private static final String CREATED_AT = "createdAt";
    private static final ZoneId APPLICATION_ZONE = ZoneId.of("Asia/Tashkent");

    private static final String STATUS = "status";
    private static final String SOURCE = "source";
    private static final String HAS_LINKED_CARDS = "hasLinkedCards";

    private static final String DIAGNOSIS_INFO = "diagnosisInfo";
    private static final String MKB10_CODE = "mkb10Code";

    private static final String SENDER_ORGANIZATION_ID = "senderOrganizationId";
    private static final String RECEIVER_ORGANIZATION_ID = "receiverOrganizationId";

    private final SenderReceiverScopePredicateFactory scopePredicateFactory;
    private final OrganizationScopeOrganizationIdResolver organizationScopeOrganizationIdResolver;

    public Specification<Form058> table(
            Form058Filter filter,
            ResolvedOrganizationScope scope,
            Boolean received
    ) {
        return (root, query, cb) -> {
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
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.isFalse(root.get("deleteInfo").get(DELETED)));

            if (filter != null && filter.isAffiliationFilterEnabled()) {
                predicates.add(patientAffiliationExists(root, query, cb, scope.organizationId()));
            }

            applyFilters(predicates, root, query, cb, filter, null);

            return cb.and(predicates.toArray(Predicate[]::new));
        };
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
                        documentValueExists(root, query, cb, normalizeDocumentValue(nnuzb))
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
            applyCreatedAtDateRangeFilter(
                    predicates,
                    root,
                    cb,
                    filter.dateFrom(),
                    filter.dateTo()
            );
        }

        if (StringUtils.hasText(filter.documentValue())) {
            predicates.add(documentValueExists(root, query, cb, normalizeDocumentValue(filter.documentValue())));
        }

        if (filter.status() != null) {
            predicates.add(cb.equal(root.get(STATUS), filter.status()));
        }

        if (StringUtils.hasText(filter.mkb10Code())) {
            predicates.add(cb.equal(root.get(DIAGNOSIS_INFO).get(MKB10_CODE), normalizeCode(filter.mkb10Code())));
        }

        if (filter.senderOrganizationId() != null) {
            predicates.add(cb.equal(root.get(SENDER_ORGANIZATION_ID), filter.senderOrganizationId()));
        }

        if (StringUtils.hasText(filter.regionCode()) || StringUtils.hasText(filter.districtCode())) {
            predicates.add(organizationLocationPredicate(root, cb, received, filter.regionCode(), filter.districtCode()));
        }

        if (StringUtils.hasText(filter.source())) {
            predicates.add(cb.equal(root.get(SOURCE), normalizeCode(filter.source())));
        }

        if (filter.hasLinkedCards() != null) {
            predicates.add(cb.equal(root.get(HAS_LINKED_CARDS), filter.hasLinkedCards()));
        }
    }

    private void applyCreatedAtDateRangeFilter(
            List<Predicate> predicates,
            Root<Form058> root,
            CriteriaBuilder cb,
            LocalDate dateFrom,
            LocalDate dateTo
    ) {
        if (dateFrom != null) {
            Instant fromInclusive = dateFrom.atStartOfDay(APPLICATION_ZONE).toInstant();

            predicates.add(cb.greaterThanOrEqualTo(root.get(CREATED_AT), fromInclusive));
        }

        if (dateTo != null) {
            Instant toExclusive = dateTo.plusDays(1).atStartOfDay(APPLICATION_ZONE).toInstant();

            predicates.add(cb.lessThan(root.get(CREATED_AT), toExclusive));
        }
    }

    /**
     * Resolves organization ids via a cached, materialized list instead of a
     * live JPA subquery. Postgres estimates `column IN (:list)` far more
     * accurately than `column IN (subquery)` — the latter forced a full
     * backward scan of form058 regardless of indexes (see resolveFilterOrganizationIds).
     */
    private Predicate organizationLocationPredicate(
            Root<Form058> root,
            CriteriaBuilder cb,
            Boolean received,
            String regionCode,
            String districtCode
    ) {
        List<Long> organizationIds = organizationScopeOrganizationIdResolver
                .resolveFilterOrganizationIds(regionCode, districtCode);

        if (received == null) {
            return cb.or(
                    organizationIdPredicate(root, cb, SENDER_ORGANIZATION_ID, organizationIds),
                    organizationIdPredicate(root, cb, RECEIVER_ORGANIZATION_ID, organizationIds)
            );
        }

        String organizationField = Boolean.TRUE.equals(received)
                ? RECEIVER_ORGANIZATION_ID
                : SENDER_ORGANIZATION_ID;

        return organizationIdPredicate(root, cb, organizationField, organizationIds);
    }

    private Predicate organizationIdPredicate(
            Root<Form058> root,
            CriteriaBuilder cb,
            String organizationIdField,
            List<Long> organizationIds
    ) {
        if (organizationIds.isEmpty()) {
            return cb.disjunction();
        }

        return root.<Long>get(organizationIdField).in(organizationIds);
    }

    private Predicate documentValueExists(
            Root<Form058> root,
            CriteriaQuery<?> query,
            CriteriaBuilder cb,
            String documentValue
    ) {
        Subquery<Long> subquery = query.subquery(Long.class);
        Root<PatientIdentifier> identifier = subquery.from(PatientIdentifier.class);

        subquery.select(cb.literal(1L));
        subquery.where(
                cb.equal(identifier.get(PATIENT).get(ID), root.get(PATIENT).get(ID)),
                cb.equal(identifier.get("value"), documentValue)
        );

        return cb.exists(subquery);
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
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeDocumentValue(String value) {
        return value.trim().toUpperCase(Locale.ROOT);
    }
}