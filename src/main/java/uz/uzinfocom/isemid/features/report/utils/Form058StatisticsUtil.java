package uz.uzinfocom.isemid.features.report.utils;

import uz.uzinfocom.isemid.features.form058.enums.FormStatus;
import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.patient.enums.Gender;
import uz.uzinfocom.isemid.features.report.dtos.Form1ReportDto;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Form058StatisticsUtil {

    public static Form1ReportDto calculate(List<Form058> forms, String name, Set<Form1ReportDto> children) {
        long confirmedTotal = 0;
        long confirmedAdults = 0;
        long confirmedUnder18 = 0;
        long confirmedFemales = 0;

        long emergencyTotal = 0;
        long emergencyAdults = 0;
        long emergencyUnder18 = 0;
        long emergencyFemales = 0;

        LocalDate today = LocalDate.now();

        for (Form058 form : forms) {
            if (form == null || form.getPatient() == null || form.getPatient().getBirthDate() == null) {
                continue; // skip invalid data
            }

            boolean isAdult = Period.between(form.getPatient().getBirthDate(), today).getYears() >= 18;
            boolean isFemale = Gender.female.name().equalsIgnoreCase(form.getPatient().getGenderCode());

            // Emergency (all forms)
            emergencyTotal++;
            if (isAdult) emergencyAdults++;
            else emergencyUnder18++;
            if (isAdult && isFemale) emergencyFemales++;

            // Confirmed (only APPROVED forms)
            if (form.getStatus() == FormStatus.APPROVED) {
                confirmedTotal++;
                if (isAdult) confirmedAdults++;
                else confirmedUnder18++;
                if (isAdult && isFemale) confirmedFemales++;
            }
        }

        return Form1ReportDto.builder()
                .name(name)
                .confirmedTotal(confirmedTotal)
                .confirmedAdults(confirmedAdults)
                .confirmedUnder18(confirmedUnder18)
                .confirmedFemales(confirmedFemales)
                .emergencyTotal(emergencyTotal)
                .emergencyAdults(emergencyAdults)
                .emergencyUnder18(emergencyUnder18)
                .emergencyFemales(emergencyFemales)
                .children(children)
                .growthRate(((double) emergencyTotal /confirmedTotal) * 100)
                .build();
    }

    public static Form1ReportDto calculate(List<Form058> forms, String name) {
        return calculate(forms, name, Set.of());
    }

    public static Set<Form1ReportDto> groupAndCalculate(
            List<Form058> forms,
            Function<Form058, String> keyExtractor,
            Function<Form058, String> nameExtractor
    ) {
        return forms.stream()
                .filter(f -> f.getPatient() != null && f.getPatient().getBirthDate() != null)
                .collect(Collectors.groupingBy(keyExtractor))
                .values().stream()
                .map(groupForms -> {
                    String name = nameExtractor.apply(groupForms.getFirst());
                    return calculate(groupForms, name);
                })
                .collect(Collectors.toSet());
    }
}

