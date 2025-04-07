package uz.uzinfocom.isemid.services.impls;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.models.dtos.Form058ReportDto;
import uz.uzinfocom.isemid.models.entities.Form058;
import uz.uzinfocom.isemid.models.entities.Organization;
import uz.uzinfocom.isemid.models.entities.Patient;
import uz.uzinfocom.isemid.models.entities.User;
import uz.uzinfocom.isemid.models.entities.enums.FormStatus;
import uz.uzinfocom.isemid.models.entities.enums.Gender;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.repositories.Form058Repository;
import uz.uzinfocom.isemid.services.Form058ReportService;
import uz.uzinfocom.isemid.services.value_set.CatalogService;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

import static uz.uzinfocom.isemid.models.entities.enums.Gender.female;

@Service
@RequiredArgsConstructor
public class Form058ReportServiceImpl implements Form058ReportService {
    private final Form058Repository form058Repository;
    private final CatalogService catalogService;

    @Override
    public ApiResponse<?> reportForm6(LocalDate fromDate, LocalDate toDate, LocalDate birthDate, Gender gender, User currentUser) {
        Specification<Form058> specification = filterByCriteria(null, fromDate, toDate, birthDate, gender, currentUser);
        List<Form058> filteredForms = form058Repository.findAll(specification);
        List<Form058ReportDto> report = buildHierarchicalReport(filteredForms, fromDate, toDate, currentUser);
        return new ApiResponse<>(true, "Success", report);
    }

    private List<Form058ReportDto> buildHierarchicalReport(List<Form058> filteredForms, LocalDate fromDate, LocalDate toDate, User currentUser) {
        Organization userOrg = currentUser.getCurrentOrganization();
        String level = userOrg.getLevel().getCode(); // Masalan: REPUBLIC, REGION, DISTRICT

        // Org nomini, viloyatini va tumanini olish
        String orgName = userOrg.getName();
        String orgState = userOrg.getState().getNameUz() != null ? userOrg.getState().getNameUz() : null;
        String orgCity = userOrg.getCity().getCode() != null ? userOrg.getCity().getNameUz() : null;

        // Foydalanuvchi darajasiga moslashtirilgan formalarni filtrlaymiz
        List<Form058> filteredByLevel = filteredForms.stream()
                .filter(form -> {
                    Organization formOrg = form.getCreatedOrg(); // Formaning tashkiloti
                    if (level.equals("republic_level")) {
                        return true; // Republic darajasi uchun barcha formalarni olish
                    }
                    if (level.equals("region_level")) {
                        return formOrg.getState().getCode().equals(userOrg.getState().getCode()); // Region darajasida faqat o'z viloyatidagi formalarni olish
                    }
                    if (level.equals("district_level")) {
                        // Tuman darajasida faqat o'z tumandagi formalarni olish
                        // City null bo'lsa, false qaytaramiz
                        if (formOrg.getCity() != null && userOrg.getCity() != null) {
                            return formOrg.getCity().getCode().equals(userOrg.getCity().getCode()); // Shahar kodlarini solishtirish
                        }
                        return false; // Agar formOrg.getCity() yoki userOrg.getCity() null bo'lsa
                    }
                    return false;
                }).toList();



        int confirmedTotal = 0, confirmedAge0To17 = 0, confirmedAge18Plus = 0, confirmedFemaleCount = 0;
        int total = 0, age0To17 = 0, age18Plus = 0, femaleCount = 0;

        for (Form058 form : filteredByLevel) {
            Patient patient = form.getPatient();
            int age = Period.between(patient.getBirthDate(), LocalDate.now()).getYears();
            boolean isFemale = patient.getGender().getCode().equals(female.name());

            // Barchasi
            total++;
            if (age <= 17) age0To17++;
            else age18Plus++;
            if (isFemale) femaleCount++;

            // Faqat tasdiqlanganlar
            if (form.getStatus().equals(FormStatus.APPROVED)) {
                confirmedTotal++;
                if (age <= 17) confirmedAge0To17++;
                else confirmedAge18Plus++;
                if (isFemale) confirmedFemaleCount++;
            }
        }

        Form058ReportDto reportDto = new Form058ReportDto();
        reportDto.setOrgName(orgName);
        reportDto.setStateCode(userOrg.getState().getCode());
        reportDto.setOrgState(orgState);
        reportDto.setOrgCity(orgCity);
        reportDto.setLevel(level);
        reportDto.setReportDate(LocalDate.now());
        reportDto.setDateFrom(fromDate);
        reportDto.setDateTo(toDate);

        reportDto.setTotal(total);
        reportDto.setAge0To17(age0To17);
        reportDto.setAge18Plus(age18Plus);
        reportDto.setFemaleCount(femaleCount);

        reportDto.setConfirmedTotal(confirmedTotal);
        reportDto.setConfirmedAge0To17(confirmedAge0To17);
        reportDto.setConfirmedAge18Plus(confirmedAge18Plus);
        reportDto.setConfirmedFemaleCount(confirmedFemaleCount);

        return List.of(reportDto); // Faqat bitta hisobot
    }


