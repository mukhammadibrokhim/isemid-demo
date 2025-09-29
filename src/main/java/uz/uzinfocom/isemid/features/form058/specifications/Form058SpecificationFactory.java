package uz.uzinfocom.isemid.features.form058.specifications;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import uz.uzinfocom.isemid.commons.utils.SpecificationUtil;
import uz.uzinfocom.isemid.features.form058.enums.FilterType;
import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.organization.models.enums.MedicalType;
import uz.uzinfocom.isemid.features.user.models.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static uz.uzinfocom.isemid.commons.utils.SpecificationUtil.fromToFilter;

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

            fromToFilter(fromDate, toDate, root, cb, predicates);

            if (stateCode != null && !stateCode.isBlank()) {
                predicates.add(cb.equal(root.get("createdOrg").get("stateCode"), stateCode));
            }
            if (cityCode != null && !cityCode.isBlank()) {
                predicates.add(cb.equal(root.get("createdOrg").get("cityCode"), cityCode));
            }

            Organization userOrg = currentUser.getCurrentOrganization();
            if (userOrg != null) {
                Long orgId = userOrg.getId();
                UUID orgUuid = userOrg.getUuid();
                MedicalType medicalType = userOrg.getMedicalType();

                Predicate accessPredicate;

                if (medicalType == MedicalType.SANEPID_SERVICE) {
                    Predicate receiverMatch = cb.equal(root.get("receiverOrganizationId"), orgId);
                    Predicate levelPredicate = SpecificationUtil.buildOrganizationLevelPredicate(root, cb, userOrg);
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