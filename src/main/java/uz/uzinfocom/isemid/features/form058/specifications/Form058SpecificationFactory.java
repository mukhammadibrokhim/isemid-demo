package uz.uzinfocom.isemid.features.form058.specifications;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import uz.uzinfocom.isemid.features.form058.enums.FilterType;
import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.organization.models.enums.LevelType;
import uz.uzinfocom.isemid.features.organization.models.enums.MedicalType;
import uz.uzinfocom.isemid.features.user.models.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class Form058SpecificationFactory {
    public Specification<Form058> build(
            LocalDate fromDate,
            LocalDate toDate,
            String stateCode,
            String cityCode,
            User currentUser,
            FilterType filterType
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (fromDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), fromDate.atStartOfDay()));
            }
            if (toDate != null) {
                predicates.add(cb.lessThan(root.get("createdAt"), toDate.plusDays(1).atStartOfDay()));
            }

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

                Predicate accessPredicate;

                if (medicalType == MedicalType.SANEPID_SERVICE) {
                    Predicate receiverMatch = cb.equal(root.get("receiverOrganizationId"), orgId);

                    Predicate levelPredicate = switch (levelType) {
                        case REPUBLICAN -> cb.conjunction();
                        case REGIONAL, URBAN ->
                                cb.equal(root.get("createdOrg").get("stateCode"), userOrg.getStateCode());
                        case DISTRICT, AREA, INTERDISTRICT, NOT_DEFINED ->
                                cb.equal(root.get("createdOrg").get("cityCode"), userOrg.getCityCode());
                    };

                    accessPredicate = cb.or(receiverMatch, levelPredicate);
                } else {
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