package uz.uzinfocom.isemid.features.report.service.impls;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.utils.PatientUtils;
import uz.uzinfocom.isemid.commons.utils.SpecificationUtils;
import uz.uzinfocom.isemid.features.classifier.models.ManualReport;
import uz.uzinfocom.isemid.features.classifier.models.enums.ReportType;
import uz.uzinfocom.isemid.features.classifier.services.ManualReportService;
import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.form058.reposiotries.Form058Repository;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.report.dtos.Form28_2ReportDto;
import uz.uzinfocom.isemid.features.report.service.Form28_2ReportService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.springframework.util.StringUtils.hasText;
import static uz.uzinfocom.isemid.commons.utils.PatientUtils.isAgeLessOrEqual;
import static uz.uzinfocom.isemid.commons.utils.PatientUtils.isAgeLessOrEqualAtFormCreation;

@Service
@RequiredArgsConstructor
public class Form28_2ReportServiceImpl implements Form28_2ReportService {

    private final ManualReportService manualReportService;
    private final Form058Repository form058Repository;

    @Override
    public List<Form28_2ReportDto> getReports(LocalDate fromDate, LocalDate toDate, String stateCode, String cityCode,
                                              String mkb10Code) {

        List<Form058> form058List = form058Repository.findAll(
                filterByCriteria(fromDate, toDate, stateCode, cityCode, mkb10Code)
        );

        List<ManualReport> manualReports =
                manualReportService.getManualReportsForForm(ReportType.FORM_28_2.name());

        List<Form28_2ReportDto> result = new ArrayList<>();

        for (ManualReport report : manualReports) {
            Set<String> mkbCodes = report.getMkb10Codes();

                List<Form058> matched = form058List.stream()
                        .filter(f -> mkbCodes.contains(f.getFinalMkb10Code()))
                        .toList();

                long total = matched.size();
                long age0to17 = matched.stream().filter(f -> isAgeLessOrEqualAtFormCreation(f, 17)).count();
                long under1Month = matched.stream().filter(PatientUtils::isUnderOneMonthAtFormCreation).count();
                long from1MonthTo1Year = matched.stream().filter(PatientUtils::isAgeFrom1MonthTo1YearAtFormCreation).count();

                Form28_2ReportDto dto = new Form28_2ReportDto();
                dto.setShortName(report.getShortName());
                dto.setTitleUz(report.getNameUz());
                dto.setTitleUzCyril(report.getNameUzCyril());
                dto.setTitleRu(report.getNameRu());
                dto.setTitleKaa(report.getNameKaa());
                dto.setCode(report.getCode());

                dto.setTotalCount(total);
                dto.setAge0to17Count(age0to17);
                dto.setUnder1MonthCount(under1Month);
                dto.setAge1MonthTo1YearCount(from1MonthTo1Year);

                result.add(dto);
        }

        return result;
    }

    private static Specification<Form058> filterByCriteria(
            LocalDate fromDate, LocalDate toDate, String stateCode,
            String cityCode, String mkb10Code
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            SpecificationUtils.fromToDateFilter(fromDate, toDate, root, cb, predicates, "createdAt");

            if (hasText(mkb10Code)) {
                predicates.add(cb.equal(root.get("finalMkb10Code"), mkb10Code));
            }

            predicates.add(cb.equal(root.get("diseasePlaceCode"), "IN_MEDICAL_INSTITUTION"));

            if (hasText(stateCode) || hasText(cityCode)) {

                Join<Form058, Organization> createdOrg =
                        root.join("createdOrg", JoinType.LEFT);

                if (hasText(stateCode)) {
                    predicates.add(
                            cb.equal(createdOrg.get("stateCode"), stateCode)
                    );
                }

                if (hasText(cityCode)) {
                    predicates.add(
                            cb.equal(createdOrg.get("cityCode"), cityCode)
                    );
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
