package uz.uzinfocom.isemid.features.report.service.impls;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.exceptions.ApiException;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.commons.utils.SpecificationUtils;
import uz.uzinfocom.isemid.features.classifier.eums.CatalogType;
import uz.uzinfocom.isemid.features.classifier.models.Catalog;
import uz.uzinfocom.isemid.features.classifier.models.ManualReport;
import uz.uzinfocom.isemid.features.classifier.services.CatalogService;
import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.form058.reposiotries.Form058Repository;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.organization.models.enums.LevelType;
import uz.uzinfocom.isemid.features.patient.enums.Gender;
import uz.uzinfocom.isemid.features.report.dtos.Form13ReportDto;
import uz.uzinfocom.isemid.features.report.dtos.Form1ReportDto;
import uz.uzinfocom.isemid.features.report.dtos.Form4_9ReportDto;
import uz.uzinfocom.isemid.features.report.service.Form4_9ReportService;
import uz.uzinfocom.isemid.features.report.utils.Form058StatisticsUtil;
import uz.uzinfocom.isemid.features.report.utils.Form4_9ReportHelper;
import uz.uzinfocom.isemid.features.user.models.User;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static uz.uzinfocom.isemid.features.report.service.impls.Form1ReportServiceImpl.filterByCriteria;
@Service
@RequiredArgsConstructor
public class Form4_9ReportServiceImpl implements Form4_9ReportService {

    private final Form058Repository form058Repository;
    private final Form4_9ReportHelper helper;

    @Override
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

        // ---- Filter forms ----
        Specification<Form058> spec = build(fromDate, toDate, currentUser);
        List<Form058> forms = form058Repository.findAll(spec);

        if (parentCode != null && !parentCode.isBlank()) {
            return helper.buildByParentCode(parentCode.trim(), forms);
        }

        return switch (org.getLevelType()) {

            case REPUBLICAN -> helper.buildRegionReports(forms);

            case REGIONAL, URBAN -> helper.buildDistrictReports(
                    org.getStateCode(), forms
            );

            case DISTRICT, AREA, INTERDISTRICT, NOT_DEFINED -> {
                String code = org.getCity() != null ?
                        org.getCity().getCode() : org.getStateCode();
                yield helper.buildOrganizations(code, forms);
            }
        };
    }

    // ---- COPY OF FORM-13 FILTER ----
    private Specification<Form058> build(
            LocalDate fromDate,
            LocalDate toDate,
            User currentUser
    ) {
        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            SpecificationUtils.fromToDateFilter(
                    fromDate, toDate,
                    root, cb, predicates, "createdAt"
            );

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

