package uz.uzinfocom.isemid.features.report.service.impls;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.utils.SpecificationUtils;
import uz.uzinfocom.isemid.features.classifier.models.ManualReport;
import uz.uzinfocom.isemid.features.classifier.models.enums.ReportType;
import uz.uzinfocom.isemid.features.classifier.services.ManualReportService;
import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.form058.reposiotries.Form058Repository;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.organization.models.enums.MedicalType;
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
    private final ManualReportService manualReportService;

    @Override
    public List<Form12ReportDto> getReport(
            LocalDate fromDate,
            LocalDate toDate,
            LocalDate birthDate,
            String mkb10Code,
            Gender gender,
            User currentUser
    ) {

        List<Form058> form058List = form058Repository.findAll(
                filterByCriteria(fromDate, toDate, birthDate, gender, currentUser)
        );

        List<ManualReport> manualReports = manualReportService.getManualReportsForForm(ReportType.FORM_12.name());

        List<Form12ReportDto> result = new ArrayList<>();

        for (ManualReport report : manualReports) {
            Set<String> mkb10Codes = report.getMkb10Codes();

            List<Form058> matched = form058List.stream()
                    .filter(f -> mkb10Codes.contains(f.getFinalMkb10Code()))
                    .toList();

            long total = matched.size();

            long below14 = matched.stream()
                    .filter(f -> f.getPatient() != null &&
                            f.getPatient().getBirthDate() != null &&
                            Period.between(f.getPatient().getBirthDate(), f.getCreatedAt().toLocalDateTime().toLocalDate()).getYears() < 14) // 14 yoshgacha
                    .count();

            long below18 = matched.stream()
                    .filter(f -> f.getPatient() != null &&
                            f.getPatient().getBirthDate() != null &&
                            Period.between(f.getPatient().getBirthDate(), f.getCreatedAt().toLocalDateTime().toLocalDate()).getYears() < 18) // 18 yoshgacha
                    .count();

            Form12ReportDto dto = new Form12ReportDto();
            dto.setShortName(report.getShortName());
            dto.setTitleUz(report.getNameUz());
            dto.setTitleUzCyril(report.getNameUzCyril());
            dto.setTitleRu(report.getNameRu());
            dto.setTitleKaa(report.getNameKaa());
            dto.setCode(report.getCode());
            dto.setTotalCount(total);
            dto.setBelow14Count(below14);
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

            SpecificationUtils.fromToDateFilter(fromDate, toDate, root, cb, predicates, "createdAt");

            if (birthDate != null) {
                predicates.add(cb.equal(root.get("patient").get("birthDate"), birthDate));
            }

            if (gender != null) {
                predicates.add(cb.equal(root.get("patient").get("genderCode"), gender.name()));
            }

            Organization userOrg = currentUser != null ? currentUser.getCurrentOrganization() : null;
            if (userOrg != null && userOrg.getMedicalType() == MedicalType.SANEPID_SERVICE) {
                SpecificationUtils.addSanepidAccessPredicate(root, cb, userOrg, predicates);
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
