package uz.uzinfocom.isemid.services.impls;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.models.dtos.Form1ReportDTO;
import uz.uzinfocom.isemid.models.entities.Form058;
import uz.uzinfocom.isemid.models.entities.Organization;
import uz.uzinfocom.isemid.models.entities.User;
import uz.uzinfocom.isemid.models.entities.enums.FormStatus;
import uz.uzinfocom.isemid.models.entities.enums.Gender;
import uz.uzinfocom.isemid.models.entities.enums.LevelStatus;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.repositories.Form058Repository;
import uz.uzinfocom.isemid.services.Form058ReportService;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static uz.uzinfocom.isemid.utils.CommonUtils.isAdult;

@Service
@RequiredArgsConstructor
public class Form058ReportServiceImpl implements Form058ReportService {
    private final Form058Repository form058Repository;

    @Override
    public ApiResponse<?> reportForm1(LocalDate fromDate, LocalDate toDate, LocalDate birthDate, Gender gender, User currentUser) {
        Specification<Form058> specification = filterByCriteria(fromDate, toDate, birthDate, gender, currentUser);
        List<Form058> form058s = form058Repository.findAll(specification);
        Organization userCurrOrg = currentUser.getCurrentOrganization();
        String level = userCurrOrg.getLevel().getCode();
        List<Form1ReportDTO> reportList = new LinkedList<>();

        if (level.equals(LevelStatus.republic_level.name())) {

            List<Form058> regionForms = form058s.stream().toList();

            Map<String, List<Form058>> districtMap = regionForms.stream()
                    .collect(Collectors.groupingBy(f -> f.getCreatedOrg().getState().getCode()));

            Set<Form1ReportDTO> children = new HashSet<>();

            for (Map.Entry<String, List<Form058>> regions : districtMap.entrySet()) {
                List<Form058> regionsFormList = regions.getValue();

                String regionName = regionsFormList.getFirst().getCreatedOrg().getName();
                String regionCode = regionsFormList.getFirst().getCreatedOrg().getState().getCode(); // region code

                Map<String, List<Form058>> cityMap = regionsFormList.stream()
                        .filter(f -> f.getCreatedOrg().getCity().getParentCode().equals(regionCode)) // faqat shu regionga tegishli tumanni oladi
                        .collect(Collectors.groupingBy(f -> f.getCreatedOrg().getCity().getCode())); // tuman kodlari bo‘yicha grouping

                Set<Form1ReportDTO> districtChildren = new HashSet<>();


                for (Map.Entry<String, List<Form058>> cityEntry : cityMap.entrySet()) {
                    List<Form058> cityForms = cityEntry.getValue();
                    String districtName = cityForms.getFirst().getCreatedOrg().getName(); // tuman nomi

                    districtChildren.add(Form1ReportDTO.builder()
                            .name(districtName)
                            .confirmedTotal(cityForms.stream().filter(f -> f.getStatus().equals(FormStatus.APPROVED)).count())
                            .confirmedAdults(cityForms.stream().filter(f -> f.getStatus().equals(FormStatus.APPROVED))
                                    .filter(f -> isAdult(f.getPatient().getBirthDate())).count())
                            .confirmedUnder18(cityForms.stream().filter(f -> f.getStatus().equals(FormStatus.APPROVED))
                                    .filter(f -> !isAdult(f.getPatient().getBirthDate())).count())
                            .confirmedFemales(cityForms.stream().filter(f -> f.getStatus().equals(FormStatus.APPROVED))
                                    .filter(f -> f.getPatient().getGender().getCode().equals(Gender.female.name())).count())
                            .emergencyTotal(cityForms.stream().filter(f -> !f.getStatus().equals(FormStatus.APPROVED)).count())
                            .emergencyAdults(cityForms.stream().filter(f -> !f.getStatus().equals(FormStatus.APPROVED))
                                    .filter(f -> isAdult(f.getPatient().getBirthDate())).count())
                            .emergencyUnder18(cityForms.stream().filter(f -> !f.getStatus().equals(FormStatus.APPROVED))
                                    .filter(f -> !isAdult(f.getPatient().getBirthDate())).count())
                            .emergencyFemales(cityForms.stream().filter(f -> !f.getStatus().equals(FormStatus.APPROVED))
                                    .filter(f -> f.getPatient().getGender().getCode().equals(Gender.female.name())).count())
                            .build());
                }



                children.add(Form1ReportDTO.builder()
                        .name(regionName)
                        .confirmedTotal(regionsFormList.stream().filter(f -> f.getStatus().equals(FormStatus.APPROVED)).count())
                        .confirmedAdults(regionsFormList.stream()
                                .filter(f -> f.getStatus().equals(FormStatus.APPROVED))
                                .filter(f -> isAdult(f.getPatient().getBirthDate())).count())
                        .confirmedUnder18(regionsFormList.stream()
                                .filter(f -> f.getStatus().equals(FormStatus.APPROVED))
                                .filter(f -> !isAdult(f.getPatient().getBirthDate())).count())
                        .confirmedFemales(regionsFormList.stream().filter(f -> f.getStatus().equals(FormStatus.APPROVED)).filter(f -> f.getPatient().getGender().getCode().equals(Gender.female.name())).count())
                        .emergencyTotal(regionsFormList.stream().filter(f -> !f.getStatus().equals(FormStatus.APPROVED)).count())
                        .emergencyAdults(regionsFormList.stream()
                                .filter(f -> !f.getStatus().equals(FormStatus.APPROVED))
                                .filter(f -> isAdult(f.getPatient().getBirthDate())).count())
                        .emergencyUnder18(regionsFormList.stream()
                                .filter(f -> !f.getStatus().equals(FormStatus.APPROVED))
                                .filter(f -> !isAdult(f.getPatient().getBirthDate())).count())
                        .emergencyFemales(regionsFormList.stream().filter(f -> !f.getStatus().equals(FormStatus.APPROVED)).filter(f -> f.getPatient().getGender().getCode().equals(Gender.female.name())).count())
                                .children(districtChildren)
                        .build());
            }

            reportList.add(Form1ReportDTO.builder()
                    .name("O'zbekiston Respublikasi")
                    .confirmedTotal(form058s.stream().filter(f -> f.getStatus().equals(FormStatus.APPROVED)).count())
                    .confirmedAdults(form058s.stream()
                            .filter(f -> f.getStatus().equals(FormStatus.APPROVED))
                            .filter(f -> isAdult(f.getPatient().getBirthDate())).count())
                    .confirmedUnder18(form058s.stream()
                            .filter(f -> f.getStatus().equals(FormStatus.APPROVED))
                            .filter(f -> !isAdult(f.getPatient().getBirthDate())).count())
                    .confirmedFemales(form058s.stream().filter(f -> f.getStatus().equals(FormStatus.APPROVED)).filter(f -> f.getPatient().getGender().getCode().equals(Gender.female.name())).count())
                    .emergencyTotal(form058s.stream().filter(f -> !f.getStatus().equals(FormStatus.APPROVED)).count())
                    .emergencyAdults(form058s.stream()
                            .filter(f -> !f.getStatus().equals(FormStatus.APPROVED))
                            .filter(f -> isAdult(f.getPatient().getBirthDate())).count())
                    .emergencyUnder18(form058s.stream()
                            .filter(f -> !f.getStatus().equals(FormStatus.APPROVED))
                            .filter(f -> !isAdult(f.getPatient().getBirthDate())).count())
                    .emergencyFemales(form058s.stream().filter(f -> !f.getStatus().equals(FormStatus.APPROVED)).filter(f -> f.getPatient().getGender().getCode().equals(Gender.female.name())).count())
                    .children(children)
                    .build());
        } else if (level.equals(LevelStatus.region_level.name())) {
            String regionCode = userCurrOrg.getState().getCode();

            List<Form058> regionForms = form058s.stream()
                    .filter(f -> f.getCreatedOrg().getState().getCode().equals(regionCode))
                    .toList();

            Map<String, List<Form058>> districtMap = regionForms.stream()
                    .collect(Collectors.groupingBy(f -> f.getCreatedOrg().getCity().getCode()));

            Set<Form1ReportDTO> children = new HashSet<>();

            for (Map.Entry<String, List<Form058>> districtEntry : districtMap.entrySet()) {
                List<Form058> districtFormList = districtEntry.getValue();

                String districtName = districtFormList.getFirst().getCreatedOrg().getName();

                children.add(Form1ReportDTO.builder()
                        .name(districtName)
                        .confirmedTotal(districtFormList.stream().filter(f -> f.getStatus().equals(FormStatus.APPROVED)).count())
                        .confirmedAdults(districtFormList.stream()
                                .filter(f -> f.getStatus().equals(FormStatus.APPROVED))
                                .filter(f -> isAdult(f.getPatient().getBirthDate())).count())
                        .confirmedUnder18(districtFormList.stream()
                                .filter(f -> f.getStatus().equals(FormStatus.APPROVED))
                                .filter(f -> !isAdult(f.getPatient().getBirthDate())).count())
                        .confirmedFemales(districtFormList.stream().filter(f -> f.getStatus().equals(FormStatus.APPROVED)).filter(f -> f.getPatient().getGender().getCode().equals(Gender.female.name())).count())
                        .emergencyTotal(districtFormList.stream().filter(f -> !f.getStatus().equals(FormStatus.APPROVED)).count())
                        .emergencyAdults(districtFormList.stream()
                                .filter(f -> !f.getStatus().equals(FormStatus.APPROVED))
                                .filter(f -> isAdult(f.getPatient().getBirthDate())).count())
                        .emergencyUnder18(districtFormList.stream()
                                .filter(f -> !f.getStatus().equals(FormStatus.APPROVED))
                                .filter(f -> !isAdult(f.getPatient().getBirthDate())).count())
                        .emergencyFemales(districtFormList.stream().filter(f -> !f.getStatus().equals(FormStatus.APPROVED)).filter(f -> f.getPatient().getGender().getCode().equals(Gender.female.name())).count())
                        .build());
            }

            reportList.add(Form1ReportDTO.builder()
                    .name(userCurrOrg.getState().getNameUz())
                    .confirmedTotal(form058s.stream().filter(f -> f.getStatus().equals(FormStatus.APPROVED)).count())
                    .confirmedAdults(form058s.stream()
                            .filter(f -> f.getStatus().equals(FormStatus.APPROVED))
                            .filter(f -> isAdult(f.getPatient().getBirthDate())).count())
                    .confirmedUnder18(form058s.stream()
                            .filter(f -> f.getStatus().equals(FormStatus.APPROVED))
                            .filter(f -> !isAdult(f.getPatient().getBirthDate())).count())
                    .confirmedFemales(form058s.stream().filter(f -> f.getStatus().equals(FormStatus.APPROVED)).filter(f -> f.getPatient().getGender().getCode().equals(Gender.female.name())).count())
                    .emergencyTotal(form058s.stream().filter(f -> !f.getStatus().equals(FormStatus.APPROVED)).count())
                    .emergencyAdults(form058s.stream()
                            .filter(f -> !f.getStatus().equals(FormStatus.APPROVED))
                            .filter(f -> isAdult(f.getPatient().getBirthDate())).count())
                    .emergencyUnder18(form058s.stream()
                            .filter(f -> !f.getStatus().equals(FormStatus.APPROVED))
                            .filter(f -> !isAdult(f.getPatient().getBirthDate())).count())
                    .emergencyFemales(form058s.stream().filter(f -> !f.getStatus().equals(FormStatus.APPROVED)).filter(f -> f.getPatient().getGender().getCode().equals(Gender.female.name())).count())
                    .children(children)
                    .build());
        } else if (level.equals(LevelStatus.district_level.name())) {

            List<Form058> districtForms = form058s.stream()
                    .filter(f -> f.getCreatedOrg().getCity().getCode().equals(userCurrOrg.getCity().getCode()))
                    .toList();


            String districtName = userCurrOrg.getCity().getNameUz();
            reportList.add(Form1ReportDTO.builder()
                    .name(districtName)
                    .confirmedTotal(districtForms.stream().filter(f -> f.getStatus().equals(FormStatus.APPROVED)).count())
                    .confirmedAdults(districtForms.stream()
                            .filter(f -> f.getStatus().equals(FormStatus.APPROVED))
                            .filter(f -> isAdult(f.getPatient().getBirthDate())).count())
                    .confirmedUnder18(districtForms.stream()
                            .filter(f -> f.getStatus().equals(FormStatus.APPROVED))
                            .filter(f -> !isAdult(f.getPatient().getBirthDate())).count())
                    .confirmedFemales(districtForms.stream().filter(f -> f.getStatus().equals(FormStatus.APPROVED)).filter(f -> f.getPatient().getGender().getCode().equals(Gender.female.name())).count())
                    .emergencyTotal(districtForms.stream().filter(f -> !f.getStatus().equals(FormStatus.APPROVED)).count())
                    .emergencyAdults(districtForms.stream()
                            .filter(f -> !f.getStatus().equals(FormStatus.APPROVED))
                            .filter(f -> isAdult(f.getPatient().getBirthDate())).count())
                    .emergencyUnder18(districtForms.stream()
                            .filter(f -> !f.getStatus().equals(FormStatus.APPROVED))
                            .filter(f -> !isAdult(f.getPatient().getBirthDate())).count())
                    .emergencyFemales(districtForms.stream().filter(f -> !f.getStatus().equals(FormStatus.APPROVED)).filter(f -> f.getPatient().getGender().getCode().equals(Gender.female.name())).count())
                    .build());
        }

        return new ApiResponse<>(true, "success", reportList);
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

            if (userOrg != null && userOrg.getLevel() != null) {
                String userLevelCode = userOrg.getLevel().getCode();

                switch (userLevelCode) {
                    case "republic_level" -> {
                    }
                    case "region_level" -> predicates.add(cb.and(
                            cb.equal(root.get("createdOrg").get("state").get("code"),
                                    userOrg.getState().getCode())
//                            cb.equal(root.get("createdOrg").get("level").get("code"), "region_level"),
//                            cb.equal(root.get("createdOrg").get("city").get("parentCode"), userOrg.getState().getCode())
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
