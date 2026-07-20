package uz.uzinfocom.isemid.features.form129.specifications;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;
import uz.uzinfocom.isemid.commons.utils.SpecificationUtils;
import uz.uzinfocom.isemid.features.form058.models.enums.FilterType;
import uz.uzinfocom.isemid.features.form129.dtos.requests.Form129FilterRequest;
import uz.uzinfocom.isemid.features.form129.models.Form129;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.patient.models.Identifier;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.util.StringUtils.hasText;

/**
 * Builds Form129 filter specification from request fields.
 *
 * <p>This method applies only request-level filters and must remain independent
 * of organization access scope resolution (see {@link Form129AccessSpecification}).
 */
public class Form129Specification {

    public static Specification<Form129> build(Form129FilterRequest filterRequest, FilterType filterType) {
        if (filterRequest == null) {
            return Specification.where(null);
        }

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            SpecificationUtils.fromToDateFilter(filterRequest.fromDate(), filterRequest.toDate(), root, cb, predicates, "createdAt");

            if (hasText(filterRequest.stateCode()) || hasText(filterRequest.cityCode())) {
                Join<Form129, Organization> createdOrgJoin = root.join("createdOrg", JoinType.INNER);

                if (hasText(filterRequest.stateCode())) {
                    predicates.add(cb.equal(
                            createdOrgJoin.get("stateCode"),
                            filterRequest.stateCode()
                    ));
                }

                if (hasText(filterRequest.cityCode())) {
                    predicates.add(cb.equal(
                            createdOrgJoin.get("cityCode"),
                            filterRequest.cityCode()
                    ));
                }
            }

            if (filterRequest.organizationId() != null) {
                String orgColumn = filterType == FilterType.RECEIVER ? "receiverOrganizationId" : "senderOrganizationId";
                predicates.add(cb.equal(root.get(orgColumn), filterRequest.organizationId()));
            }

            if (hasText(filterRequest.patientDocValue())) {
                assert query != null;
                Subquery<Long> identifierExistsSubquery = query.subquery(Long.class);
                Root<Identifier> identifierRoot = identifierExistsSubquery.from(Identifier.class);

                identifierExistsSubquery.select(cb.literal(1L));
                identifierExistsSubquery.where(
                        cb.equal(
                                identifierRoot.get("patient").get("id"),
                                root.get("patient").get("id")
                        ),
                        cb.equal(
                                identifierRoot.get("value"),
                                filterRequest.patientDocValue()
                        )
                );

                predicates.add(cb.exists(identifierExistsSubquery));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
