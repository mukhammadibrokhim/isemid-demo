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
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.repositories.Form058Repository;
import uz.uzinfocom.isemid.services.Form058ReportService;
import uz.uzinfocom.isemid.services.value_set.CatalogService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class Form058ReportServiceImpl implements Form058ReportService {
    private final Form058Repository form058Repository;
    private final CatalogService catalogService;


    @Override
    public ApiResponse<?> reportForm1(LocalDate fromDate, LocalDate toDate, LocalDate birthDate, Gender gender, User currentUser) {
        Specification<Form058> spec = filterByCriteria(fromDate, toDate, birthDate, gender, currentUser);
        List<Form058> form058List = form058Repository.findAll(spec);
        List<Form058> confirmed058List = form058List.stream()
                .filter(form058 -> form058.getStatus().equals(FormStatus.APPROVED))
                .toList();

        Set<Form1ReportDTO> reports = new HashSet<>();

        String userLevel = currentUser.getCurrentOrganization().getLevel().getCode();

        for (Form058 form058 : form058List) {
            Organization org = form058.getCreatedOrg();
            String level = org.getLevel().getCode();

            if (level.equals(userLevel)) {
                String regionCode = org.getState().getCode();
                String districtCode = org.getCity() != null ? org.getCity().getCode() : null;

                // Hisobotni yaratish
                Form1ReportDTO dto = Form1ReportDTO.builder()
                        .level(level)
                        .code(regionCode)
                        .name(org.getState())
                        .confirmedTotal(confirmed058List.size())
                        .emergencyTotal(form058List.size())
                        .build();

                // Agar daraja "republic_level" bo'lsa, barcha regionlarni bolalar sifatida qo'shish
                if ("republic_level".equals(level)) {
                    Set<String> regionCodes = new HashSet<>();
                    for (Form058 subForm : form058List) {
                        String subRegionCode = subForm.getCreatedOrg().getState().getCode();
                        if (!regionCodes.contains(subRegionCode)) {
                            regionCodes.add(subRegionCode);

                            Form1ReportDTO regionDto = Form1ReportDTO.builder()
                                    .level("region_level")
                                    .code(subRegionCode)
                                    .name(subForm.getCreatedOrg().getState())
                                    .confirmedTotal(confirmed058List.size())
                                    .emergencyTotal(form058List.size())
                                    .build();

                            // Region uchun districtlarni qo'shish
                            Set<Form1ReportDTO> children = new HashSet<>();
                            for (Form058 districtForm : confirmed058List) {
                                if (districtForm.getCreatedOrg().getLevel().getCode().equals("district_level") &&
                                        districtForm.getCreatedOrg().getState().getCode().equals(subRegionCode)) {
                                    Form1ReportDTO districtDto = Form1ReportDTO.builder()
                                            .level("district_level")
                                            .code(districtForm.getCreatedOrg().getCity().getCode())
                                            .name(districtForm.getCreatedOrg().getCity())
                                            .confirmedTotal(confirmed058List.size())
                                            .emergencyTotal(form058List.size())
                                            .build();
                                    children.add(districtDto);
                                }
                            }
                            regionDto.setChildren(children);
                            reports.add(regionDto);
                        }
                    }
                } else if ("region_level".equals(level)) {
                    // Region darajasidagi districtlarni qo'shish
                    Set<Form1ReportDTO> children = new HashSet<>();
                    for (Form058 districtForm : form058List) {
                        if (districtForm.getCreatedOrg().getLevel().getCode().equals("district_level") &&
                                districtForm.getCreatedOrg().getState().getCode().equals(org.getState().getCode())) {
                            Form1ReportDTO districtDto = Form1ReportDTO.builder()
                                    .level("district_level")
                                    .code(districtForm.getCreatedOrg().getCity().getCode())
                                    .name(districtForm.getCreatedOrg().getCity())
                                    .confirmedTotal(confirmed058List.size())
                                    .emergencyTotal(form058List.size())
                                    .build();
                            children.add(districtDto);
                        }
                    }
                    dto.setChildren(children);
                }

                reports.add(dto);
            }
        }
        return new ApiResponse<>(true, "Success", reports);
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
                    case "region_level" -> {
                        predicates.add(cb.equal(root.get("createdOrg").get("state").get("code"),
                                userOrg.getState().getCode()));
                    }
                    case "district_level" -> {
                        predicates.add(cb.equal(root.get("createdOrg").get("city").get("code"),
                                userOrg.getCity().getCode()));
                    }
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

}
