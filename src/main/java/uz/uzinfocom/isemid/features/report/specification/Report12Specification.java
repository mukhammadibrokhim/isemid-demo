package uz.uzinfocom.isemid.features.report.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.organization.models.enums.LevelType;
import uz.uzinfocom.isemid.features.report.model.Report12;
import uz.uzinfocom.isemid.features.user.models.User;

import java.util.ArrayList;

import static org.springframework.util.StringUtils.hasText;
import static uz.uzinfocom.isemid.commons.configs.security.util.SecurityUtils.isSuperAdmin;

public class Report12Specification {

    public static Specification<Report12> build(
            String regionName,
            User currentUser
    ) {
        return (root, query, cb) -> {

            var predicates = new ArrayList<Predicate>();

            // optional filter by region_name
            if (hasText(regionName)) {
                predicates.add(cb.like(
                        cb.lower(root.get("regionName")),
                        "%" + regionName.toLowerCase() + "%"
                ));
            }

            // =======================
            //   ORGANIZATION FILTER
            // =======================

            if (currentUser != null && currentUser.getCurrentOrganization() != null) {

                Organization org = currentUser.getCurrentOrganization();
                LevelType level = org.getLevelType();

                // SuperAdmin → all allowed
                if (!isSuperAdmin(currentUser)) {

                    Predicate orgPredicate = switch (level) {
                        case REPUBLICAN -> cb.conjunction(); // sees all
                        case REGIONAL ->
                                cb.equal(root.get("createdOrg").get("stateCode"), org.getStateCode());

                        case URBAN -> {
                            if ("UZ-TK".equalsIgnoreCase(org.getStateCode())) {
                                yield cb.equal(root.get("createdOrg").get("stateCode"), org.getStateCode());
                            } else {
                                yield cb.equal(root.get("createdOrg").get("cityCode"), org.getCityCode());
                            }
                        }

                        case DISTRICT, AREA, INTERDISTRICT, NOT_DEFINED ->
                                cb.equal(root.get("createdOrg").get("uuid"), org.getUuid());
                    };

                    predicates.add(orgPredicate);
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}