package uz.uzinfocom.app.platform.iam.application.organization.query.specification;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.platform.iam.application.organization.query.dto.request.OrganizationFilerRequest;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.scope.ResolvedOrganizationScope;
import uz.uzinfocom.app.platform.scope.jpa.OrganizationScopePredicateFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class OrganizationSpecification {

    private static final String ID = "id";

    private final OrganizationScopePredicateFactory scopePredicateFactory;

    /**
     * Every Organization list/search must stay within the caller's current
     * organization scope — scope is mandatory here, not just another optional
     * filter field. Organization is its own scope target, so the scope
     * predicate is applied directly against the entity's own id.
     */
    public Specification<Organization> byFilter(OrganizationFilerRequest request, ResolvedOrganizationScope scope) {
        Objects.requireNonNull(request, "OrganizationFilerRequest must not be null");
        Objects.requireNonNull(scope, "ResolvedOrganizationScope must not be null");

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(scopePredicateFactory.apply(root, cb, ID, scope));

            if (StringUtils.hasText(request.name())) {
                predicates.add(cb.like(
                        cb.lower(cb.coalesce(root.get("name"), "")),
                        like(request.name())
                ));
            }

            if (StringUtils.hasText(request.tin())) {
                predicates.add(cb.like(
                        cb.lower(cb.coalesce(root.get("tin"), "")),
                        like(request.tin())
                ));
            }

            if (StringUtils.hasText(request.regionCode())) {
                predicates.add(cb.equal(root.get("regionCode"), request.regionCode().trim()));
            }

            if (StringUtils.hasText(request.districtCode())) {
                predicates.add(cb.equal(root.get("districtCode"), request.districtCode().trim()));
            }

            if (request.active() != null) {
                predicates.add(cb.equal(root.get("active"), request.active()));
            }

            if (request.levelType() != null) {
                predicates.add(cb.equal(root.get("levelType"), request.levelType()));
            }

            if (request.medicalType() != null) {
                predicates.add(cb.equal(root.get("medicalType"), request.medicalType()));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static String like(String value) {
        return "%" + value.trim().toLowerCase(Locale.ROOT) + "%";
    }
}
