package uz.uzinfocom.app.modules.iam.application.organization.query.specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.modules.iam.application.organization.query.dto.request.OrganizationFilerRequest;
import uz.uzinfocom.app.modules.iam.application.organization.query.dto.request.OrganizationLookupRequest;
import uz.uzinfocom.app.modules.iam.domain.Organization;
import uz.uzinfocom.app.modules.iam.domain.enums.MedicalType;
import uz.uzinfocom.app.modules.iam.domain.enums.ServiceType;
import uz.uzinfocom.app.orchestration.scope.ResolvedOrganizationScope;
import uz.uzinfocom.app.orchestration.scope.jpa.OrganizationScopePredicateFactory;

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

    /**
     * Lookup search (dropdowns/typeahead) is unscoped by design — unlike
     * {@link #byFilter}, callers pick from the whole organization tree.
     */
    public Specification<Organization> byLookupFilter(OrganizationLookupRequest request) {
        Objects.requireNonNull(request, "OrganizationLookupRequest must not be null");

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (request.id() != null) {
                predicates.add(cb.equal(root.get(ID), request.id()));
            }

            String search = request.normalizedSearch();
            if (StringUtils.hasText(search)) {
                String pattern = like(search);
                predicates.add(cb.or(
                        cb.like(cb.lower(cb.coalesce(root.get("name"), "")), pattern),
                        cb.like(cb.lower(cb.coalesce(root.get("tin"), "")), pattern),
                        cb.like(cb.lower(cb.coalesce(root.get("phone"), "")), pattern),
                        cb.like(cb.lower(cb.coalesce(root.get("regionCode"), "")), pattern),
                        cb.like(cb.lower(cb.coalesce(root.get("districtCode"), "")), pattern)
                ));
            }

            if (request.levelType() != null) {
                predicates.add(cb.equal(root.get("levelType"), request.levelType()));
            }

            List<MedicalType> medicalTypes = request.normalizedMedicalTypes();
            if (!medicalTypes.isEmpty()) {
                predicates.add(root.<MedicalType>get("medicalType").in(medicalTypes));
            }

            List<ServiceType> serviceTypes = request.normalizedServiceTypes();
            if (!serviceTypes.isEmpty()) {
                Join<Organization, ServiceType> serviceTypeJoin = root.join("serviceTypes", JoinType.INNER);
                predicates.add(serviceTypeJoin.in(serviceTypes));
                query.distinct(true);
            }

            if (request.active() != null) {
                predicates.add(cb.equal(root.get("active"), request.active()));
            }

            if (StringUtils.hasText(request.regionCode())) {
                predicates.add(cb.equal(root.get("regionCode"), request.regionCode().trim()));
            }

            if (StringUtils.hasText(request.districtCode())) {
                predicates.add(cb.equal(root.get("districtCode"), request.districtCode().trim()));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static String like(String value) {
        return "%" + value.trim().toLowerCase(Locale.ROOT) + "%";
    }
}
