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
import uz.uzinfocom.isemid.features.report.service.Form13ReportService;
import uz.uzinfocom.isemid.features.report.utils.Form13ReportHelper;
import uz.uzinfocom.isemid.features.user.models.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class Form13ReportServiceImpl implements Form13ReportService {
    private final Form058Repository form058Repository;
    private final Form13ReportHelper helper;

    @Override
    public Object generateForm13(
            User currentUser,
            LocalDate startDate,
            LocalDate endDate,
            String parentCode
    ) {

        Organization org = currentUser.getCurrentOrganization();

        // ---- Load filtered Form058 ----
        List<Form058> forms = form058Repository.findAll(
                build(startDate, endDate, currentUser)
        );

        // ---- If direct parentCode requested (Region/District) ----
        if (parentCode != null && !parentCode.isBlank()) {
            return helper.buildByParentCode(parentCode.trim(), forms);
        }

        // ---- Selection based on user level ----
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

    // ====================================================================
    // FORM058 FILTER (DATE + SECURITY LEVEL)
    // ====================================================================
    private Specification<Form058> build(
            LocalDate fromDate,
            LocalDate toDate,
            User currentUser
    ) {
        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.notEqual(root.get("status"), FormStatus.CANCELED));
            // ---- Date filter ----
            SpecificationUtils.fromToDateFilter(
                    fromDate,
                    toDate,
                    root, cb,
                    predicates,
                    "createdAt"
            );

            // ---- Access level filter ----
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
