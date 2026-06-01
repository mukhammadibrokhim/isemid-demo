package uz.uzinfocom.isemid.features.report.service.impls;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.isemid.commons.utils.SpecificationUtils;
import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.form058.models.enums.FormStatus;
import uz.uzinfocom.isemid.features.form058.reposiotries.Form058Repository;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.patient.enums.Gender;
import uz.uzinfocom.isemid.features.report.service.Form4_9ReportService;
import uz.uzinfocom.isemid.features.report.utils.Form4_9ReportHelper;
import uz.uzinfocom.isemid.features.user.models.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.util.StringUtils.hasText;

@Service
@RequiredArgsConstructor
public class Form4_9ReportServiceImpl implements Form4_9ReportService {

    private final Form058Repository form058Repository;
    private final Form4_9ReportHelper helper;

    @Override
    @Transactional(readOnly = true)
    public Object getReport(
            LocalDate fromDate,
            LocalDate toDate,
            LocalDate birthDate,
            String mkb10Code,
            Gender gender,
            User currentUser,
            String parentCode
    ) {
        Organization org = currentUser.getCurrentOrganization();

        Specification<Form058> spec = build(
                fromDate,
                toDate,
                birthDate,
                mkb10Code,
                gender,
                currentUser
        );

        List<Form058> forms = form058Repository.findAll(spec);

        if (hasText(parentCode)) {
            return helper.buildByParentCode(parentCode.trim(), forms);
        }

        return switch (org.getLevelType()) {
            case REPUBLICAN -> helper.buildRegionReports(forms);

            case REGIONAL, URBAN -> helper.buildDistrictReports(
                    org.getStateCode(),
                    forms
            );

            case DISTRICT, AREA, INTERDISTRICT, NOT_DEFINED -> {
                String code = org.getCity() != null
                        ? org.getCity().getCode()
                        : org.getCityCode();

                if (!hasText(code)) {
                    code = org.getStateCode();
                }

                yield helper.buildOrganizations(code, forms);
            }
        };
    }

    private Specification<Form058> build(
            LocalDate fromDate,
            LocalDate toDate,
            LocalDate birthDate,
            String mkb10Code,
            Gender gender,
            User currentUser
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (query != null) {
                query.distinct(true);

                /*
                 * Helper needs createdOrg and patient for in-memory grouping/counting.
                 * Fetch join prevents N+1 queries and LazyInitializationException.
                 */
                if (Form058.class.equals(query.getResultType())) {
                    root.fetch("createdOrg", JoinType.LEFT);
                    root.fetch("patient", JoinType.LEFT);
                }
            }

            SpecificationUtils.fromToDateFilter(
                    fromDate,
                    toDate,
                    root,
                    cb,
                    predicates,
                    "createdAt"
            );

            /*
             * Report must be built from NOT APPROVED forms.
             * If status is nullable, NULL is also treated as not approved.
             */
            predicates.add(
                    cb.or(
                            cb.isNull(root.get("status")),
                            cb.notEqual(root.get("status"), FormStatus.APPROVED)
                    )
            );

            if (hasText(mkb10Code)) {
                predicates.add(cb.equal(root.get("finalMkb10Code"), mkb10Code.trim()));
            }

            if (birthDate != null || gender != null) {
                Join<Form058, ?> patientJoin = root.join("patient", JoinType.INNER);

                if (birthDate != null) {
                    predicates.add(cb.equal(patientJoin.get("birthDate"), birthDate));
                }

                if (gender != null) {
                    predicates.add(cb.equal(patientJoin.get("gender"), gender));
                }
            }

            if (currentUser != null && currentUser.getCurrentOrganization() != null) {
                SpecificationUtils.addSanepidAccessPredicate(
                        root,
                        cb,
                        currentUser.getCurrentOrganization(),
                        predicates
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}