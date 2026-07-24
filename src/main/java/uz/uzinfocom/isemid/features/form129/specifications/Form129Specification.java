package uz.uzinfocom.isemid.features.form129.specifications;

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

            String orgIdField = filterType == FilterType.RECEIVER ? "receiverOrganizationId" : "senderOrganizationId";

            if (filterRequest.organizationId() != null) {
                predicates.add(cb.equal(root.get(orgIdField), filterRequest.organizationId()));
            }

            if (hasText(filterRequest.stateCode()) || hasText(filterRequest.cityCode())) {
                assert query != null;
                Subquery<Long> orgSubquery = query.subquery(Long.class);
                Root<Organization> orgRoot = orgSubquery.from(Organization.class);

                List<Predicate> orgPredicates = new ArrayList<>();
                orgPredicates.add(cb.equal(orgRoot.get("id"), root.get(orgIdField)));

                if (hasText(filterRequest.stateCode())) {
                    orgPredicates.add(cb.equal(orgRoot.get("stateCode"), filterRequest.stateCode()));
                }

                if (hasText(filterRequest.cityCode())) {
                    orgPredicates.add(cb.equal(orgRoot.get("cityCode"), filterRequest.cityCode()));
                }

                orgSubquery.select(orgRoot.get("id"));
                orgSubquery.where(orgPredicates.toArray(new Predicate[0]));

                predicates.add(cb.exists(orgSubquery));
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
