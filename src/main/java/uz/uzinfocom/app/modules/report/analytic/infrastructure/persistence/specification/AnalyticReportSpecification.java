package uz.uzinfocom.app.modules.report.analytic.infrastructure.persistence.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.report.analytic.application.query.dto.AnalyticReportFilterRequest;
import uz.uzinfocom.app.modules.report.analytic.domain.AnalyticReport;
import uz.uzinfocom.app.modules.report.analytic.domain.AnalyticReportStatus;
import uz.uzinfocom.app.platform.security.auth.AdminAccessGuard;
import uz.uzinfocom.app.platform.security.context.CurrentUserProvider;
import uz.uzinfocom.app.orchestration.scope.ResolvedOrganizationScope;
import uz.uzinfocom.app.orchestration.scope.jpa.OrganizationScopePredicateFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class AnalyticReportSpecification {

    private static final String ORGANIZATION_ID = "organizationId";

    private final OrganizationScopePredicateFactory scopePredicateFactory;
    private final AdminAccessGuard adminAccessGuard;
    private final CurrentUserProvider currentUserProvider;

    /**
     * Every listing must stay within the caller's own organization scope —
     * their own organization's reports plus every report created by an
     * organization within their scope (region/district/republic), same rule
     * as every other scoped table in this codebase. On top of that a
     * {@code TEMPLATE} (draft) is private to the user who created it; only
     * {@code FINAL} reports are shared across the scope. Admins see drafts too.
     */
    public Specification<AnalyticReport> byFilter(AnalyticReportFilterRequest request, ResolvedOrganizationScope scope) {
        Objects.requireNonNull(scope, "ResolvedOrganizationScope must not be null");

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(scopePredicateFactory.apply(root, cb, ORGANIZATION_ID, scope));
            predicates.add(draftVisibility(root, cb));

            if (request != null && request.status() != null) {
                predicates.add(cb.equal(root.get("status"), request.status()));
            }

            if (request != null && request.from() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("toDate"), request.from()));
            }

            if (request != null && request.to() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("fromDate"), request.to()));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    /**
     * Detail lookup under the same scope rule as {@link #byFilter} - a report
     * outside the caller's scope is indistinguishable from a missing one.
     */
    public Specification<AnalyticReport> visibleById(Long id, ResolvedOrganizationScope scope) {
        Objects.requireNonNull(scope, "ResolvedOrganizationScope must not be null");

        return (root, query, cb) -> cb.and(
                scopePredicateFactory.apply(root, cb, ORGANIZATION_ID, scope),
                draftVisibility(root, cb),
                cb.equal(root.get("id"), id)
        );
    }

    private Predicate draftVisibility(Root<AnalyticReport> root, CriteriaBuilder cb) {
        if (adminAccessGuard.isAdmin()) {
            return cb.conjunction();
        }

        Predicate isFinal = cb.equal(root.get("status"), AnalyticReportStatus.FINAL);
        Long userId = currentUserProvider.userIdOrNull();
        if (userId == null) {
            return isFinal;
        }
        return cb.or(isFinal, cb.equal(root.get("createdBy"), userId));
    }
}
