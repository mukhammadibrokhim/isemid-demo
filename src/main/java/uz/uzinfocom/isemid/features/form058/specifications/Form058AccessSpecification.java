package uz.uzinfocom.isemid.features.form058.specifications;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import uz.uzinfocom.isemid.commons.security.scope.ResolvedAccessScope;
import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.form058.models.enums.FilterType;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.organization.models.enums.LevelType;
import uz.uzinfocom.isemid.features.patient.models.Affiliation;
import uz.uzinfocom.isemid.features.patient.models.enums.AffiliationType;

/**
 * Builds Form058-specific access filtering rules.
 *
 * <p>This class applies already resolved organization access scope to Form058 sender/receiver
 * organization IDs and optionally applies patient affiliation filtering.
 *
 * <p>Important:
 * Form058 does not store direct Organization relations for sender/receiver.
 * It stores only raw IDs:
 * <ul>
 *     <li>senderOrganizationId</li>
 *     <li>receiverOrganizationId</li>
 * </ul>
 *
 * <p>Also, affiliation does not belong to Form058 itself.
 * It belongs to Form058 -> patient -> affiliations.
 */
@Component
public class Form058AccessSpecification {

    /**
     * Builds the final Form058 access specification.
     *
     * @param filterType determines which organization side should be checked
     * @param scope resolved access scope for the current user organization
     * @param currentOrganizationId current user's organization database ID
     * @param affiliation optional affiliation filter flag
     * @return final specification ready to be combined with other filters
     */
    public Specification<Form058> byScope(
            FilterType filterType,
            ResolvedAccessScope scope,
            Long currentOrganizationId,
            Boolean affiliation
    ) {
        FilterType effectiveType = filterType == null ? FilterType.SENDER : filterType;

        Specification<Form058> accessSpec = switch (effectiveType) {
            case SENDER -> organizationIdInScope("senderOrganizationId", scope);
            case RECEIVER -> organizationIdInScope("receiverOrganizationId", scope);
            case ALL -> Specification.where(organizationIdInScope("senderOrganizationId", scope))
                    .or(organizationIdInScope("receiverOrganizationId", scope));
        };

        if (Boolean.TRUE.equals(affiliation)) {
            accessSpec = accessSpec.and(hasAffiliation(currentOrganizationId));
        }

        return accessSpec;
    }

    /**
     * Applies affiliation filter through Form058 -> patient -> affiliations.
     *
     * <p>Business rule preserved from the old implementation:
     * patient must have affiliation for the current organization and
     * affiliation type must be WORKPLACE or EDUCATIONAL.
     */
    private Specification<Form058> hasAffiliation(Long currentOrganizationId) {
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
     * Builds an ID-based access predicate for a raw organization ID field of Form058.
     *
     * <p>Generated SQL is conceptually similar to:
     * form058.sender_organization_id in (
     *     select o.id
     *     from organization o
     *     where [resolved scope predicate]
     * )
     */
    private Specification<Form058> organizationIdInScope(String formOrganizationIdField, ResolvedAccessScope scope) {
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
     * Converts resolved scope into a predicate over Organization table.
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