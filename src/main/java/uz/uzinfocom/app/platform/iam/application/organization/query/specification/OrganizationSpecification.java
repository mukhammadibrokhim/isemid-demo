package uz.uzinfocom.app.platform.iam.application.organization.query.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.platform.iam.application.organization.query.dto.OrganizationFilerRequest;
import uz.uzinfocom.app.platform.iam.domain.Organization;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class OrganizationSpecification {

    private OrganizationSpecification() {
    }

    public static Specification<Organization> byFilter(OrganizationFilerRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

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
