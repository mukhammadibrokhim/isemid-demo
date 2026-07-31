package uz.uzinfocom.app.modules.report.form2.manual.infrastructure.persistence.specification;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.report.form2.manual.application.query.dto.Form2ManualEntryFilterRequest;
import uz.uzinfocom.app.modules.report.form2.manual.domain.Form2ManualEntry;
import uz.uzinfocom.app.platform.scope.ResolvedOrganizationScope;
import uz.uzinfocom.app.platform.scope.jpa.OrganizationScopePredicateFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class Form2ManualEntrySpecification {

    private static final String ORGANIZATION_ID = "organizationId";

    private final OrganizationScopePredicateFactory scopePredicateFactory;

    /**
     * Every listing must stay within the caller's own organization scope —
     * a viewer sees their own organization's entries plus every entry
     * created by an organization within their scope (region/district/
     * republic), same rule as every other scoped table in this codebase.
     */
    public Specification<Form2ManualEntry> byFilter(
            Form2ManualEntryFilterRequest request, ResolvedOrganizationScope scope
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
