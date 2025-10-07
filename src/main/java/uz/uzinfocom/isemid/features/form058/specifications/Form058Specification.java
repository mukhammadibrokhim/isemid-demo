package uz.uzinfocom.isemid.features.form058.specifications;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import uz.uzinfocom.isemid.commons.utils.SpecificationUtils;
import uz.uzinfocom.isemid.features.form058.enums.FilterType;
import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.organization.models.enums.LevelType;
import uz.uzinfocom.isemid.features.organization.models.enums.MedicalType;
import uz.uzinfocom.isemid.features.patient.models.Affiliation;
import uz.uzinfocom.isemid.features.patient.models.Patient;
import uz.uzinfocom.isemid.features.patient.models.enums.AffiliationType;
import uz.uzinfocom.isemid.features.user.models.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.springframework.util.StringUtils.hasText;

public class Form058Specification {
    public static Specification<Form058> build(
            LocalDate fromDate,
            LocalDate toDate,
            String stateCode,
            String cityCode,
            Boolean affiliation,
            User currentUser,
            FilterType filterType
    ) {
        return (root, query, cb) -> {
            Objects.requireNonNull(query).distinct(true);
            List<Predicate> predicates = new ArrayList<>();

            SpecificationUtils.fromToDateFilter(fromDate, toDate, root, cb, predicates, "createdAt");

            if (hasText(stateCode)) {
                predicates.add(cb.equal(root.get("createdOrg").get("stateCode"), stateCode));
            }
            if (hasText(cityCode)) {
                predicates.add(cb.equal(root.get("createdOrg").get("cityCode"), cityCode));
            }

            var org = currentUser.getCurrentOrganization();
            if (org != null) {
                Long orgId = org.getId();
                UUID orgUuid = org.getUuid();
                LevelType levelType = org.getLevelType();
                MedicalType medicalType = org.getMedicalType();
                String orgStateCode = org.getStateCode();
                String orgCityCode = org.getCityCode();

                Predicate accessPredicate;

                if (Boolean.TRUE.equals(affiliation)) {
                    Join<Form058, Patient> patientJoin = root.join("patient", JoinType.LEFT);
                    Join<Patient, Affiliation> affJoin = patientJoin.join("affiliations", JoinType.LEFT);

                    Predicate orgMatch = cb.equal(affJoin.get("organizationId"), orgId);
                    Predicate typeMatch = affJoin.get("type").in(AffiliationType.WORKPLACE, AffiliationType.EDUCATIONAL);

                    accessPredicate = cb.and(orgMatch, typeMatch);
                }
                else if (medicalType == MedicalType.SANEPID_SERVICE) {
                    if (levelType == LevelType.REPUBLICAN) {
                        accessPredicate = cb.conjunction();
                    } else if (levelType == LevelType.REGIONAL) {
                        accessPredicate = SpecificationUtils.buildLevelPredicate(
                                root, cb, levelType, orgStateCode, orgCityCode
                        );
                    } else {
                        accessPredicate = cb.equal(root.get("receiverOrganizationId"), orgId);
                    }
                }
                else {
                    accessPredicate = (filterType == FilterType.RECEIVED)
                            ? cb.equal(root.get("receiverOrganizationId"), orgId)
                            : cb.equal(root.get("createdOrgUuid"), orgUuid);
                }

                predicates.add(accessPredicate);
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
