package uz.uzinfocom.isemid.features.form058.specifications;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import uz.uzinfocom.isemid.features.form058.enums.FilterType;
import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.patient.models.Patient;
import uz.uzinfocom.isemid.features.user.models.User;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Form058Specification {
    public static Specification<Form058> getFilteredSpecification(
            LocalDate fromDate,
            LocalDate toDate,
            User currentUser,
            FilterType filterType
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (currentUser.getCurrentOrganization() != null) {
                if (filterType == FilterType.RECEIVED) {
                    predicates.add(criteriaBuilder.equal(
                            root.get("receiverOrganizationId"), currentUser.getCurrentOrganization().getId()));
                } else {
                    Expression<UUID> createdOrgUuid = root.get("createdOrgUuid");

                    Join<Form058, Patient> patientJoin = root.join("patient", JoinType.LEFT);

                    Expression<UUID> workplaceOrgUuid = patientJoin.get("workPlaceOrgId");

                    Predicate createdMatches = criteriaBuilder.equal(createdOrgUuid, currentUser.getCurrentOrganization().getUuid());
                    Predicate workplaceMatches = criteriaBuilder.equal(workplaceOrgUuid, currentUser.getCurrentOrganization().getId());
                    Predicate eitherMatches = criteriaBuilder.or(createdMatches, workplaceMatches);

                    predicates.add(eitherMatches);
                }
            }

            if (fromDate != null && toDate != null) {
                predicates.add(criteriaBuilder.between(
                        root.get("createdAt"),
                        Timestamp.valueOf(fromDate.atStartOfDay()),
                        Timestamp.valueOf(toDate.atTime(LocalTime.MAX))
                ));
            } else if (fromDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("createdAt"),
                        Timestamp.valueOf(fromDate.atStartOfDay())
                ));
            } else if (toDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("createdAt"),
                        Timestamp.valueOf(toDate.atTime(LocalTime.MAX))
                ));
            }


            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
