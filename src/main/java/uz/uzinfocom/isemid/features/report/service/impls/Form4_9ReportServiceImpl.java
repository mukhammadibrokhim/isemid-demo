package uz.uzinfocom.isemid.features.report.service.impls;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.exceptions.ApiException;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.classifier.eums.CatalogType;
import uz.uzinfocom.isemid.features.classifier.models.Catalog;
import uz.uzinfocom.isemid.features.classifier.services.CatalogService;
import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.form058.reposiotries.Form058Repository;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.organization.models.enums.LevelType;
import uz.uzinfocom.isemid.features.patient.enums.Gender;
import uz.uzinfocom.isemid.features.report.dtos.Form1ReportDto;
import uz.uzinfocom.isemid.features.report.dtos.Form4_9ReportDto;
import uz.uzinfocom.isemid.features.report.service.Form4_9ReportService;
import uz.uzinfocom.isemid.features.report.utils.Form058StatisticsUtil;
import uz.uzinfocom.isemid.features.user.models.User;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static uz.uzinfocom.isemid.features.report.service.impls.Form1ReportServiceImpl.filterByCriteria;

@Service
@RequiredArgsConstructor
public class Form4_9ReportServiceImpl implements Form4_9ReportService {
    private final Form058Repository form058Repository;
    private final MessageUtil messageUtil;
    private final CatalogService catalogService;

    @Override
    public Object getReport(LocalDate fromDate, LocalDate toDate, LocalDate birthDate, String mkb10Code, Gender gender, User currentUser) {
        Specification<Form058> specification = filterByCriteria(fromDate, toDate, birthDate, mkb10Code, gender, currentUser);
        List<Form058> form058s = form058Repository.findAll(specification);
        Organization organization = currentUser.getCurrentOrganization();
        String level = organization.getLevelType().name();

        if (level.isEmpty()) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, messageUtil.get("error.organization.level.code.missing"));
        }

        if (level.equals(LevelType.REPUBLICAN.name())) {
            return createRepublicReport(form058s);

        }
//        else if (level.equals(LevelType.REGIONAL.name())) {
//            return creteRegionReport(form058s, organization);
//
//        } else if (level.equals(LevelType.DISTRICT.name()) || level.equals(LevelType.AREA.name()) || level.equals(LevelType.URBAN.name())
//                   || level.equals(LevelType.INTERDISTRICT.name()) || level.equals(LevelType.NOT_DEFINED.name())) {
//            return creteDistrictReport(form058s, organization);
//        }
        throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, messageUtil.get("error.organization.level.invalid"));
    }


    private Object createRepublicReport(List<Form058> form058s) {

        Map<String, Long> count = countByCategory(form058s);

        Form4_9ReportDto report = Form4_9ReportDto.builder()
                .name("Republic")
                .unorganized(count.getOrDefault("NO_ORGANIZED", 0L))
                .organized(count.getOrDefault("ORGANIZED", 0L))
                .pupils(count.getOrDefault("STUDENT_SCHOOL", 0L))
                .secondaryInstitutions(count.getOrDefault("MIDDLE_STUDENT", 0L))
                .institutions(count.getOrDefault("STUDENT", 0L))
                .employee(count.getOrDefault("WORKER", 0L))
                .medEmployee(count.getOrDefault("MEDICAL_WORKER", 0L))
                .unEmployee(count.getOrDefault("NOT_EMPLOYED", 0L))
                .pensioners(count.getOrDefault("PENSIONER", 0L))
                .homelessPeople(count.getOrDefault("UNSHELTRED", 0L))
                .build();

        return report;
    }

    private Map<String, Long> countByCategory(List<Form058> form058s) {
        return form058s.stream()
                .collect(Collectors.groupingBy(
                        fm -> fm.getPatient().getCategoryCode(),
                        Collectors.counting()
                ));
    }


}
