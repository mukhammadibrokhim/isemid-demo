package uz.uzinfocom.isemid.features.report.service.impls;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.classifier.models.ManualReport;
import uz.uzinfocom.isemid.features.classifier.models.Mkb10;
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

@Service
@RequiredArgsConstructor
public class Form12ReportServiceImpl implements Form12ReportService {
    private final Form058Repository form058Repository;
    private final ManualReportRepository manualReportRepository;

    @Override
    public ApiResponse<?> getReport(LocalDate fromDate, LocalDate toDate, LocalDate birthDate, Gender gender, User currentUser) {
        Specification<Form058> form058Specification = filterByCriteria(fromDate, toDate, birthDate, gender, currentUser);
        List<Form058> form058s = form058Repository.findAll(form058Specification);
        List<ManualReport> manualReports = manualReportRepository.findAll();

        List<Form12ReportDto> result = new ArrayList<>();

        for (ManualReport report : manualReports) {
            List<String> mkb10Codes = report.getMkb10Codes()
                    .stream()
                    .map(Mkb10::getCode)
                    .toList();

            List<Form058> matched = form058s.stream()
                    .filter(f -> mkb10Codes.contains(f.getMkb10Code()) || mkb10Codes.contains(f.getFinalMkb10Code()))
                    .toList();

            long total = matched.size();
            long below18 = matched.stream()
                    .filter(f -> {
                        if (f.getPatient() == null || f.getPatient().getBirthDate() == null) return false;
                        return Period.between(f.getPatient().getBirthDate(), LocalDate.now()).getYears() < 18;
                    })
                    .count();

            Form12ReportDto dto = new Form12ReportDto();
            dto.setTitleUz(report.getNameUz());
            dto.setTitleUzCyril(report.getNameUzCyril());
            dto.setTitleRu(report.getNameRu());
            dto.setTitleKaa(report.getNameKaa());
            dto.setCode(report.getCode());
            dto.setMkb10Codes(String.join(", ", mkb10Codes));
            dto.setTotalCount(total);
            dto.setBelow18Count(below18);

            result.add(dto);
        }

        return new ApiResponse<>(true, MessageUtil.OPERATION_SUCCESS, result);
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

            Organization userOrg = currentUser.getCurrentOrganization();

            if (fromDate != null && toDate != null) {
                predicates.add(cb.between(root.get("createdAt"),
                        fromDate.atStartOfDay(),
                        toDate.atTime(23, 59, 59)));
            }

            if (birthDate != null) {
                predicates.add(cb.equal(root.get("patient").get("birthDate"), birthDate));
            }

            if (gender != null) {
                predicates.add(cb.equal(root.get("patient").get("gender").get("code"), gender.name()));
            }

            if (userOrg != null && userOrg.getLevelCode() != null) {
                String userLevelCode = userOrg.getLevelCode();

                switch (userLevelCode) {
                    case "republic_level" -> {
                    }
                    case "region_level" -> predicates.add(cb.and(
                            cb.equal(root.get("createdOrg").get("state").get("code"),
                                    userOrg.getState().getCode())
                    ));

                    case "district_level" -> predicates.add(cb.and(
                            cb.equal(root.get("createdOrg").get("city").get("parentCode"), userOrg.getCity().getParentCode()),
                            cb.equal(root.get("createdOrg").get("level").get("code"), "district_level")
                    ));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
