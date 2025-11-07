package uz.uzinfocom.isemid.features.report.service.impls;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.utils.SpecificationUtils;
import uz.uzinfocom.isemid.features.classifier.models.Mkb10;
import uz.uzinfocom.isemid.features.classifier.repositories.Mkb10Repository;
import uz.uzinfocom.isemid.features.form058.enums.FormStatus;
import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.form058.reposiotries.Form058Repository;
import uz.uzinfocom.isemid.features.report.dtos.MonitoringResponse;
import uz.uzinfocom.isemid.features.report.service.MonitoringService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
public class MonitoringServiceImpl implements MonitoringService {
    private final Form058Repository form058Repository;
    private final Mkb10Repository mkb10Repository;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");


    @Override
    public MonitoringResponse getMonitoring(String fromDate, String toDate) {
        LocalDate from = parseDate(fromDate);
        LocalDate to = parseDate(toDate);

        Specification<Form058> spec = filterByCreatedDate(from, to);
        List<Form058> forms = form058Repository.findAll(spec);

        MonitoringResponse result = new MonitoringResponse();
        result.setDiseaseDate(LocalDate.now());
        result.setTotalCases(forms.size());

        int confirmed = 0;
        int unconfirmed = 0;
        int male = 0;
        int female = 0;

        MonitoringResponse.AgeGroupCount ageGroups = new MonitoringResponse.AgeGroupCount();

        for (Form058 f : forms) {
            // status
            if (f.getStatus() != null) {
                if (f.getStatus() == FormStatus.APPROVED) {
                    confirmed++;
                } else {
                    unconfirmed++;
                }
            }

            // gender
            if (f.getPatient() != null && f.getPatient().getGenderCode() != null) {
                if (f.getPatient().getGenderCode().equalsIgnoreCase("male")) male++;
                else female++;
            }

            // age groups
            if (f.getPatient() != null && f.getPatient().getBirthDate() != null) {
                int age = java.time.Period.between(f.getPatient().getBirthDate(), LocalDate.now()).getYears();
                if (age <= 3) ageGroups.setAge_0_3(ageGroups.getAge_0_3() + 1);
                else if (age <= 6) ageGroups.setAge_3_6(ageGroups.getAge_3_6() + 1);
                else if (age <= 10) ageGroups.setAge_6_10(ageGroups.getAge_6_10() + 1);
                else if (age <= 17) ageGroups.setAge_10_17(ageGroups.getAge_10_17() + 1);
                else if (age <= 25) ageGroups.setAge_18_25(ageGroups.getAge_18_25() + 1);
                else if (age <= 45) ageGroups.setAge_25_45(ageGroups.getAge_25_45() + 1);
                else if (age <= 65) ageGroups.setAge_45_65(ageGroups.getAge_45_65() + 1);
                else ageGroups.setAge_65_plus(ageGroups.getAge_65_plus() + 1);
            }
            if (f.getFinalMkb10Code() != null) {
                result.setDiseaseType(buildDiseaseType(f.getFinalMkb10Code()));
            }

        }


        result.setStatus(MonitoringResponse.StatusCount.builder()
                .confirmed(confirmed)
                .unconfirmed(unconfirmed)
                .build());

        result.setGender(MonitoringResponse.GenderCount.builder()
                .male(male)
                .female(female)
                .build());

        result.setAgeGroups(ageGroups);

        return result;
    }

    private MonitoringResponse.DiseaseType buildDiseaseType(String mkb10Code) {
        if (mkb10Code == null) {
            return null;
        }

        var optionalMkb = mkb10Repository.findByCode(mkb10Code);
        if (optionalMkb.isEmpty()) {
            return MonitoringResponse.DiseaseType.builder()
                    .code(mkb10Code)
                    .disease("Noma’lum kasallik")
                    .build();
        }

        Mkb10 mkb = optionalMkb.get();

        String disease = mkb.getNameUz();
        String block = null;
        String clazz = null;
        AtomicReference<String> section = new AtomicReference<>();

        // 🔹 parent 1 → blok
        if (mkb.getParentId() != null) {
            var parent = mkb10Repository.findById(mkb.getParentId());
            if (parent.isPresent()) {
                block = parent.get().getNameUz();

                // 🔹 parent 2 → klass
                if (parent.get().getParentId() != null) {
                    var grand = mkb10Repository.findById(parent.get().getParentId());
                    if (grand.isPresent()) {
                        clazz = grand.get().getNameUz();

                        // 🔹 parent 3 → bo‘lim
                        if (grand.get().getParentId() != null) {
                            var top = mkb10Repository.findById(grand.get().getParentId());
                            top.ifPresent(t -> section.set(t.getNameUz()));
                        }
                    }
                }
            }
        }

        return MonitoringResponse.DiseaseType.builder()
                .code(mkb.getCode())
                .disease(disease)
                .block(block)
                .clazz(clazz)
                .section(section.toString())
                .build();
    }


    public static Specification<Form058> filterByCreatedDate(LocalDate fromDate, LocalDate toDate) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            SpecificationUtils.fromToDateFilter(fromDate, toDate, root, cb, predicates, "createdAt");

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private LocalDate parseDate(String date) {
        if (date == null || date.isBlank()) return null;
        return LocalDate.parse(date, FORMATTER);
    }
}
