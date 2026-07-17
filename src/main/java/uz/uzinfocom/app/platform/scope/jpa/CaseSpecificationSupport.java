package uz.uzinfocom.app.platform.scope.jpa;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.patient.domain.model.PatientIdentifier;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;

/**
 * Shared predicate-building helpers for the "case" specifications (Form058,
 * Form0581) — pulled out because both specifications hand-rolled the exact
 * same Criteria API plumbing for date-range filtering, sender/receiver
 * organization-id filtering, and document-value lookup, differing only in
 * the root entity type.
 */
@Component
@RequiredArgsConstructor
public class CaseSpecificationSupport {

    private static final ZoneId APPLICATION_ZONE = ZoneId.of("Asia/Tashkent");

    private final OrganizationScopeOrganizationIdResolver organizationScopeOrganizationIdResolver;

    public <T> void applyCreatedAtDateRangeFilter(
            List<Predicate> predicates,
            Root<T> root,
            CriteriaBuilder cb,
            String createdAtField,
            LocalDate dateFrom,
            LocalDate dateTo
    ) {
        if (dateFrom != null) {
            Instant fromInclusive = dateFrom.atStartOfDay(APPLICATION_ZONE).toInstant();
            predicates.add(cb.greaterThanOrEqualTo(root.get(createdAtField), fromInclusive));
        }

        if (dateTo != null) {
            Instant toExclusive = dateTo.plusDays(1).atStartOfDay(APPLICATION_ZONE).toInstant();
            predicates.add(cb.lessThan(root.get(createdAtField), toExclusive));
        }
    }

    /**
     * Resolves organization ids via a cached, materialized list instead of a
     * live JPA subquery. Postgres estimates `column IN (:list)` far more
     * accurately than `column IN (subquery)` — the latter forces a full
     * backward scan regardless of indexes.
     */
    public <T> Predicate organizationLocationPredicate(
            Root<T> root,
            CriteriaBuilder cb,
            Boolean received,
            String senderOrganizationIdField,
            String receiverOrganizationIdField,
            String regionCode,
            String districtCode
    ) {
        List<Long> organizationIds = organizationScopeOrganizationIdResolver
                .resolveFilterOrganizationIds(regionCode, districtCode);

        if (received == null) {
            return cb.or(
                    organizationIdPredicate(root, cb, senderOrganizationIdField, organizationIds),
                    organizationIdPredicate(root, cb, receiverOrganizationIdField, organizationIds)
            );
        }

        String organizationField = Boolean.TRUE.equals(received)
                ? receiverOrganizationIdField
                : senderOrganizationIdField;

        return organizationIdPredicate(root, cb, organizationField, organizationIds);
    }

    public <T> Predicate organizationIdPredicate(
            Root<T> root,
            CriteriaBuilder cb,
            String organizationIdField,
            List<Long> organizationIds
    ) {
        if (organizationIds.isEmpty()) {
            return cb.disjunction();
        }

        return root.<Long>get(organizationIdField).in(organizationIds);
    }

    public <T> Predicate documentValueExists(
            Root<T> root,
            CriteriaQuery<?> query,
            CriteriaBuilder cb,
            String patientField,
            String documentValue
    ) {
        Subquery<Long> subquery = query.subquery(Long.class);
        Root<PatientIdentifier> identifier = subquery.from(PatientIdentifier.class);

        subquery.select(cb.literal(1L));
        subquery.where(
                cb.equal(identifier.get("patient").get("id"), root.get(patientField).get("id")),
                cb.equal(identifier.get("value"), documentValue)
        );

        return cb.exists(subquery);
    }

    public String normalizeCode(String value) {
        return value.trim().toUpperCase(Locale.ROOT);
    }
}
