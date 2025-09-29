package uz.uzinfocom.isemid.features.report.service.impls;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.utils.SpecificationUtil;
import uz.uzinfocom.isemid.features.classifier.models.ManualReport;
import uz.uzinfocom.isemid.features.classifier.repositories.ManualReportRepository;
import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.form058.reposiotries.Form058Repository;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.patient.enums.Gender;
import uz.uzinfocom.isemid.features.report.dtos.Form12ReportDto;
import uz.uzinfocom.isemid.features.report.service.Form12ReportService;
import uz.uzinfocom.isemid.features.user.models.User;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class Form12ReportServiceImpl implements Form12ReportService {
    private final Form058Repository form058Repository;
    private final ManualReportRepository manualReportRepository;

    @Override
    public Object getReport(LocalDate fromDate, LocalDate toDate, LocalDate birthDate, String mkb10Code, Gender gender, User currentUser) {
        Specification<Form058> form058Specification = filterByCriteria(fromDate, toDate, birthDate, gender, currentUser);
        List<Form058> form058s = form058Repository.findAll(form058Specification);
        List<ManualReport> manualReports = manualReportRepository.findAll();

        List<Form12ReportDto> result = new ArrayList<>();

        for (ManualReport report : manualReports) {

            if (!Boolean.TRUE.equals(report.getIncludeInTotal())) {
                continue;
            }

            Set<String> mkb10Codes = report.getMkb10Codes();

            List<Form058> matched = form058s.stream()
                    .filter(f -> mkb10Codes.contains(f.getMkb10Code()) || mkb10Codes.contains(f.getFinalMkb10Code()))
                    .toList();

            long total = matched.size();
            long below18 = matched.stream()
                    .filter(f -> {
                        if (f.getPatient() == null || f.getPatient().getBirthDate() == null) return false;
                        return Period.between(f.getPatient().getBirthDate(), LocalDate.now()).getYears() < 18;
                    }).count();

            Form12ReportDto dto = new Form12ReportDto();
            dto.setShortName(report.getShortName());
            dto.setTitleUz(report.getNameUz());
            dto.setTitleUzCyril(report.getNameUzCyril());
            dto.setTitleRu(report.getNameRu());
            dto.setTitleKaa(report.getNameKaa());
            dto.setCode(report.getCode());
            dto.setTotalCount(total);
            dto.setBelow18Count(below18);

            result.add(dto);
        }

        return result;
    }

    public static Specification<Form058> filterByCriteria(
            LocalDate fromDate,
            LocalDate toDate,
            LocalDate birthDate,
            Gender gender,
            User currentUser
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            SpecificationUtil.fromToFilter(fromDate, toDate, root, cb, predicates);

            if (birthDate != null) {
                predicates.add(cb.equal(root.get("patient").get("birthDate"), birthDate));
            }

            if (gender != null) {
                predicates.add(cb.equal(root.get("patient").get("gender").get("code"), gender.name()));
            }

            Organization userOrg = currentUser.getCurrentOrganization();
            if (userOrg != null) {
                Predicate levelPredicate = SpecificationUtil.buildOrganizationLevelPredicate(root, cb, userOrg);
                predicates.add(levelPredicate);
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

}
