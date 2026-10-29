package uz.uzinfocom.app.modules.report.form31.infrastructure.persistence.specification;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.report.form31.application.query.dto.Form31EntryFilterRequest;
import uz.uzinfocom.app.modules.report.form31.domain.Form31Entry;
import uz.uzinfocom.app.orchestration.scope.ResolvedOrganizationScope;
import uz.uzinfocom.app.orchestration.scope.jpa.OrganizationScopePredicateFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class Form31EntrySpecification {

    private static final String ORGANIZATION_ID = "organizationId";

    private final OrganizationScopePredicateFactory scopePredicateFactory;

    /**
     * Every listing must stay within the caller's own organization scope —
     * a viewer sees their own organization's entries plus every entry
     * created by an organization within their scope (region/district/
     * republic), same rule as every other scoped table in this codebase.
     */
    public Specification<Form31Entry> byFilter(
            Form31EntryFilterRequest request, ResolvedOrganizationScope scope
    ) {
        Objects.requireNonNull(scope, "ResolvedOrganizationScope must not be null");

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(scopePredicateFactory.apply(root, cb, ORGANIZATION_ID, scope));

            if (request != null && request.from() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("toDate"), request.from()));
            }

            if (request != null && request.to() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("fromDate"), request.to()));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
