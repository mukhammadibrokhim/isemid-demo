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
import uz.uzinfocom.isemid.features.user.models.User;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
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

                if (medicalType == MedicalType.SANEPID_SERVICE) {
                    accessPredicate = switch (levelType) {
                        case REPUBLICAN -> cb.conjunction(); // Respublika → hamma ko‘rinadi
                        case REGIONAL, URBAN -> cb.equal(root.get("createdOrg").get("stateCode"), orgStateCode);
                        case DISTRICT, AREA, INTERDISTRICT, NOT_DEFINED ->
                                cb.equal(root.get("createdOrg").get("cityCode"), orgCityCode);
                    };
                } else {
                    if (filterType == FilterType.RECEIVED) {
                        Predicate receiverMatch = cb.equal(root.get("receiverOrganizationId"), orgId);

                        Join<Form058, Patient> patientJoin = root.join("patient", JoinType.LEFT);
                        Join<Patient, Affiliation> affiliationJoin = patientJoin.join("affiliations", JoinType.LEFT);

                        Predicate orgMatch = cb.equal(affiliationJoin.get("organizationId"), orgId);
                        Predicate typeMatch = affiliationJoin.get("type")
                                .in(AffiliationType.WORKPLACE, AffiliationType.EDUCATIONAL);
                        Predicate affiliationValid = cb.and(orgMatch, typeMatch);

                        if (Boolean.TRUE.equals(affiliation)) {
                            accessPredicate = affiliationValid;
                        } else {
                            accessPredicate = receiverMatch;
                        }
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

            // --- Date filter ---
            if (fromDate != null && toDate != null) {
                predicates.add(cb.between(
                        root.get("createdAt"),
                        Timestamp.valueOf(fromDate.atStartOfDay()),
                        Timestamp.valueOf(toDate.atTime(LocalTime.MAX))
                ));
            } else if (fromDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("createdAt"),
                        Timestamp.valueOf(fromDate.atStartOfDay())
                ));
            } else if (toDate != null) {
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("createdAt"),
                        Timestamp.valueOf(toDate.atTime(LocalTime.MAX))
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
