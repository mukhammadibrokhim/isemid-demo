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
import uz.uzinfocom.isemid.features.report.dtos.Form28_1ReportDto;
import uz.uzinfocom.isemid.features.report.service.Form28_1ReportService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.springframework.util.StringUtils.hasText;
import static uz.uzinfocom.isemid.commons.utils.PatientUtils.*;

@Service
@RequiredArgsConstructor
public class Form28_1ReportServiceImpl implements Form28_1ReportService {

    private final Form058Repository form058Repository;
    private final ManualReportService manualReportService;

    @Override
    public List<Form28_1ReportDto> getReports(LocalDate fromDate, LocalDate toDate, String stateCode, String cityCode,
                                              String mkb10Code) {

        List<Form058> form058List = form058Repository.findAll(
                filterByCriteria(fromDate, toDate, stateCode, cityCode, mkb10Code)
        );

        List<ManualReport> manualReports = manualReportService.getManualReportsForForm(ReportType.FORM_28_1.name());

        List<Form28_1ReportDto> result = new ArrayList<>();

        for (ManualReport report : manualReports) {
            Set<String> mkbCodes = report.getMkb10Codes();


            List<Form058> matched = form058List.stream()
                    .filter(f -> mkbCodes.contains(f.getFinalMkb10Code()))
                    .toList();

                long total = matched.size();
                long female = matched.stream().filter(PatientUtils::isFemale).count();
                long age017 = matched.stream().filter(f -> isAgeLessOrEqualAtFormCreation(f, 17)).count();
                long age014 = matched.stream().filter(f -> isAgeLessOrEqualAtFormCreation(f, 14)).count();
                long under1 = matched.stream().filter(f -> isAgeLessOrEqualAtFormCreation(f, 1)).count();
                long age1to2 = matched.stream().filter(f -> isAgeBetweenAtFormCreation(f, 1, 2)).count();
                long age3to5 = matched.stream().filter(f -> isAgeBetweenAtFormCreation(f, 3, 5)).count();
                long rural = matched.stream().filter(PatientUtils::isRural).count();
                long rural017 = matched.stream().filter(f -> isRural(f) && isAgeLessOrEqualAtFormCreation(f, 17)).count();

                Form28_1ReportDto dto = new Form28_1ReportDto();
                dto.setShortName(report.getShortName());
                dto.setTitleUz(report.getNameUz());
                dto.setTitleUzCyril(report.getNameUzCyril());
                dto.setTitleRu(report.getNameRu());
                dto.setTitleKaa(report.getNameKaa());
                dto.setCode(report.getCode());

                dto.setTotalCount(total);
                dto.setFemaleCount(female);
                dto.setAge0to17Count(age017);
                dto.setAge0to14Count(age014);
                dto.setUnder1YearCount(under1);
                dto.setAge1to2Count(age1to2);
                dto.setAge3to5Count(age3to5);
                dto.setRural(new Form28_1ReportDto.RuralBreakdownDto(rural, rural017));

                result.add(dto);
            }
        return result;
    }

    private static Specification<Form058> filterByCriteria(
            LocalDate fromDate,
            LocalDate toDate,
            String stateCode,
            String cityCode,
            String mkb10Code
    ) {
        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            SpecificationUtils.fromToDateFilter(
                    fromDate, toDate, root, cb, predicates, "createdAt"
            );

            if (hasText(mkb10Code)) {
                predicates.add(cb.equal(root.get("finalMkb10Code"), mkb10Code));
            }

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

