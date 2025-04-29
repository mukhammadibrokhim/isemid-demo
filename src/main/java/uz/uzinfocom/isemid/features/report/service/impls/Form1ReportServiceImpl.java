package uz.uzinfocom.isemid.features.report.service.impls;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.classifier.eums.CatalogType;
import uz.uzinfocom.isemid.features.classifier.models.Catalog;
import uz.uzinfocom.isemid.features.classifier.services.CatalogService;
import uz.uzinfocom.isemid.features.form058.enums.FormStatus;
import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.form058.reposiotries.Form058Repository;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.patient.enums.Gender;
import uz.uzinfocom.isemid.features.report.dtos.Form1ReportDto;
import uz.uzinfocom.isemid.features.report.enums.LevelStatus;
import uz.uzinfocom.isemid.features.report.service.Form1ReportService;
import uz.uzinfocom.isemid.features.user.models.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static uz.uzinfocom.isemid.features.report.utils.ReportUtil.isAdult;

@Service
@RequiredArgsConstructor
public class Form1ReportServiceImpl implements Form1ReportService {

    private final Form058Repository form058Repository;
    private final CatalogService catalogService;

    @Override
    public ApiResponse<?> getReport(LocalDate fromDate, LocalDate toDate, LocalDate birthDate, Gender gender, User currentUser) {
        Specification<Form058> specification = filterByCriteria(fromDate, toDate, birthDate, gender, currentUser);
        List<Form058> form058s = form058Repository.findAll(specification);
        Organization organization = currentUser.getCurrentOrganization();
        String level = organization.getLevelCode();
        List<Catalog> states = catalogService.finAllByType(CatalogType.state);

        List<Form1ReportDto> reportList = new LinkedList<>();


        if (level.equals(LevelStatus.republic_level.name())) {
            List<Form058> confirmed58 = form058s.stream().filter(form058 -> form058.getStatus().equals(FormStatus.APPROVED)).toList();


            Set<Form1ReportDto> children = states.stream().map(state -> {
                List<Form058> stateForms = form058s.stream()
                        .filter(f -> f.getCreatedOrg() != null
                                && f.getCreatedOrg().getState() != null
                                && f.getCreatedOrg().getState().getCode().equals(state.getCode()))
                        .toList();

                return Form1ReportDto.builder()
                        .name(state.getNameRu())
                        .confirmedTotal(stateForms.stream().
                                filter(f -> f.getStatus().equals(FormStatus.APPROVED)).count())
                        .confirmedAdults(stateForms.stream()
                                .filter(f -> f.getStatus().equals(FormStatus.APPROVED))
                                .filter(f -> isAdult(f.getPatient().getBirthDate())).count())
                        .confirmedUnder18(stateForms.stream().filter(f -> f.getStatus().equals(FormStatus.APPROVED))
                                .filter(f -> !isAdult(f.getPatient().getBirthDate())).count())
                        .confirmedFemales(stateForms.stream().filter(f -> f.getStatus().equals(FormStatus.APPROVED))
                                .filter(f -> f.getPatient().getGenderCode().equals(Gender.female.name())).count())
                        .emergencyTotal(stateForms.stream().filter(f -> !f.getStatus().equals(FormStatus.APPROVED)).count())
                        .emergencyAdults(stateForms.stream().filter(f -> !f.getStatus().equals(FormStatus.APPROVED))
                                .filter(f -> isAdult(f.getPatient().getBirthDate())).count())
                        .emergencyUnder18(stateForms.stream().filter(f -> !f.getStatus().equals(FormStatus.APPROVED))
                                .filter(f -> !isAdult(f.getPatient().getBirthDate())).count())
                        .emergencyFemales(stateForms.stream().filter(f -> !f.getStatus().equals(FormStatus.APPROVED))
                                .filter(f -> f.getPatient().getGenderCode().equals(Gender.female.name())).count())
                        .build();
            }).collect(Collectors.toSet());

            reportList.add(Form1ReportDto.builder()
                    .name("O'znekiston Respoublikasi")
                    .confirmedTotal(confirmed58.stream().filter(f -> f.getStatus().equals(FormStatus.APPROVED)).count())
                    .confirmedAdults(confirmed58.stream().filter(f -> f.getStatus().equals(FormStatus.APPROVED))
                            .filter(f -> isAdult(f.getPatient().getBirthDate())).count())
                    .confirmedUnder18(confirmed58.stream().filter(f -> f.getStatus().equals(FormStatus.APPROVED))
                            .filter(f -> !isAdult(f.getPatient().getBirthDate())).count())
                    .confirmedFemales(confirmed58.stream().filter(f -> f.getStatus().equals(FormStatus.APPROVED))
                            .filter(f -> f.getPatient().getGenderCode().equals(Gender.female.name())).count())
                    .emergencyTotal(form058s.stream().filter(f -> !f.getStatus().equals(FormStatus.APPROVED)).count())
                    .emergencyAdults(form058s.stream().filter(f -> !f.getStatus().equals(FormStatus.APPROVED))
                            .filter(f -> isAdult(f.getPatient().getBirthDate())).count())
                    .emergencyUnder18(form058s.stream().filter(f -> !f.getStatus().equals(FormStatus.APPROVED))
                            .filter(f -> !isAdult(f.getPatient().getBirthDate())).count())
                    .emergencyFemales(form058s.stream().filter(f -> !f.getStatus().equals(FormStatus.APPROVED))
                            .filter(f -> f.getPatient().getGenderCode().equals(Gender.female.name())).count())
                    .children(children)
                    .build());

        } else if (level.equals(LevelStatus.region_level.name())) {

        } else if (level.equals(LevelStatus.district_level.name())) {

        }

        return new ApiResponse<>(true, MessageUtil.OPERATION_SUCCESS, reportList);
    }

//    @Override
//    public ApiResponse<?> getReport(LocalDate fromDate, LocalDate toDate, LocalDate birthDate, Gender gender, User currentUser) {
//        Specification<Form058> specification = filterByCriteria(fromDate, toDate, birthDate, gender, currentUser);
//        List<Form058> form058s = form058Repository.findAll(specification);
//
//        Organization userCurrOrg = currentUser.getCurrentOrganization();
//
//        String level = userCurrOrg.getLevelCode();
//        List<Form1ReportDto> reportList = new LinkedList<>();
//
//        if (level.equals(LevelStatus.republic_level.name())) {
//
//            List<Form058> regionForms = form058s.stream().toList();
//
//            Map<String, List<Form058>> districtMap = regionForms.stream()
//                    .filter(f -> f.getCreatedOrg() != null && f.getCreatedOrg().getStateCode() != null)
//                    .collect(Collectors.groupingBy(f -> f.getCreatedOrg().getStateCode()));
//
//            Set<Form1ReportDto> children = new HashSet<>();
//
//            for (Map.Entry<String, List<Form058>> regions : districtMap.entrySet()) {
//                List<Form058> regionsFormList = regions.getValue();
//
//                String regionName = regionsFormList.getFirst().getCreatedOrg().getState().getNameUz();
//                String regionCode = regionsFormList.getFirst().getCreatedOrg().getStateCode(); // region code
//
//                Map<String, List<Form058>> cityMap = regionsFormList.stream()
//                        .filter(f -> f.getCreatedOrg().getCity().getParentCode().equals(regionCode)) // faqat shu regionga tegishli tumanni oladi
//                        .collect(Collectors.groupingBy(f -> f.getCreatedOrg().getCityCode())); // tuman kodlari bo‘yicha grouping
//
//                Set<Form1ReportDto> districtChildren = new HashSet<>();
//
//                for (Map.Entry<String, List<Form058>> cityEntry : cityMap.entrySet()) {
//                    List<Form058> cityForms = cityEntry.getValue();
//                    String districtName = cityForms.getFirst().getCreatedOrg().getName(); // tuman nomi
//
//                    districtChildren.add(Form1ReportDto.builder()
//                            .name(districtName)
//                            .confirmedTotal(cityForms.stream().filter(f -> f.getStatus().equals(FormStatus.APPROVED)).count())
//                            .confirmedAdults(cityForms.stream().filter(f -> f.getStatus().equals(FormStatus.APPROVED))
//                                    .filter(f -> isAdult(f.getPatient().getBirthDate())).count())
//                            .confirmedUnder18(cityForms.stream().filter(f -> f.getStatus().equals(FormStatus.APPROVED))
//                                    .filter(f -> !isAdult(f.getPatient().getBirthDate())).count())
//                            .confirmedFemales(cityForms.stream().filter(f -> f.getStatus().equals(FormStatus.APPROVED))
//                                    .filter(f -> f.getPatient().getGenderCode().equals(Gender.female.name())).count())
//                            .emergencyTotal(cityForms.stream().filter(f -> !f.getStatus().equals(FormStatus.APPROVED)).count())
//                            .emergencyAdults(cityForms.stream().filter(f -> !f.getStatus().equals(FormStatus.APPROVED))
//                                    .filter(f -> isAdult(f.getPatient().getBirthDate())).count())
//                            .emergencyUnder18(cityForms.stream().filter(f -> !f.getStatus().equals(FormStatus.APPROVED))
//                                    .filter(f -> !isAdult(f.getPatient().getBirthDate())).count())
//                            .emergencyFemales(cityForms.stream().filter(f -> !f.getStatus().equals(FormStatus.APPROVED))
//                                    .filter(f -> f.getPatient().getGenderCode().equals(Gender.female.name())).count())
//                            .build());
//                }
//
//
//                children.add(Form1ReportDto.builder()
//                        .name(regionName)
//                        .confirmedTotal(regionsFormList.stream().filter(f -> f.getStatus().equals(FormStatus.APPROVED)).count())
//                        .confirmedAdults(regionsFormList.stream()
//                                .filter(f -> f.getStatus().equals(FormStatus.APPROVED))
//                                .filter(f -> isAdult(f.getPatient().getBirthDate())).count())
//                        .confirmedUnder18(regionsFormList.stream()
//                                .filter(f -> f.getStatus().equals(FormStatus.APPROVED))
//                                .filter(f -> !isAdult(f.getPatient().getBirthDate())).count())
//                        .confirmedFemales(regionsFormList.stream().filter(f -> f.getStatus().equals(FormStatus.APPROVED)).filter(f -> f.getPatient().getGenderCode().equals(Gender.female.name())).count())
//                        .emergencyTotal(regionsFormList.stream().filter(f -> !f.getStatus().equals(FormStatus.APPROVED)).count())
//                        .emergencyAdults(regionsFormList.stream()
//                                .filter(f -> !f.getStatus().equals(FormStatus.APPROVED))
//                                .filter(f -> isAdult(f.getPatient().getBirthDate())).count())
//                        .emergencyUnder18(regionsFormList.stream()
//                                .filter(f -> !f.getStatus().equals(FormStatus.APPROVED))
//                                .filter(f -> !isAdult(f.getPatient().getBirthDate())).count())
//                        .emergencyFemales(regionsFormList.stream().filter(f -> !f.getStatus().equals(FormStatus.APPROVED)).filter(f -> f.getPatient().getGenderCode().equals(Gender.female.name())).count())
//                        .children(districtChildren)
//                        .build());
//            }
//
//            reportList.add(Form1ReportDto.builder()
//                    .name("O'zbekiston Respublikasi")
//                    .confirmedTotal(form058s.stream().filter(f -> f.getStatus().equals(FormStatus.APPROVED)).count())
//                    .confirmedAdults(form058s.stream()
//                            .filter(f -> f.getStatus().equals(FormStatus.APPROVED))
//                            .filter(f -> isAdult(f.getPatient().getBirthDate())).count())
//                    .confirmedUnder18(form058s.stream()
//                            .filter(f -> f.getStatus().equals(FormStatus.APPROVED))
//                            .filter(f -> !isAdult(f.getPatient().getBirthDate())).count())
//                    .confirmedFemales(form058s.stream().filter(f -> f.getStatus().equals(FormStatus.APPROVED)).filter(f -> f.getPatient().getGenderCode().equals(Gender.female.name())).count())
//                    .emergencyTotal(form058s.stream().filter(f -> !f.getStatus().equals(FormStatus.APPROVED)).count())
//                    .emergencyAdults(form058s.stream()
//                            .filter(f -> !f.getStatus().equals(FormStatus.APPROVED))
//                            .filter(f -> isAdult(f.getPatient().getBirthDate())).count())
//                    .emergencyUnder18(form058s.stream()
//                            .filter(f -> !f.getStatus().equals(FormStatus.APPROVED))
//                            .filter(f -> !isAdult(f.getPatient().getBirthDate())).count())
//                    .emergencyFemales(form058s.stream().filter(f -> !f.getStatus().equals(FormStatus.APPROVED)).filter(f -> f.getPatient().getGenderCode().equals(Gender.female.name())).count())
//                    .children(children)
//                    .build());
//        } else if (level.equals(LevelStatus.region_level.name())) {
//            String regionCode = userCurrOrg.getState().getCode();
//
//            List<Form058> regionForms = form058s.stream()
//                    .filter(f -> f.getCreatedOrg().getState().getCode().equals(regionCode))
//                    .toList();
//
//            Map<String, List<Form058>> districtMap = regionForms.stream()
//                    .collect(Collectors.groupingBy(f -> f.getCreatedOrg().getCity().getCode()));
//
//            Set<Form1ReportDto> children = new HashSet<>();
//
//            for (Map.Entry<String, List<Form058>> districtEntry : districtMap.entrySet()) {
//                List<Form058> districtFormList = districtEntry.getValue();
//
//                String districtName = districtFormList.getFirst().getCreatedOrg().getName();
//
//                children.add(Form1ReportDto.builder()
//                        .name(districtName)
//                        .confirmedTotal(districtFormList.stream().filter(f -> f.getStatus().equals(FormStatus.APPROVED)).count())
//                        .confirmedAdults(districtFormList.stream()
//                                .filter(f -> f.getStatus().equals(FormStatus.APPROVED))
//                                .filter(f -> isAdult(f.getPatient().getBirthDate())).count())
//                        .confirmedUnder18(districtFormList.stream()
//                                .filter(f -> f.getStatus().equals(FormStatus.APPROVED))
//                                .filter(f -> !isAdult(f.getPatient().getBirthDate())).count())
//                        .confirmedFemales(districtFormList.stream().filter(f -> f.getStatus().equals(FormStatus.APPROVED)).filter(f -> f.getPatient().getGenderCode().equals(Gender.female.name())).count())
//                        .emergencyTotal(districtFormList.stream().filter(f -> !f.getStatus().equals(FormStatus.APPROVED)).count())
//                        .emergencyAdults(districtFormList.stream()
//                                .filter(f -> !f.getStatus().equals(FormStatus.APPROVED))
//                                .filter(f -> isAdult(f.getPatient().getBirthDate())).count())
//                        .emergencyUnder18(districtFormList.stream()
//                                .filter(f -> !f.getStatus().equals(FormStatus.APPROVED))
//                                .filter(f -> !isAdult(f.getPatient().getBirthDate())).count())
//                        .emergencyFemales(districtFormList.stream().filter(f -> !f.getStatus().equals(FormStatus.APPROVED)).filter(f -> f.getPatient().getGenderCode().equals(Gender.female.name())).count())
//                        .build());
//            }
//
//            reportList.add(Form1ReportDto.builder()
//                    .name(userCurrOrg.getState().getNameUz())
//                    .confirmedTotal(form058s.stream().filter(f -> f.getStatus().equals(FormStatus.APPROVED)).count())
//                    .confirmedAdults(form058s.stream()
//                            .filter(f -> f.getStatus().equals(FormStatus.APPROVED))
//                            .filter(f -> isAdult(f.getPatient().getBirthDate())).count())
//                    .confirmedUnder18(form058s.stream()
//                            .filter(f -> f.getStatus().equals(FormStatus.APPROVED))
//                            .filter(f -> !isAdult(f.getPatient().getBirthDate())).count())
//                    .confirmedFemales(form058s.stream().filter(f -> f.getStatus().equals(FormStatus.APPROVED)).filter(f -> f.getPatient().getGenderCode().equals(Gender.female.name())).count())
//                    .emergencyTotal(form058s.stream().filter(f -> !f.getStatus().equals(FormStatus.APPROVED)).count())
//                    .emergencyAdults(form058s.stream()
//                            .filter(f -> !f.getStatus().equals(FormStatus.APPROVED))
//                            .filter(f -> isAdult(f.getPatient().getBirthDate())).count())
//                    .emergencyUnder18(form058s.stream()
//                            .filter(f -> !f.getStatus().equals(FormStatus.APPROVED))
//                            .filter(f -> !isAdult(f.getPatient().getBirthDate())).count())
//                    .emergencyFemales(form058s.stream().filter(f -> !f.getStatus().equals(FormStatus.APPROVED)).filter(f -> f.getPatient().getGenderCode().equals(Gender.female.name())).count())
//                    .children(children)
//                    .build());
//        } else if (level.equals(LevelStatus.district_level.name())) {
//
//            List<Form058> districtForms = form058s.stream()
//                    .filter(f -> f.getCreatedOrg().getCity().getCode().equals(userCurrOrg.getCity().getCode()))
//                    .toList();
//
//
//            String districtName = userCurrOrg.getCity().getNameUz();
//            reportList.add(Form1ReportDto.builder()
//                    .name(districtName)
//                    .confirmedTotal(districtForms.stream().filter(f -> f.getStatus().equals(FormStatus.APPROVED)).count())
//                    .confirmedAdults(districtForms.stream()
//                            .filter(f -> f.getStatus().equals(FormStatus.APPROVED))
//                            .filter(f -> isAdult(f.getPatient().getBirthDate())).count())
//                    .confirmedUnder18(districtForms.stream()
//                            .filter(f -> f.getStatus().equals(FormStatus.APPROVED))
//                            .filter(f -> !isAdult(f.getPatient().getBirthDate())).count())
//                    .confirmedFemales(districtForms.stream().filter(f -> f.getStatus().equals(FormStatus.APPROVED)).filter(f -> f.getPatient().getGenderCode().equals(Gender.female.name())).count())
//                    .emergencyTotal(districtForms.stream().filter(f -> !f.getStatus().equals(FormStatus.APPROVED)).count())
//                    .emergencyAdults(districtForms.stream()
//                            .filter(f -> !f.getStatus().equals(FormStatus.APPROVED))
//                            .filter(f -> isAdult(f.getPatient().getBirthDate())).count())
//                    .emergencyUnder18(districtForms.stream()
//                            .filter(f -> !f.getStatus().equals(FormStatus.APPROVED))
//                            .filter(f -> !isAdult(f.getPatient().getBirthDate())).count())
//                    .emergencyFemales(districtForms.stream().filter(f -> !f.getStatus().equals(FormStatus.APPROVED)).filter(f -> f.getPatient().getGenderCode().equals(Gender.female.name())).count())
//                    .build());
//        }
//        return new ApiResponse<>(true, MessageUtil.OPERATION_SUCCESS, reportList);
//    }

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
                                    userOrg.getState().getCode())));

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
