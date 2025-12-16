package uz.uzinfocom.isemid.features.report.specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import uz.uzinfocom.isemid.commons.utils.SpecificationUtils;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.organization.models.enums.LevelType;
import uz.uzinfocom.isemid.features.organization.models.enums.MedicalType;
import uz.uzinfocom.isemid.features.report.model.FormReport31;
import uz.uzinfocom.isemid.features.report.model.FormReport7;
import uz.uzinfocom.isemid.features.user.models.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.springframework.util.StringUtils.hasText;
import static uz.uzinfocom.isemid.commons.configs.security.util.SecurityUtils.isSuperAdmin;

public class FormReport31Specification {
    public static Specification<FormReport31> build(
            LocalDate fromDate,
            LocalDate toDate,
            String stateCode,
            String cityCode,
            User currentUser
    ) {
        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            // DATE FILTER
            SpecificationUtils.fromToDateFilter(fromDate, toDate, root, cb, predicates, "createdAt");

            // ===== JOIN — faqat 1 marta =====
            Join<FormReport7, Organization> createdOrg = root.join("createdOrg", JoinType.LEFT);

            // ===== STATE / CITY FILTERS =====
            if (hasText(stateCode)) {
                predicates.add(cb.equal(createdOrg.get("stateCode"), stateCode));
            }

            if (hasText(cityCode)) {
                predicates.add(cb.equal(createdOrg.get("cityCode"), cityCode));
            }

            // ===== ACCESS CONTROL =====
            if (currentUser != null && currentUser.getCurrentOrganization() != null) {

                Organization org = currentUser.getCurrentOrganization();

                // SUPER ADMIN — cheklov yo‘q
                if (isSuperAdmin(currentUser)) {
                    return cb.and(predicates.toArray(new Predicate[0]));
                }

                Long orgId = org.getId();
                UUID orgUuid = org.getUuid();
                LevelType levelType = org.getLevelType();
                MedicalType medicalType = org.getMedicalType();
                String orgStateCode = org.getStateCode();
                String orgCityCode = org.getCityCode();

                Predicate accessPredicate;

                // ===== SANEPID → LEVEL FILTER CREATED_ORG BILAN =====
                if (medicalType == MedicalType.SANEPID_SERVICE) {

                    accessPredicate = SpecificationUtils.buildLevelPredicate(
                            root,                     // <<== to‘g‘ri JOYN
                            cb,
                            levelType,
                            orgStateCode,
                            orgCityCode,
                            orgId
                    );
                }

                // ===== OTHER ORGANIZATIONS =====
                else {
                    accessPredicate = cb.equal(createdOrg.get("uuid"), orgUuid);
                }

                predicates.add(accessPredicate);
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
