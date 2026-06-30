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
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.scope.ResolvedOrganizationScope;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class Form058Specification {

    private static final String SENDER_ORGANIZATION_ID = "senderOrganizationId";

    private final Form058ScopePredicateFactory form058ScopePredicateFactory;

    public Specification<Form058> table(
            Form058Filter filter,
            ResolvedOrganizationScope scope,
            Boolean received
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.isFalse(root.get("deleted")));

            predicates.add(accessScopePredicate(
                    root,
                    query,
                    cb,
                    filter,
                    scope,
                    received
            ));

            applyFilters(predicates, root, query, cb, filter);

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    public Specification<Form058> visibleById(
            Long id,
            ResolvedOrganizationScope scope
    ) {
        return visible(scope)
                .and((root, query, cb) ->
                        cb.equal(root.get("id"), id)
                );
    }

    public Specification<Form058> visibleByNnuzb(
            String nnuzb,
            ResolvedOrganizationScope scope
    ) {
        return visible(scope)
                .and((root, query, cb) ->
                        documentValueExists(root, query, cb, nnuzb)
                );
    }

    private Specification<Form058> visible(ResolvedOrganizationScope scope) {
        return (root, query, cb) -> cb.and(
                cb.isFalse(root.get("deleted")),
                form058ScopePredicateFactory.applyDirectionScope(
                        root,
                        query,
                        cb,
                        scope,
                        null
                )
        );
    }

    /**
     * Builds the access scope predicate for the Form058 table.
     * <p>
     * Default mode:
     * Form058 records are filtered by the regular sender/receiver organization scope.
     * <p>
     * Affiliation mode:
     * Form058 records are filtered only by patient affiliation.
     * The patient must have WORKPLACE or EDUCATIONAL affiliation with the current organization.
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
            return patientAffiliationExists(
                    root,
                    query,
                    cb,
                    scope.organizationUuid()
            );
        }

        return form058ScopePredicateFactory.applyDirectionScope(
                root,
                query,
                cb,
                scope,
                received
        );
    }

    private void applyFilters(
            List<Predicate> predicates,
            Root<Form058> root,
            CriteriaQuery<?> query,
            CriteriaBuilder cb,
            Form058Filter filter
    ) {
        if (filter == null) {
            return;
        }

        if (filter.id() != null) {
            predicates.add(cb.equal(
                    root.get("id"),
                    filter.id()
            ));
        }

        if (StringUtils.hasText(filter.documentValue())) {
            predicates.add(documentValueExists(
                    root,
                    query,
                    cb,
                    normalize(filter.documentValue())
            ));
        }

        if (filter.status() != null) {
            predicates.add(cb.equal(
                    root.get("status"),
                    filter.status()
            ));
        }

        if (StringUtils.hasText(filter.mkb10Code())) {
            predicates.add(equalIgnoreCase(
                    cb,
                    root.get("diagnosisInfo").get("mkb10Code"),
                    filter.mkb10Code()
            ));
        }

        if (filter.senderOrganizationId() != null) {
            predicates.add(cb.equal(
                    root.get(SENDER_ORGANIZATION_ID),
                    filter.senderOrganizationId()
            ));
        }

        if (StringUtils.hasText(filter.regionCode())) {
            predicates.add(root.<Long>get(SENDER_ORGANIZATION_ID).in(
                    organizationIdsByRegion(query, cb, filter.regionCode())
            ));
        }

        if (StringUtils.hasText(filter.districtCode())) {
            predicates.add(root.<Long>get(SENDER_ORGANIZATION_ID).in(
                    organizationIdsByDistrict(query, cb, filter.districtCode())
            ));
        }

        if (StringUtils.hasText(filter.source())) {
            predicates.add(cb.equal(
                    root.get("source"),
                    filter.source().trim().toUpperCase(Locale.ROOT)
            ));
        }

        if (filter.hasLinkedCards() != null) {
            predicates.add(cb.equal(
                    root.get("hasLinkedCards"),
                    filter.hasLinkedCards()
            ));
        }
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
                cb.equal(
                        identifier.get("patient").get("id"),
                        root.get("patient").get("id")
                ),
                cb.equal(
                        identifier.get("value"),
                        documentValue
                )
        );

        return cb.exists(subquery);
    }

    private Predicate patientAffiliationExists(
            Root<Form058> root,
            CriteriaQuery<?> query,
            CriteriaBuilder cb,
            UUID currentOrganizationUuid
    ) {
        if (currentOrganizationUuid == null) {
            return cb.disjunction();
        }

        Subquery<Long> subquery = query.subquery(Long.class);
        Root<PatientAffiliation> affiliation = subquery.from(PatientAffiliation.class);

        subquery.select(cb.literal(1L));
        subquery.where(
                cb.equal(
                        affiliation.get("patient").get("id"),
                        root.get("patient").get("id")
                ),
                cb.equal(
                        affiliation.get("organizationUuid"),
                        currentOrganizationUuid
                ),
                affiliation.get("type").in(
                        AffiliationType.WORKPLACE,
                        AffiliationType.EDUCATIONAL
                )
        );

        return cb.exists(subquery);
    }

    private Subquery<Long> organizationIdsByRegion(
            CriteriaQuery<?> query,
            CriteriaBuilder cb,
            String regionCode
    ) {
        Subquery<Long> subquery = query.subquery(Long.class);
        Root<Organization> organization = subquery.from(Organization.class);

        subquery.select(organization.get("id"))
                .where(
                        cb.equal(
                                cb.lower(organization.get("regionCode")),
                                normalize(regionCode)
                        ),
                        cb.isFalse(organization.get("deleted"))
                );

        return subquery;
    }

    private Subquery<Long> organizationIdsByDistrict(
            CriteriaQuery<?> query,
            CriteriaBuilder cb,
            String districtCode
    ) {
        Subquery<Long> subquery = query.subquery(Long.class);
        Root<Organization> organization = subquery.from(Organization.class);

        subquery.select(organization.get("id"))
                .where(
                        cb.equal(
                                cb.lower(organization.get("districtCode")),
                                normalize(districtCode)
                        ),
                        cb.isFalse(organization.get("deleted"))
                );

        return subquery;
    }

    private Predicate equalIgnoreCase(
            CriteriaBuilder cb,
            Expression<String> field,
            String value
    ) {
        return cb.equal(
                cb.lower(field),
                normalize(value)
        );
    }

    private String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }
}