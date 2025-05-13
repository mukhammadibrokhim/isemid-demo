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
        List<Form058> confirmedForm58 = forms.stream().filter(f -> f.getStatus() == FormStatus.APPROVED).toList();

        long confirmedTotal = confirmedForm58.size();

        long confirmedAdults = confirmedForm58.stream().filter(f -> isAdult(f.getPatient().getBirthDate())).count();

        long confirmedUnder18 =  confirmedForm58.stream().filter(f -> !isAdult(f.getPatient().getBirthDate())).count();

        long confirmedFemales = confirmedForm58.stream()
                .filter(f -> isAdult(f.getPatient().getBirthDate()))
                .filter(f -> Gender.female.name().equals(f.getPatient().getGenderCode())).count();

        long emergencyTotal = forms.size();

        long emergencyAdults = forms.stream().filter(f-> isAdult(f.getPatient().getBirthDate())).count();
        long emergencyUnder18 = forms.stream().filter(f-> !isAdult(f.getPatient().getBirthDate())).count();
        long emergencyFemales = forms.stream()
                .filter(f -> isAdult(f.getPatient().getBirthDate()))
                .filter(f -> Gender.female.name().equals(f.getPatient().getGenderCode())).count();


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
                .growthRate((confirmedTotal + emergencyTotal) * 100)
                .build();
    }

    public static Form1ReportDto calculate(List<Form058> forms, String name) {
        return calculate(forms, name, Set.of());
    }

    public static <T> Set<Form1ReportDto> groupAndCalculate(
            List<Form058> forms,
            Function<Form058, String> keyExtractor,
            Function<Form058, String> nameExtractor
    ) {
        return forms.stream()
                .collect(Collectors.groupingBy(keyExtractor))
                .values().stream()
                .map(groupForms -> {
                    String name = nameExtractor.apply(groupForms.getFirst());
                    return calculate(groupForms, name);
                })
                .collect(Collectors.toSet());
    }

    private static boolean isAdult(LocalDate birthDate) {
        return Period.between(birthDate, LocalDate.now()).getYears() >= 18;
    }
}
