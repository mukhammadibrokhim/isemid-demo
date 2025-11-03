package uz.uzinfocom.isemid.features.report.service.impls;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.utils.SpecificationUtils;
import uz.uzinfocom.isemid.features.classifier.models.ManualReport;
import uz.uzinfocom.isemid.features.classifier.repositories.ManualReportRepository;
import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.form058.reposiotries.Form058Repository;
import uz.uzinfocom.isemid.features.report.dtos.Form28_1ReportDto;
import uz.uzinfocom.isemid.features.report.service.Form28_1ReportService;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.springframework.util.StringUtils.hasText;

@Service
@RequiredArgsConstructor
public class Form28_1ReportServiceImpl implements Form28_1ReportService {

    private final Form058Repository form058Repository;
    private final ManualReportRepository manualReportRepository;

    @Override
    public List<Form28_1ReportDto> getReports(LocalDate fromDate, LocalDate toDate, String mkb10Code) {

        List<Form058> form058List = form058Repository.findAll(
                filterByCriteria(fromDate, toDate, mkb10Code)
        );

        List<ManualReport> manualReports = manualReportRepository.findAll()
                .stream()
                .filter(r -> Boolean.TRUE.equals(r.getIncludeInTotal()))
                .toList();

        List<Form28_1ReportDto> result = new ArrayList<>();

        for (ManualReport report : manualReports) {
            Set<String> mkbCodes = report.getMkb10Codes();

            for (String code : mkbCodes) {

                // ✅ Final kod mavjud bo‘lsa — faqat finalMkb10Code bo‘yicha
                // ✅ Aks holda — oddiy mkb10Code bo‘yicha hisoblaymiz
                List<Form058> matched = form058List.stream()
                        .filter(f -> {
                            String finalCode = f.getFinalMkb10Code();

                            if (finalCode != null && !finalCode.isBlank()) {
                                return code.equalsIgnoreCase(finalCode);
                            }
                            return false;
                        })
                        .toList();

                if (matched.isEmpty()) continue;

                long total = matched.size();
                long female = matched.stream().filter(this::isFemale).count();
                long age017 = matched.stream().filter(f -> isAgeLessOrEqual(f, 17)).count();
                long age014 = matched.stream().filter(f -> isAgeLessOrEqual(f, 14)).count();
                long under1 = matched.stream().filter(f -> isAgeLessOrEqual(f, 1)).count();
                long age1to2 = matched.stream().filter(f -> isAgeBetween(f, 1, 2)).count();
                long age3to5 = matched.stream().filter(f -> isAgeBetween(f, 3, 5)).count();
                long rural = matched.stream().filter(this::isRural).count();
                long rural017 = matched.stream().filter(f -> isRural(f) && isAgeLessOrEqual(f, 17)).count();

                Form28_1ReportDto dto = new Form28_1ReportDto();
                dto.setShortName(report.getShortName());
                dto.setTitleUz(report.getNameUz());
                dto.setTitleUzCyril(report.getNameUzCyril());
                dto.setTitleRu(report.getNameRu());
                dto.setTitleKaa(report.getNameKaa());
                dto.setIcd10Code(code);

                dto.setTotalCases(total);
                dto.setFemaleCases(female);
                dto.setAge0to17Cases(age017);
                dto.setAge0to14Cases(age014);
                dto.setUnder1YearCases(under1);
                dto.setAge1to2Cases(age1to2);
                dto.setAge3to5Cases(age3to5);
                dto.setRuralBreakdown(new Form28_1ReportDto.RuralBreakdownDto(rural, rural017));

                result.add(dto);
            }
        }
        return result;
    }

    private static Specification<Form058> filterByCriteria(LocalDate fromDate, LocalDate toDate, String mkb10Code) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            SpecificationUtils.fromToDateFilter(fromDate, toDate, root, cb, predicates, "createdAt");

            if (hasText(mkb10Code)) {
                predicates.add(cb.equal(root.get("finalMkb10Code"), mkb10Code));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private boolean isFemale(Form058 f) {
        return f.getPatient() != null &&
                f.getPatient().getGenderCode() != null &&
                f.getPatient().getGenderCode().equalsIgnoreCase("FEMALE");
    }

    private boolean isAgeLessOrEqual(Form058 f, int maxAge) {
        if (f.getPatient() == null || f.getPatient().getBirthDate() == null) return false;
        int age = Period.between(f.getPatient().getBirthDate(), LocalDate.now()).getYears();
        return age <= maxAge;
    }

    private boolean isAgeBetween(Form058 f, int min, int max) {
        if (f.getPatient() == null || f.getPatient().getBirthDate() == null) return false;
        int age = Period.between(f.getPatient().getBirthDate(), LocalDate.now()).getYears();
        return age >= min && age <= max;
    }

    private boolean isRural(Form058 f) {
        return f.getPatient() != null &&
                f.getPatient().getPopulationTypeCode() != null &&
                f.getPatient().getPopulationTypeCode().equalsIgnoreCase("RURAL");
    }
}

