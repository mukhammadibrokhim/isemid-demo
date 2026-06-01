package uz.uzinfocom.isemid.features.report.service.impls;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.utils.SpecificationUtils;
import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.form058.models.enums.FormStatus;
import uz.uzinfocom.isemid.features.form058.reposiotries.Form058Repository;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.report.service.Form6ReportService;
import uz.uzinfocom.isemid.features.report.utils.Form6ReportHelper;
import uz.uzinfocom.isemid.features.user.models.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.util.StringUtils.hasText;

@Service
@RequiredArgsConstructor
public class Form6ReportServiceImpl implements Form6ReportService {
    private final Form058Repository form058Repository;
    private final Form6ReportHelper helper;

    @Override
    public Object generateFor6(
            User currentUser,
            LocalDate startDate,
            LocalDate endDate,
            String parentCode,
            String mkb10Code) {

        Organization org = currentUser.getCurrentOrganization();

        List<Form058> forms = form058Repository.findAll(
                build(startDate, endDate, mkb10Code, currentUser)
        );

        if (parentCode != null && !parentCode.isBlank()) {
            return helper.buildByParentCode(parentCode.trim(), forms);
        }

        return switch (org.getLevelType()) {

            case REPUBLICAN -> helper.buildRegionReports(forms);

            case REGIONAL, URBAN -> helper.buildDistrictReports(
                    org.getStateCode(),
                    forms
            );

            case DISTRICT, AREA, INTERDISTRICT, NOT_DEFINED -> {
                String cityCode = org.getCity() != null ?
                        org.getCity().getCode() : org.getStateCode();
                yield helper.buildOrganizations(cityCode, forms);
            }
        };
    }

    private Specification<Form058> build(
            LocalDate fromDate,
            LocalDate toDate,
            String mkb10Code, User currentUser
    ) {
        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            SpecificationUtils.fromToDateFilter(
                    fromDate,
                    toDate,
                    root, cb,
                    predicates,
                    "createdAt"
            );

            predicates.add(
                    cb.equal(root.get("status"), FormStatus.APPROVED)
            );

            if (hasText(mkb10Code)) {
                predicates.add(cb.equal(root.get("finalMkb10Code"), mkb10Code));
            }

            if (currentUser != null && currentUser.getCurrentOrganization() != null) {
                SpecificationUtils.addSanepidAccessPredicate(
                        root, cb,
                        currentUser.getCurrentOrganization(),
                        predicates
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

}