    @Override
    public ApiResponse<?> reportForRegion(LocalDate fromDate, LocalDate toDate, LocalDate birthDate, Gender gender, User currentUser) {
        if (!"republic_level".equals(currentUser.getCurrentOrganization().getLevel().getCode())&& !"region_level".equals(currentUser.getCurrentOrganization().getLevel().getCode())) {
            return new ApiResponse<>(false, "Access denied. This endpoint is for republic level region level.", null);
        }
        List<Form058> filteredForms = form058Repository.findAll(filterByCriteria(null, fromDate, toDate, birthDate, gender, currentUser));
        List<Form058ReportDto> report = buildHierarchicalReportForAllRegions(filteredForms, fromDate, toDate, currentUser);
        return new ApiResponse<>(true, "Success", report);
    }

    @Override
    public ApiResponse<?> reportForDistrictByRegionCode(String regionCode,LocalDate fromDate, LocalDate toDate, LocalDate birthDate, Gender gender, User currentUser){
        if (!"republic_level".equals(currentUser.getCurrentOrganization().getLevel().getCode())&& !"region_level".equals(currentUser.getCurrentOrganization().getLevel().getCode())) {
            return new ApiResponse<>(false, "Access denied. This endpoint is for republic level and region level.", null);
        }
        List<Form058> filteredForms = form058Repository.findAll(filterByCriteria(regionCode, fromDate, toDate,birthDate,gender, currentUser));
        List<Form058ReportDto> report = buildHierarchicalReportForDistricts(filteredForms, fromDate, toDate, currentUser);
        return new ApiResponse<>(true, "Success", report);
    }

    private List<Form058ReportDto> buildHierarchicalReportForDistricts(List<Form058> filteredForms, LocalDate fromDate, LocalDate toDate, User currentUser) {
        Organization userOrg = currentUser.getCurrentOrganization();

        List<Form058ReportDto> reportDtos = new ArrayList<>();

        filteredForms.stream()
                .map(Form058::getCreatedOrg) // Formadagi tashkilotni olish
                .filter(org -> "district_level".equals(org.getLevel().getCode())) // Faqat district darajasi
                .distinct() // Yagona districtlarni olish
                .forEach(districtOrg -> {
                    // Har bir district uchun hisobotni yaratish
                    Form058ReportDto reportDto = new Form058ReportDto();
                    reportDto.setStateCode(userOrg.getState().getCode());
                    reportDto.setOrgName(districtOrg.getName());
                    reportDto.setOrgState(districtOrg.getState().getNameUz());
                    reportDto.setOrgCity(districtOrg.getCity().getNameUz());
                    reportDto.setLevel(districtOrg.getLevel().getCode());
                    reportDto.setReportDate(LocalDate.now());
                    reportDto.setDateFrom(fromDate);
                    reportDto.setDateTo(toDate);

                    // District uchun hisobot ma'lumotlarini to‘plash
                    accumulateReportDataForDistrict(reportDto, filteredForms, districtOrg);

                    reportDtos.add(reportDto);
                });

        return reportDtos;
    }

    private void accumulateReportDataForDistrict(Form058ReportDto dto, List<Form058> filteredForms, Organization districtOrg) {
        int confirmedTotal = 0, confirmedAge0To17 = 0, confirmedAge18Plus = 0, confirmedFemaleCount = 0;
        int total = 0, age0To17 = 0, age18Plus = 0, femaleCount = 0;

        for (Form058 form : filteredForms) {
            Organization formOrg = form.getCreatedOrg();
            if (formOrg.equals(districtOrg)) {
                Patient patient = form.getPatient();
                int age = Period.between(patient.getBirthDate(), LocalDate.now()).getYears();
                boolean isFemale = patient.getGender().getCode().equals(Gender.female.name());

                // Barchasi
                total++;
                if (age <= 17) age0To17++;
                else age18Plus++;
                if (isFemale) femaleCount++;

                // Faqat tasdiqlanganlar
                if (form.getStatus().equals(FormStatus.APPROVED)) {
                    confirmedTotal++;
                    if (age <= 17) confirmedAge0To17++;
                    else confirmedAge18Plus++;
                    if (isFemale) confirmedFemaleCount++;
                }
            }
        }

        dto.setTotal(total);
        dto.setAge0To17(age0To17);
        dto.setAge18Plus(age18Plus);
        dto.setFemaleCount(femaleCount);

        dto.setConfirmedTotal(confirmedTotal);
        dto.setConfirmedAge0To17(confirmedAge0To17);
        dto.setConfirmedAge18Plus(confirmedAge18Plus);
        dto.setConfirmedFemaleCount(confirmedFemaleCount);
    }




