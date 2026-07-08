package uz.uzinfocom.app.platform.scope.jpa;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.scope.OrganizationScopeMode;
import uz.uzinfocom.app.platform.scope.ResolvedOrganizationScope;
import uz.uzinfocom.app.shared.exception.ScopeViolationException;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OrganizationScopePredicateFactory {

    private final OrganizationScopeOrganizationIdResolver organizationIdResolver;

    /**
     * Generic organization scope predicate.
     * <p>
     * Rules:
     * - Non-SANEPID_SERVICE -> only own organization
     * - SANEPID_SERVICE + ALL -> no organization restriction
     * - SANEPID_SERVICE + REGION -> organizations by stateCode
     * - SANEPID_SERVICE + DISTRICT -> organizations by cityCode
     * - SANEPID_SERVICE + ORGANIZATION -> only own organization
     */
    public <T> Predicate apply(
            Root<T> root,
            CriteriaBuilder cb,
            String organizationIdField,
            ResolvedOrganizationScope scope
    ) {
        validate(scope, organizationIdField);

        if (!scope.isSanepidService()) {
            return directOrganization(root, cb, organizationIdField, scope.organizationId());
        }

        OrganizationScopeMode mode = scope.mode();

        return switch (mode) {
            case ALL -> cb.conjunction();

            case ORGANIZATION -> directOrganization(
                    root,
                    cb,
                    organizationIdField,
                    scope.organizationId()
            );

            case REGION, DISTRICT -> byResolvedOrganizationIds(
                    root,
                    cb,
                    organizationIdField,
                    organizationIdResolver.resolveScopeOrganizationIds(
                            mode,
                            scope.regionCode(),
                            scope.districtCode()
                    )
            );
        };
    }

    /**
     * Same scope rules as {@link #apply}, but for entities that relate to
     * Organization through a many-to-many collection (e.g. User.organizations)
     * instead of a single scalar FK column. Applied as a correlated EXISTS
     * subquery joining the collection, filtered by a materialized, cached
     * organization id list rather than a live region/district column
     * comparison — see {@link OrganizationScopeOrganizationIdResolver} for
     * why a literal id list is preferred over a live-joined filter.
     */
    public <T> Predicate applyToCollection(
            Root<T> root,
            CriteriaQuery<?> query,
            CriteriaBuilder cb,
            String collectionField,
            ResolvedOrganizationScope scope
    ) {
        validate(scope, collectionField);

        if (!scope.isSanepidService()) {
            return existsOrganization(root, query, cb, collectionField, List.of(requireOrganizationId(scope)));
        }

        OrganizationScopeMode mode = scope.mode();

        return switch (mode) {
            case ALL -> cb.conjunction();

            case ORGANIZATION -> existsOrganization(
                    root, query, cb, collectionField, List.of(requireOrganizationId(scope))
            );

            case REGION, DISTRICT -> existsOrganization(
                    root,
                    query,
                    cb,
                    collectionField,
                    organizationIdResolver.resolveScopeOrganizationIds(mode, scope.regionCode(), scope.districtCode())
            );
        };
    }

    private <T> Predicate existsOrganization(
            Root<T> root,
            CriteriaQuery<?> query,
            CriteriaBuilder cb,
            String collectionField,
            List<Long> organizationIds
    ) {
        if (organizationIds == null || organizationIds.isEmpty()) {
            return cb.disjunction();
        }

        Subquery<Long> subquery = query.subquery(Long.class);
        Root<T> correlatedRoot = subquery.correlate(root);
        Join<T, Organization> organization = correlatedRoot.join(collectionField, JoinType.INNER);

        Predicate idPredicate = organizationIds.size() == 1
                ? cb.equal(organization.<Long>get("id"), organizationIds.getFirst())
                : organization.<Long>get("id").in(organizationIds);

        subquery.select(cb.literal(1L)).where(idPredicate);

        return cb.exists(subquery);
    }

    private Long requireOrganizationId(ResolvedOrganizationScope scope) {
        if (scope.organizationId() == null) {
            throw new ScopeViolationException("organization.scope_violation");
        }

        return scope.organizationId();
    }

    private <T> Predicate directOrganization(
            Root<T> root,
            CriteriaBuilder cb,
            String organizationIdField,
            Long organizationId
    ) {
        if (organizationId == null) {
            throw new ScopeViolationException("organization.scope_violation");
        }

        return cb.equal(
                root.<Long>get(organizationIdField),
                organizationId
        );
    }

    private <T> Predicate byResolvedOrganizationIds(
            Root<T> root,
            CriteriaBuilder cb,
            String organizationIdField,
            List<Long> organizationIds
    ) {
        if (organizationIds == null || organizationIds.isEmpty()) {
            return cb.disjunction();
        }

        if (organizationIds.size() == 1) {
            return cb.equal(
                    root.<Long>get(organizationIdField),
                    organizationIds.getFirst()
            );
        }

        return root.<Long>get(organizationIdField).in(organizationIds);
    }

    private void validate(
            ResolvedOrganizationScope scope,
            String organizationIdField
    ) {
        if (scope == null || !StringUtils.hasText(organizationIdField)) {
            throw new ScopeViolationException("organization.scope_violation");
        }

        if (scope.mode() == null) {
            throw new ScopeViolationException("organization.scope_violation");
        }

        if (scope.medicalType() == null) {
            throw new ScopeViolationException("organization.scope_violation");
        }
    }
}