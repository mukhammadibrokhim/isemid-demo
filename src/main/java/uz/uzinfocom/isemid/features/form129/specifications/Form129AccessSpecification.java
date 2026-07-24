package uz.uzinfocom.isemid.features.form129.specifications;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import uz.uzinfocom.isemid.commons.security.scope.ResolvedAccessScope;
import uz.uzinfocom.isemid.features.form058.models.enums.FilterType;
import uz.uzinfocom.isemid.features.form129.models.Form129;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.organization.models.enums.LevelType;
import uz.uzinfocom.isemid.features.patient.models.Affiliation;
import uz.uzinfocom.isemid.features.patient.models.enums.AffiliationType;

/**
 * Builds Form129-specific access filtering rules.
 *
 * <p>Mirrors {@code Form058AccessSpecification} exactly: Form129 stores raw
 * {@code senderOrganizationId} / {@code receiverOrganizationId} columns (no JPA relation),
 * just like Form058.
 */
@Component
public class Form129AccessSpecification {

    public Specification<Form129> byScope(
            FilterType filterType,
            ResolvedAccessScope scope,
            Long currentOrganizationId,
            Boolean affiliation
    ) {
        FilterType effectiveType = filterType == null ? FilterType.SENDER : filterType;

        Specification<Form129> accessSpec = switch (effectiveType) {
            case SENDER -> organizationIdInScope("senderOrganizationId", scope);
            case RECEIVER -> organizationIdInScope("receiverOrganizationId", scope);
            case ALL -> Specification.<Form129>where(organizationIdInScope("senderOrganizationId", scope))
                    .or(organizationIdInScope("receiverOrganizationId", scope));
        };

        if (Boolean.TRUE.equals(affiliation)) {
            accessSpec = accessSpec.and(hasAffiliation(currentOrganizationId));
        }

        return accessSpec;
    }

    /**
     * Applies affiliation filter through Form129 -> patient -> affiliations.
     *
     * <p>Business rule preserved from the sibling forms: patient must have affiliation
     * for the current organization and affiliation type must be WORKPLACE or EDUCATIONAL.
     */
    private Specification<Form129> hasAffiliation(Long currentOrganizationId) {
        return (root, query, cb) -> {
            if (currentOrganizationId == null) {
                return cb.disjunction();
            }

            assert query != null;
            Subquery<Long> affiliationExistsSubquery = query.subquery(Long.class);
            Root<Affiliation> affiliationRoot = affiliationExistsSubquery.from(Affiliation.class);

            affiliationExistsSubquery.select(cb.literal(1L));
            affiliationExistsSubquery.where(
                    cb.equal(
                            affiliationRoot.get("patient").get("id"),
                            root.get("patient").get("id")
                    ),
                    cb.equal(
                            affiliationRoot.get("organizationId"),
                            currentOrganizationId
                    ),
                    affiliationRoot.get("type").in(
                            AffiliationType.WORKPLACE,
                            AffiliationType.EDUCATIONAL
                    )
            );

            return cb.exists(affiliationExistsSubquery);
        };
    }

    /**
     * Builds an ID-based access predicate for a raw organization ID field of Form129.
     */
    private Specification<Form129> organizationIdInScope(String formOrganizationIdField, ResolvedAccessScope scope) {
        return (root, query, cb) -> {
            if (scope == null || scope.isEmpty()) {
                return cb.disjunction();
            }

            if (scope.mode() == ResolvedAccessScope.Mode.ALL) {
                return cb.conjunction();
            }

            if (scope.mode() == ResolvedAccessScope.Mode.NONE) {
                return cb.disjunction();
            }

            assert query != null;
            Subquery<Long> organizationIdSubquery = query.subquery(Long.class);
            Root<Organization> organizationRoot = organizationIdSubquery.from(Organization.class);

            organizationIdSubquery.select(organizationRoot.get("id"));
            organizationIdSubquery.where(buildScopePredicate(organizationRoot, cb, scope));

            return root.get(formOrganizationIdField).in(organizationIdSubquery);
        };
    }

    /**
     * Converts resolved scope into a predicate over the Organization table.
     */
    private Predicate buildScopePredicate(
            Root<Organization> organizationRoot,
            CriteriaBuilder cb,
            ResolvedAccessScope scope
    ) {
        return switch (scope.mode()) {
            case ALL -> cb.conjunction();

            case STATE_NON_REPUBLICAN -> cb.and(
                    cb.equal(organizationRoot.get("stateCode"), scope.stateCode()),
                    cb.notEqual(organizationRoot.get("levelType"), LevelType.REPUBLICAN)
            );

            case DIRECT_ORGANIZATION -> cb.equal(
                    organizationRoot.get("uuid"),
                    scope.organizationUuid()
            );

            case NONE -> cb.disjunction();
        };
    }
}
