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

/**
 * Builds Form058-specific access filtering rules.
 *
 * <p>This class is intentionally responsible only for applying already resolved organization access
 * scope to Form058 fields.
 *
 * <p>Important design note:
 * Form058 does not have JPA relations like {@code senderOrganization} or {@code receiverOrganization}.
 * Instead, it stores only raw database IDs:
 * <ul>
 *     <li>{@code senderOrganizationId}</li>
 *     <li>{@code receiverOrganizationId}</li>
 * </ul>
 *
 * <p>Because of that, join-path based filtering cannot be used here.
 * Instead, this class applies access using ID-based subqueries against the Organization table.
 *
 * <p>This allows us to:
 * <ul>
 *     <li>keep the current entity model unchanged</li>
 *     <li>apply correct hierarchical access control</li>
 *     <li>avoid leaking data between unrelated organizations</li>
 * </ul>
 */
@Component
public class Form058AccessSpecification {

    /**
     * Builds the final Form058 access specification.
     *
     * <p>The resolved scope can be applied to:
     * <ul>
     *     <li>sender organization only</li>
     *     <li>receiver organization only</li>
     *     <li>both sender and receiver using OR semantics</li>
     * </ul>
     *
     * <p>If affiliation is provided, it is appended as an additional business filter.
     *
     * @param filterType determines which organization side should be checked
     * @param scope resolved access scope for the current user organization
     * @param affiliation optional additional business filter
     * @return final specification ready to be combined with other Form058 filters
     */
    public Specification<Form058> byScope(
            FilterType filterType,
            ResolvedAccessScope scope,
            Boolean affiliation
    ) {
        FilterType effectiveType = filterType == null ? FilterType.SENDER : filterType;

        Specification<Form058> accessSpec = switch (effectiveType) {
            case SENDER -> organizationIdInScope("senderOrganizationId", scope);
            case RECEIVER -> organizationIdInScope("receiverOrganizationId", scope);
            case ALL -> Specification.where(organizationIdInScope("senderOrganizationId", scope))
                    .or(organizationIdInScope("receiverOrganizationId", scope));
        };

        if (affiliation != null) {
            accessSpec = accessSpec.and(hasAffiliation(affiliation));
        }

        return accessSpec;
    }

    /**
     * Applies optional affiliation filter.
     */
    private Specification<Form058> hasAffiliation(Boolean affiliation) {
        return (root, query, cb) -> cb.equal(root.get("affiliation"), affiliation);
    }

    /**
     * Builds an ID-based access predicate for a raw organization ID field of Form058.
     *
     * <p>Generated SQL is conceptually similar to:
     * <pre>
     *     form058.sender_organization_id in (
     *         select o.id
     *         from organization o
     *         where [resolved scope predicate]
     *     )
     * </pre>
     *
     * <p>This is a good fit for the current model because Form058 stores only raw foreign-key-like
     * IDs and not Organization relations.
     *
     * @param formOrganizationIdField field name inside Form058
     * @param scope resolved access scope
     * @return specification for sender or receiver visibility
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
     *
     * <p>This method intentionally duplicates only the minimum scope logic needed for Form058
     * and keeps this class self-sufficient, so no other classes need to be changed.
     *
     * <p>Expected Organization fields:
     * <ul>
     *     <li>{@code id}</li>
     *     <li>{@code uuid}</li>
     *     <li>{@code stateCode}</li>
     *     <li>{@code levelType}</li>
     * </ul>
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