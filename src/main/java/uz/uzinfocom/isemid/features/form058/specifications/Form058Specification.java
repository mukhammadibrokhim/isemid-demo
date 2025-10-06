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
//        return (root, query, cb) -> {
//            Objects.requireNonNull(query).distinct(true);
//            List<Predicate> predicates = new ArrayList<>();
//
//            if (currentUser.getCurrentOrganization() != null) {
//                var org = currentUser.getCurrentOrganization();
//
//                Long orgId = org.getId();
//                UUID orgUuid = org.getUuid();
//                LevelType levelType = org.getLevelType();
//                MedicalType medicalType = org.getMedicalType();
//                String orgStateCode = org.getStateCode();
//                String orgCityCode = org.getCityCode();
//
//                Predicate accessPredicate;
//
//                if (Boolean.TRUE.equals(affiliation)) {
//                    Join<Form058, Patient> patientJoin = root.join("patient", JoinType.LEFT);
//                    Join<Patient, Affiliation> affiliationJoin = patientJoin.join("affiliations", JoinType.LEFT);
//
//                    Predicate orgMatch = cb.equal(affiliationJoin.get("organizationId"), orgId);
//                    Predicate typeMatch = affiliationJoin.get("type")
//                            .in(AffiliationType.WORKPLACE, AffiliationType.EDUCATIONAL);
//
//                    accessPredicate = cb.and(orgMatch, typeMatch);
//                }
//
//                else if (medicalType == MedicalType.SANEPID_SERVICE) {
//                    Predicate levelPredicate = SpecificationUtils.buildLevelPredicate(
//                            root, cb, levelType, orgStateCode, orgCityCode
//                    );
//
//                    if (filterType == FilterType.RECEIVED) {
//                        Predicate receiverPredicate = cb.equal(root.get("receiverOrganizationId"), orgId);
//                        accessPredicate = cb.and(levelPredicate, receiverPredicate);
//                    } else if (filterType == FilterType.ALL) {
//                        Predicate createdPredicate = cb.equal(root.get("createdOrgUuid"), orgUuid);
//                        accessPredicate = cb.and(levelPredicate, createdPredicate);
//                    } else {
//                        accessPredicate = levelPredicate;
//                    }
//                }
//
//                else {
//                    if (filterType == FilterType.RECEIVED) {
//                        accessPredicate = cb.equal(root.get("receiverOrganizationId"), orgId);
//                    } else {
//                        accessPredicate = cb.equal(root.get("createdOrgUuid"), orgUuid);
//                    }
//                }
//
//                predicates.add(accessPredicate);
//            }
//
//            if (hasText(stateCode)) {
//                predicates.add(cb.equal(root.get("createdOrg").get("stateCode"), stateCode));
//            }
//            if (hasText(cityCode)) {
//                predicates.add(cb.equal(root.get("createdOrg").get("cityCode"), cityCode));
//            }
//
//            SpecificationUtils.fromToDateFilter(fromDate, toDate, root, cb, predicates, "createdAt");
//            return cb.and(predicates.toArray(new Predicate[0]));
//        };
        return (root, query, cb) -> {
            Objects.requireNonNull(query).distinct(true);
            List<Predicate> predicates = new ArrayList<>();

            SpecificationUtils.fromToDateFilter(fromDate, toDate, root, cb, predicates, "createdAt");

            if (stateCode != null && !stateCode.isBlank()) {
                predicates.add(cb.equal(root.get("createdOrg").get("stateCode"), stateCode));
            }
            if (cityCode != null && !cityCode.isBlank()) {
                predicates.add(cb.equal(root.get("createdOrg").get("cityCode"), cityCode));
            }

            var userOrg = currentUser.getCurrentOrganization();
            if (userOrg != null) {
                UUID orgUuid = userOrg.getUuid();
                Long orgId = userOrg.getId();
                LevelType levelType = userOrg.getLevelType();
                MedicalType medicalType = userOrg.getMedicalType();
                String orgStateCode = userOrg.getStateCode();
                String orgCityCode = userOrg.getCityCode();

                Predicate accessPredicate;

                if (medicalType == MedicalType.SANEPID_SERVICE) {
                    if (levelType == LevelType.REPUBLICAN) {
                        accessPredicate = cb.conjunction();
                    } else {
                        Predicate levelPredicate = SpecificationUtils.buildLevelPredicate(
                                root, cb, levelType, orgStateCode, orgCityCode
                        );

                        if (filterType == FilterType.RECEIVED) {
                            Predicate receiverMatch = cb.equal(root.get("receiverOrganizationId"), orgId);
                            accessPredicate = cb.and(levelPredicate, receiverMatch);
                        } else if (filterType == FilterType.ALL) {
                            Predicate createdMatch = cb.equal(root.get("createdOrgUuid"), orgUuid);
                            accessPredicate = cb.and(levelPredicate, createdMatch);
                        } else {
                            accessPredicate = levelPredicate;
                        }
                    }
                } else {
                    if (filterType == FilterType.RECEIVED) {
                        accessPredicate = cb.equal(root.get("receiverOrganizationId"), orgId);
                    } else {
                        accessPredicate = cb.equal(root.get("createdOrgUuid"), orgUuid);
                    }
                }

                predicates.add(accessPredicate);
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
