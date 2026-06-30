package uz.uzinfocom.app.platform.scope.jpa;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.scope.OrganizationScopeMode;
import uz.uzinfocom.app.platform.scope.ResolvedOrganizationScope;
import uz.uzinfocom.app.shared.exception.ScopeViolationException;

@Component
public class OrganizationScopePredicateFactory {

    /**
     * Generic organization scope predicate.
     * <p>
     * Rule:
     * - SANEPID_SERVICE: uses resolved hierarchy scope: ALL / REGION / DISTRICT / ORGANIZATION
     * - Non-SANEPID_SERVICE: always direct organization filter
     */
    public <T> Predicate apply(
            Root<T> root,
            CriteriaQuery<?> query,
            CriteriaBuilder cb,
            String organizationIdField,
            ResolvedOrganizationScope scope
    ) {
        validate(scope, organizationIdField);

        /*
         * Critical rule:
         * If selected organization is NOT SANEPID_SERVICE,
         * do not apply REGION/DISTRICT hierarchy.
         * It must see only records related to its own organization.
         */
        if (!scope.isSanepidService()) {
            return directOrganization(root, cb, organizationIdField, scope);
        }

        OrganizationScopeMode mode = scope.mode();

        return switch (mode) {
            case ALL -> cb.conjunction();

            case ORGANIZATION -> directOrganization(
                    root,
                    cb,
                    organizationIdField,
                    scope
            );

            case REGION -> byRegion(
                    root,
                    query,
                    cb,
                    organizationIdField,
                    scope
            );

            case DISTRICT -> byDistrict(
                    root,
                    query,
                    cb,
                    organizationIdField,
                    scope
            );
        };
    }

    private <T> Predicate directOrganization(
            Root<T> root,
            CriteriaBuilder cb,
            String organizationIdField,
            ResolvedOrganizationScope scope
    ) {
        if (scope.organizationId() == null) {
            throw new ScopeViolationException("organization.scope_violation");
        }

        return cb.equal(
                root.<Long>get(organizationIdField),
                scope.organizationId()
        );
    }

    private <T> Predicate byRegion(
            Root<T> root,
            CriteriaQuery<?> query,
            CriteriaBuilder cb,
            String organizationIdField,
            ResolvedOrganizationScope scope
    ) {
        if (!StringUtils.hasText(scope.regionCode())) {
            throw new ScopeViolationException("organization.scope_violation");
        }

        return root.<Long>get(organizationIdField).in(
                organizationIdsByRegion(query, cb, scope.regionCode())
        );
    }

    private <T> Predicate byDistrict(
            Root<T> root,
            CriteriaQuery<?> query,
            CriteriaBuilder cb,
            String organizationIdField,
            ResolvedOrganizationScope scope
    ) {
        if (!StringUtils.hasText(scope.districtCode())) {
            throw new ScopeViolationException("organization.scope_violation");
        }

        return root.<Long>get(organizationIdField).in(
                organizationIdsByDistrict(query, cb, scope.districtCode())
        );
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
                        cb.equal(organization.get("regionCode"), regionCode),
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
                        cb.equal(organization.get("districtCode"), districtCode),
                        cb.isFalse(organization.get("deleted"))
                );

        return subquery;
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