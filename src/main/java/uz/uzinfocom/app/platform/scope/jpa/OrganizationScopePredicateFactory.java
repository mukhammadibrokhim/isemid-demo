package uz.uzinfocom.app.platform.scope.jpa;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
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
            CriteriaQuery<?> query,
            CriteriaBuilder cb,
            String organizationIdField,
            ResolvedOrganizationScope scope
    ) {
        validate(scope, organizationIdField);

        if (!scope.isSanepidService()) {
            return directOrganization(
                    root,
                    cb,
                    organizationIdField,
                    scope.organizationId()
            );
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