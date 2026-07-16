package uz.uzinfocom.app.modules.form0581.infrastructure.persistence.specification;

import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.modules.form0581.application.query.Form0581Filter;
import uz.uzinfocom.app.modules.form0581.domain.model.Form0581;
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
public class Form0581Specification {

    private static final String ID = "id";
    private static final String DELETED = "deleted";
    private static final String PATIENT = "patient";

    private static final String CREATED_AT = "createdAt";
    private static final ZoneId APPLICATION_ZONE = ZoneId.of("Asia/Tashkent");

    private static final String STATUS = "status";
    private static final String SOURCE = "source";

    private static final String DIAGNOSIS_INFO = "diagnosisInfo";
    private static final String MKB10_CODE = "mkb10Code";

    private static final String SENDER_ORGANIZATION_ID = "senderOrganizationId";
    private static final String RECEIVER_ORGANIZATION_ID = "receiverOrganizationId";

    private final SenderReceiverScopePredicateFactory scopePredicateFactory;
    private final OrganizationScopeOrganizationIdResolver organizationScopeOrganizationIdResolver;

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
                        documentValueExists(root, query, cb, normalizeDocumentValue(documentValue))
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
    }

    private void applyCreatedAtDateRangeFilter(
            List<Predicate> predicates,
            Root<Form0581> root,
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
     * live JPA subquery — matches Form058Specification's approach, since
     * `column IN (:list)` is estimated far more accurately by Postgres than
     * `column IN (subquery)`.
     */
    private Predicate organizationLocationPredicate(
            Root<Form0581> root,
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
            Root<Form0581> root,
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
            Root<Form0581> root,
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

    private String normalizeCode(String value) {
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeDocumentValue(String value) {
        return value.trim().toUpperCase(Locale.ROOT);
    }
}
