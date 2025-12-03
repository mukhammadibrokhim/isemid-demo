package uz.uzinfocom.isemid.features.report.service.impls;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.utils.SpecificationUtils;
import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.form058.reposiotries.Form058Repository;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.report.service.Form11ReportService;
import uz.uzinfocom.isemid.features.report.utils.Form11ReportHelper;
import uz.uzinfocom.isemid.features.user.models.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class Form11ReportServiceImpl implements Form11ReportService {
    private final Form058Repository form058Repository;
    private final Form11ReportHelper helper;

    @Override
    public Object generate(LocalDate startDate, LocalDate endDate, String parentCode, Long koef, Long population,
                           User currentUser) {
        Organization org = currentUser.getCurrentOrganization();

        List<Form058> forms = form058Repository.findAll(build(startDate, endDate, currentUser));

        if (parentCode != null && !parentCode.isBlank()) {
            return helper.buildByParentCode(parentCode.trim(), forms, koef, population);
        }

        return switch (org.getLevelType()) {

            case REPUBLICAN -> helper.buildRegionReports(forms, koef, population);

            case REGIONAL, URBAN -> helper.buildDistrictReports(
                    org.getStateCode(),
                    forms,
                    koef,
                    population
            );

            case DISTRICT, AREA, INTERDISTRICT, NOT_DEFINED -> {
                String cityCode = org.getCity() != null ?
                        org.getCity().getCode() : org.getStateCode();
                yield helper.buildOrganizations(cityCode, forms, koef, population);
            }
        };
    }

    private Specification<Form058> build(
            LocalDate fromDate,
            LocalDate toDate,
            User currentUser
    ) {
        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            SpecificationUtils.fromToDateFilter(fromDate, toDate, root, cb, predicates, "createdAt");

            if (currentUser != null && currentUser.getCurrentOrganization() != null) {
                SpecificationUtils.addSanepidAccessPredicate(root, cb, currentUser.getCurrentOrganization(), predicates);
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
