package uz.uzinfocom.isemid.services.impls;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.models.dtos.Form1ReportDTO;
import uz.uzinfocom.isemid.models.entities.Form058;
import uz.uzinfocom.isemid.models.entities.Organization;
import uz.uzinfocom.isemid.models.entities.User;
import uz.uzinfocom.isemid.models.entities.enums.Gender;
import uz.uzinfocom.isemid.models.entities.enums.LevelStatus;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.repositories.Form058Repository;
import uz.uzinfocom.isemid.services.Form058ReportService;
import uz.uzinfocom.isemid.services.value_set.CatalogService;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class Form058ReportServiceImpl implements Form058ReportService {
    private final Form058Repository form058Repository;
    private final CatalogService catalogService;


    @Override
    public ApiResponse<?> reportForm1(LocalDate fromDate, LocalDate toDate, LocalDate birthDate, Gender gender, User currentUser) {
        Specification<Form058> specification = filterByCriteria(fromDate, toDate, birthDate, gender, currentUser);
        List<Form058> form058s = form058Repository.findAll(specification);
        Organization userCurrOrg = currentUser.getCurrentOrganization();
        String level = userCurrOrg.getLevel().getCode();
        List<Form1ReportDTO> reportList = new LinkedList<>();

        if (level.equals(LevelStatus.republic_level.name())) {

            // Group by regionCode (UZ-SI, UZ-AN, ...)
            Map<String, List<Form058>> regionMap = form058s.stream()
                    .filter(f -> f.getCreatedOrg().getLevel().getCode().equals(LevelStatus.region_level.name()))
                    .collect(Collectors.groupingBy(f -> f.getCreatedOrg().getState().getCode()));

            for (Map.Entry<String, List<Form058>> regionEntry : regionMap.entrySet()) {
                String regionCode = regionEntry.getKey();
                List<Form058> regionForms = regionEntry.getValue();
                String regionName = regionForms.get(0).getCreatedOrg().getState().getNameUz();
                long regionFormCount = regionForms.size();

                // For district report (district-level)
                List<Form058> districtForms = form058s.stream()
                        .filter(f -> f.getCreatedOrg().getLevel().getCode().equals(LevelStatus.district_level.name()))
                        .filter(f -> f.getCreatedOrg().getCity() != null)
                        .filter(f -> regionCode.equals(f.getCreatedOrg().getCity().getParentCode()))
                        .toList();

                Map<String, List<Form058>> districtMap = districtForms.stream()
                        .collect(Collectors.groupingBy(f -> f.getCreatedOrg().getCity().getCode()));

                Set<Form1ReportDTO> children = new HashSet<>();
                for (Map.Entry<String, List<Form058>> districtEntry : districtMap.entrySet()) {
                    String districtCode = districtEntry.getKey();
                    List<Form058> districtFormList = districtEntry.getValue();
                    String districtName = districtFormList.get(0).getCreatedOrg().getCity().getNameUz();
                    long formCount = districtFormList.size();

                    children.add(Form1ReportDTO.builder()
                            .name(districtName)
                            .build());
                }

                long districtFormCount = districtForms.size();
                long total = regionFormCount + districtFormCount;

                reportList.add(Form1ReportDTO.builder()
                        .name(regionName)
                        .children(children)
                        .build());
            }
        } else if (level.equals(LevelStatus.region_level.name())) {
            String regionCode = userCurrOrg.getState().getCode();

            List<Form058> regionForms = form058s.stream()
                    .filter(f -> f.getCreatedOrg().getState().getCode().equals(regionCode))
                    .toList();

            Map<String, List<Form058>> districtMap = form058s.stream()
                    .filter(f -> f.getCreatedOrg().getLevel().getCode().equals(LevelStatus.district_level.name()))
                    .collect(Collectors.groupingBy(f -> f.getCreatedOrg().getCity().getCode()));

            Set<Form1ReportDTO> children = new HashSet<>();

            for (Map.Entry<String, List<Form058>> districtEntry : districtMap.entrySet()) {
                String districtCode = districtEntry.getKey();
                List<Form058> districtFormList = districtEntry.getValue();

                String districtName = districtFormList.get(0).getCreatedOrg().getCity().getNameUz();

                children.add(Form1ReportDTO.builder()
                        .name(districtName)
                        .build());
            }

            reportList.add(Form1ReportDTO.builder()
                    .name(userCurrOrg.getState().getNameUz())  // Region name
                    .emergencyTotal((int) form058s.stream()
                            .filter(f -> f.getCreatedOrg().getState().getCode().equals(currentUser.getCurrentOrganization().getState().getCode()))
                            .count())  // Total number of records for the region
                    .children(children)  // Add districts as children
                    .build());
        } else if (level.equals(LevelStatus.district_level.name())) {

            List<Form058> districtForms = form058s.stream()
                    .filter(f -> f.getCreatedOrg().getCity().getCode().equals(userCurrOrg.getCity().getCode()))
                    .toList();


            String districtName = userCurrOrg.getCity().getNameUz();
            reportList.add(Form1ReportDTO.builder()
                    .name(districtName)
                            .emergencyTotal(districtForms.size())
                    .build());
        }

        return new ApiResponse<>(true, "Viloyatlar + tumanlar bo‘yicha Form058 hisobot", reportList);
    }

//    @Override
//    public ApiResponse<?> reportForm1(LocalDate fromDate, LocalDate toDate, LocalDate birthDate, Gender gender, User currentUser) {
//        Specification<Form058> spec = filterByCriteria(fromDate, toDate, birthDate, gender, currentUser);
//        List<Form058> form058List = form058Repository.findAll(spec);
//        List<Form058> confirmed058List = form058List.stream()
//                .filter(form058 -> form058.getStatus().equals(FormStatus.APPROVED))
//                .toList();
//
//        Set<Form1ReportDTO> reports = new HashSet<>();
//
//        String userLevel = currentUser.getCurrentOrganization().getLevel().getCode();
//
//        for (Form058 form058 : form058List) {
//            Organization org = form058.getCreatedOrg();
//            String level = org.getLevel().getCode();
//
//            if (level.equals(userLevel)) {
//                String regionCode = org.getState().getCode();
//                String districtCode = org.getCity() != null ? org.getCity().getCode() : null;
//
//                Form1ReportDTO dto = Form1ReportDTO.builder()
//                        .level(level)
//                        .code(regionCode)
//                        .name(org.getState())
//                        .confirmedTotal(confirmed058List.size())
//                        .emergencyTotal(form058List.size())
//                        .build();
//
//                // Agar daraja "republic_level" bo'lsa, barcha regionlarni bolalar sifatida qo'shish
//                if ("republic_level".equals(level)) {
//                    Set<String> regionCodes = new HashSet<>();
//                    for (Form058 subForm : form058List) {
//                        String subRegionCode = subForm.getCreatedOrg().getState().getCode();
//                        if (!regionCodes.contains(subRegionCode)) {
//                            regionCodes.add(subRegionCode);
//
//                            Form1ReportDTO regionDto = Form1ReportDTO.builder()
//                                    .level("region_level")
//                                    .code(subRegionCode)
//                                    .name(subForm.getCreatedOrg().getState())
//                                    .confirmedTotal(confirmed058List.size())
//                                    .emergencyTotal(form058List.size())
//                                    .build();
//
//                            // Region uchun districtlarni qo'shish
//                            Set<Form1ReportDTO> children = new HashSet<>();
//                            for (Form058 districtForm : confirmed058List) {
//                                if (districtForm.getCreatedOrg().getLevel().getCode().equals("district_level") &&
//                                        districtForm.getCreatedOrg().getState().getCode().equals(subRegionCode)) {
//                                    Form1ReportDTO districtDto = Form1ReportDTO.builder()
//                                            .level("district_level")
//                                            .code(districtForm.getCreatedOrg().getCity().getCode())
//                                            .name(districtForm.getCreatedOrg().getCity())
//                                            .confirmedTotal(confirmed058List.size())
//                                            .emergencyTotal(form058List.size())
//                                            .build();
//                                    children.add(districtDto);
//                                }
//                            }
//                            regionDto.setChildren(children);
//                            reports.add(regionDto);
//                        }
//                    }
//                } else if ("region_level".equals(level)) {
//                    // Region darajasidagi districtlarni qo'shish
//                    Set<Form1ReportDTO> children = new HashSet<>();
//                    for (Form058 districtForm : form058List) {
//                        if (districtForm.getCreatedOrg().getLevel().getCode().equals("district_level") &&
//                                districtForm.getCreatedOrg().getState().getCode().equals(org.getState().getCode())) {
//                            Form1ReportDTO districtDto = Form1ReportDTO.builder()
//                                    .level("district_level")
//                                    .code(districtForm.getCreatedOrg().getCity().getCode())
//                                    .name(districtForm.getCreatedOrg().getCity())
//                                    .confirmedTotal(confirmed058List.size())
//                                    .emergencyTotal(form058List.size())
//                                    .build();
//                            children.add(districtDto);
//                        }
//                    }
//                    dto.setChildren(children);
//                }
//
//                reports.add(dto);
//            }
//        }
//        return new ApiResponse<>(true, "Success", reports);
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
