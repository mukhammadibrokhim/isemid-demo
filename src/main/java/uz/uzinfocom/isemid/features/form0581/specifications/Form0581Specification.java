package uz.uzinfocom.isemid.features.form0581.specifications;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;
import uz.uzinfocom.isemid.commons.utils.SpecificationUtils;
import uz.uzinfocom.isemid.features.form0581.dtos.requests.Form0581FilterRequest;
import uz.uzinfocom.isemid.features.form0581.models.Form0581;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.patient.models.Identifier;
import uz.uzinfocom.isemid.features.patient.models.Patient;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.util.StringUtils.hasText;

/**
 * Builds Form0581 filter specification from request fields.
 *
 * <p>This method applies only request-level filters and must remain independent
 * of organization access scope resolution (see {@link Form0581AccessSpecification}).
 */
public class Form0581Specification {

    public static Specification<Form0581> build(Form0581FilterRequest filterRequest) {
        if (filterRequest == null) {
            return Specification.where(null);
        }

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            SpecificationUtils.fromToDateFilter(filterRequest.fromDate(), filterRequest.toDate(), root, cb, predicates, "createdAt");

            if (hasText(filterRequest.stateCode()) || hasText(filterRequest.cityCode())) {
                Join<Form0581, Organization> createdOrgJoin = root.join("createdOrg", JoinType.INNER);

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

            if (hasText(filterRequest.mkb10Code())) {
                predicates.add(cb.equal(root.get("mkb10Code"), filterRequest.mkb10Code()));
            }
            if (filterRequest.status() != null) {
                predicates.add(cb.equal(root.get("status"), filterRequest.status()));
            }

            if (hasText(filterRequest.source())) {
                predicates.add(cb.equal(root.get("source"), filterRequest.source().trim()));
            }

            if (filterRequest.organizationId() != null) {
                predicates.add(cb.equal(root.get("senderOrganizationId"), filterRequest.organizationId()));
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

    public static Specification<Form0581> build(final String nnuzb) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (hasText(nnuzb)) {
                Join<Form0581, Patient> patientJoin = root.join("patient", JoinType.LEFT);
                Join<Patient, Identifier> idJoin = patientJoin.join("identifiers", JoinType.LEFT);

                predicates.add(cb.equal(idJoin.get("value"), nnuzb));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Form0581> buildStats(
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
                predicates.add(root.get("diagnosisApproval").get("finalMkb10Code").in(mkb10Codes));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