    private List<Form058ReportDto> buildHierarchicalReportForAllRegions(List<Form058> filteredForms, LocalDate fromDate, LocalDate toDate, User currentUser) {
        Organization userOrg = currentUser.getCurrentOrganization();

        List<Form058ReportDto> reportDtos = new ArrayList<>();

        // Har bir regionni ajratish
        filteredForms.stream()
                .map(Form058::getCreatedOrg) // Formadagi tashkilotni olish
                .filter(org -> "region_level".equals(org.getLevel().getCode())) // Faqat region darajasi
                .distinct() // Yagona regionlarni olish
                .forEach(regionOrg -> {
                    // Har bir region uchun hisobotni yaratish
                    Form058ReportDto reportDto = new Form058ReportDto();
                    reportDto.setStateCode(currentUser.getCurrentOrganization().getState().getCode());
                    reportDto.setOrgName(regionOrg.getName());
                    reportDto.setOrgState(regionOrg.getState().getNameUz());
                    reportDto.setOrgCity(regionOrg.getCity().getNameUz());
                    reportDto.setLevel(regionOrg.getLevel().getCode());
                    reportDto.setReportDate(LocalDate.now());
                    reportDto.setDateFrom(fromDate);
                    reportDto.setDateTo(toDate);

                    // Region uchun hisobot ma'lumotlarini to‘plash
                    accumulateReportDataForRegion(reportDto, filteredForms, regionOrg);

                    reportDtos.add(reportDto);
                });

        return reportDtos;
    }

    private void accumulateReportDataForRegion(Form058ReportDto dto, List<Form058> filteredForms, Organization regionOrg) {
        // Regionga tegishli formalarni ajratish
        int confirmedTotal = 0, confirmedAge0To17 = 0, confirmedAge18Plus = 0, confirmedFemaleCount = 0;
        int total = 0, age0To17 = 0, age18Plus = 0, femaleCount = 0;

        for (Form058 form : filteredForms) {
            Organization formOrg = form.getCreatedOrg();
            if (formOrg.equals(regionOrg)) {
                Patient patient = form.getPatient();
                int age = Period.between(patient.getBirthDate(), LocalDate.now()).getYears();
                boolean isFemale = patient.getGender().getCode().equals(Gender.female.name());

                // Barchasi
                total++;
                if (age <= 17) age0To17++;
                else age18Plus++;
                if (isFemale) femaleCount++;

                // Faqat tasdiqlanganlar
                if (form.getStatus().equals(FormStatus.APPROVED)) {
                    confirmedTotal++;
                    if (age <= 17) confirmedAge0To17++;
                    else confirmedAge18Plus++;
                    if (isFemale) confirmedFemaleCount++;
                }
            }
        }

        dto.setTotal(total);
        dto.setAge0To17(age0To17);
        dto.setAge18Plus(age18Plus);
        dto.setFemaleCount(femaleCount);

        dto.setConfirmedTotal(confirmedTotal);
        dto.setConfirmedAge0To17(confirmedAge0To17);
        dto.setConfirmedAge18Plus(confirmedAge18Plus);
        dto.setConfirmedFemaleCount(confirmedFemaleCount);
    }


    public static Specification<Form058> filterByCriteria(
            String regionCode,
            LocalDate fromDate,
            LocalDate toDate,
            LocalDate birthDate,
            Gender gender,
            User currentUser) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            Organization userOrg = currentUser.getCurrentOrganization();
            if (userOrg != null) {
                if (fromDate != null && toDate != null) {
                    predicates.add(cb.between(root.get("createdAt"), fromDate.atStartOfDay(), toDate.atTime(23, 59, 59)));
                }

                if (birthDate != null) {
                    predicates.add(cb.equal(root.get("patient").get("birthDate"), birthDate));
                }

                if (gender != null) {
                    predicates.add(cb.equal(root.get("patient").get("gender").get("code"), gender.name()));
                }
                // Region kodiga asoslangan filtr
                if (regionCode != null) {
                    predicates.add(cb.equal(root.get("createdOrg").get("state").get("code"), regionCode));
                }


                switch (userOrg.getLevel().getCode()) {
                    case "republic_level" -> {
                    }
                    case "region_level" ->
                            predicates.add(cb.equal(root.get("createdOrg").get("state").get("code"), userOrg.getState().getCode()));
                    case "district_level" ->

                            predicates.add(cb.equal(root.get("createdOrg").get("state").get("code"), userOrg.getCity().getCode()));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
