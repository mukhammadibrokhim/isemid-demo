package uz.uzinfocom.isemid.features.report.utils;

import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.time.Period;

@UtilityClass
public class ReportUtil {

    public static boolean isAdult(LocalDate birthDate) {
        if (birthDate == null) return false;
        int age = Period.between(birthDate, LocalDate.now()).getYears();
        return age >= 18;
    }
}
