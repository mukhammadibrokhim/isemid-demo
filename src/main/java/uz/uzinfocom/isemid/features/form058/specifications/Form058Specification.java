package uz.uzinfocom.isemid.features.form058.specifications;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import uz.uzinfocom.isemid.features.form058.enums.FilterType;
import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.organization.models.enums.LevelType;
import uz.uzinfocom.isemid.features.organization.models.enums.MedicalType;
import uz.uzinfocom.isemid.features.patient.models.Affiliation;
import uz.uzinfocom.isemid.features.patient.models.Patient;
import uz.uzinfocom.isemid.features.patient.models.enums.AffiliationType;
import uz.uzinfocom.isemid.features.report.service.impls.LocationReportServiceImpl;
import uz.uzinfocom.isemid.features.user.models.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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
            List<Predicate> predicates = new ArrayList<>();

            if (currentUser.getCurrentOrganization() != null) {
                var org = currentUser.getCurrentOrganization();
                Long orgId = org.getId();
                UUID orgUuid = org.getUuid();
                LevelType levelType = org.getLevelType();
                MedicalType medicalType = org.getMedicalType();
                String orgStateCode = org.getStateCode();
                String orgCityCode = org.getCityCode();

                Predicate accessPredicate;

                if (Boolean.TRUE.equals(affiliation)) {
                    Join<Form058, Patient> patientJoin = root.join("patient", JoinType.LEFT);
                    Join<Patient, Affiliation> affiliationJoin = patientJoin.join("affiliations", JoinType.LEFT);

                    Predicate orgMatch = cb.equal(affiliationJoin.get("organizationId"), orgId);
                    Predicate typeMatch = affiliationJoin.get("type")
                            .in(AffiliationType.WORKPLACE, AffiliationType.EDUCATIONAL);

                    accessPredicate = cb.and(orgMatch, typeMatch);
                } else if (medicalType == MedicalType.SANEPID_SERVICE) {
                    Predicate receiverMatch = cb.equal(root.get("receiverOrganizationId"), orgId);

                    Predicate levelPredicate = switch (levelType) {
                        case REPUBLICAN -> cb.conjunction();
                        case REGIONAL -> cb.equal(root.get("createdOrg").get("stateCode"), orgStateCode);
                        case URBAN -> {
                            if ("UZ-TO".equals(orgStateCode)) {
                                yield cb.equal(root.get("createdOrg").get("stateCode"), orgStateCode);
                            } else {
                                yield cb.equal(root.get("createdOrg").get("cityCode"), orgCityCode);
                            }
                        }
                        case DISTRICT, AREA, INTERDISTRICT, NOT_DEFINED ->
                                cb.equal(root.get("createdOrg").get("cityCode"), orgCityCode);
                    };

                    accessPredicate = cb.or(receiverMatch, levelPredicate);
                } else {
                    if (filterType == FilterType.RECEIVED) {
                        accessPredicate = cb.equal(root.get("receiverOrganizationId"), orgId);
                    } else {
                        accessPredicate = cb.equal(root.get("createdOrgUuid"), orgUuid);
                    }
                }

                predicates.add(accessPredicate);
            }

            if (hasText(stateCode)) {
                predicates.add(cb.equal(root.get("createdOrg").get("stateCode"), stateCode));
            }
            if (hasText(cityCode)) {
                predicates.add(cb.equal(root.get("createdOrg").get("cityCode"), cityCode));
            }

            LocationReportServiceImpl.fromToFilter(fromDate, toDate, root, cb, predicates);

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}