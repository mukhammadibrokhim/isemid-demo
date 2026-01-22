package uz.uzinfocom.isemid.features.form058.specifications;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.hibernate.boot.model.naming.Identifier;
import org.springframework.data.jpa.domain.Specification;
import uz.uzinfocom.isemid.commons.utils.SpecificationUtils;
import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.form058.models.enums.FilterType;
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
import static uz.uzinfocom.isemid.commons.configs.security.util.SecurityUtils.isSuperAdmin;

public class Form058Specification {
    public static Specification<Form058> build(
            LocalDate fromDate,
            LocalDate toDate,
            String stateCode,
            String cityCode,
            Boolean affiliation,
            String mkb10Code,
            Long organizationId,
            String patientDocValue,
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
            if (hasText(mkb10Code)) {
                predicates.add(cb.equal(root.get("finalMkb10Code"), mkb10Code));
            }
            if (hasText(patientDocValue)) {
                Join<Form058, Patient> patientJoin = root.join("patient", JoinType.LEFT);
                Join<Patient, Identifier> idJoin = patientJoin.join("identifiers", JoinType.LEFT);

                predicates.add(cb.equal(idJoin.get("value"), patientDocValue));
            }

            if (organizationId != null && filterType != null) {
                switch (filterType) {
                    case ALL -> predicates.add(cb.equal(root.get("senderOrganizationId"), organizationId));
                    case RECEIVED -> predicates.add(cb.equal(root.get("receiverOrganizationId"), organizationId));
                }
            }

            var org = currentUser.getCurrentOrganization();
            if (org != null) {

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

                if (Boolean.TRUE.equals(affiliation)) {

                    if (medicalType == MedicalType.SANEPID_SERVICE) {

                        Join<Form058, Patient> patientJoin = root.join("patient", JoinType.LEFT);
                        Join<Patient, Affiliation> affJoin = patientJoin.join("affiliations", JoinType.LEFT);

                        Predicate orgMatch = cb.equal(affJoin.get("organizationId"), orgId);
                        Predicate typeMatch = affJoin.get("type")
                                .in(AffiliationType.WORKPLACE, AffiliationType.EDUCATIONAL);

                        accessPredicate = cb.and(orgMatch, typeMatch);
                    } else {
                        accessPredicate = cb.disjunction();
                    }
                }

                else if (medicalType == MedicalType.SANEPID_SERVICE) {

                    accessPredicate = SpecificationUtils.buildLevelPredicate(
                            root, cb, levelType, orgStateCode, orgCityCode, orgId
                    );
                }

                else {

                    Predicate createdByOrg = cb.equal(root.get("createdOrgUuid"), orgUuid);
                    Predicate receivedByOrg = cb.equal(root.get("receiverOrganizationId"), orgId);

                    if (filterType == FilterType.ALL) {
                        accessPredicate = createdByOrg;
                    } else if (filterType == FilterType.RECEIVED) {
                        accessPredicate = receivedByOrg;
                    } else {
                        accessPredicate = cb.or(createdByOrg, receivedByOrg);
                    }
                }

                predicates.add(accessPredicate);
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Form058> build(
            final String nnuzb
    ) {
        return (root, query, cb) -> {
            Objects.requireNonNull(query).distinct(true);
            List<Predicate> predicates = new ArrayList<>();

            if (hasText(nnuzb)) {
                Join<Form058, Patient> patientJoin = root.join("patient", JoinType.LEFT);
                Join<Patient, Identifier> idJoin = patientJoin.join("identifiers", JoinType.LEFT);

                predicates.add(cb.equal(idJoin.get("value"), nnuzb));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Form058> buildStats(
            List<String> regionCodes,
            LocalDate fromDate,
            LocalDate toDate,
            List<String> mkb10Codes,
            boolean confirmed
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (regionCodes != null && !regionCodes.isEmpty()) {
                predicates.add(root.get("createdOrg").get("stateCode").in(regionCodes));
            }

            if (confirmed)
                predicates.add(cb.equal(root.get("status"), "APPROVED"));
            else
                predicates.add(cb.notEqual(root.get("status"), "APPROVED"));

            SpecificationUtils.fromToDateFilter(fromDate, toDate, root, cb, predicates, "createdAt");

            if (mkb10Codes != null && !mkb10Codes.isEmpty()) {
                predicates.add(root.get("finalMkb10Code").in(mkb10Codes));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
