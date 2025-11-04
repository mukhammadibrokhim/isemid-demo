package uz.uzinfocom.isemid.commons.utils;

import uz.uzinfocom.isemid.features.form058.models.Form058;
import java.time.LocalDate;

/**
 * ✅ Utility class for patient gender, age and residence-based checks.
 */
public class PatientUtils {

    /**
     * ✅ Returns TRUE if patient is FEMALE.
     */
    public static boolean isFemale(Form058 f) {
        return f != null &&
                f.getPatient() != null &&
                f.getPatient().getGenderCode() != null &&
                f.getPatient().getGenderCode().equalsIgnoreCase("FEMALE");
    }

    /**
     * ✅ Returns true if patient has NOT reached maxAge yet.
     * Example: maxAge = 17 → birthDate + 18 years > now → still under 18.
     */
    public static boolean isAgeLessOrEqual(Form058 f, int maxAge) {
        if (f == null || f.getPatient() == null || f.getPatient().getBirthDate() == null) return false;
        return f.getPatient().getBirthDate()
                .plusYears(maxAge + 1)
                .isAfter(LocalDate.now());
    }

    /**
     * ✅ Returns true if minAge ≤ age < maxAge + 1.
     */
    public static boolean isAgeBetween(Form058 f, int minAge, int maxAge) {
        if (f == null || f.getPatient() == null || f.getPatient().getBirthDate() == null) return false;
        LocalDate birthDate = f.getPatient().getBirthDate();
        LocalDate now = LocalDate.now();
        return !birthDate.plusYears(minAge).isAfter(now) &&
                birthDate.plusYears(maxAge + 1).isAfter(now);
    }

    /**
     * ✅ Returns true if child is younger than 1 month.
     * ("до 1 месяца" / "1 ойгача")
     */
    public static boolean isUnderOneMonth(Form058 f) {
        if (f == null || f.getPatient() == null || f.getPatient().getBirthDate() == null) return false;
        return f.getPatient().getBirthDate()
                .plusMonths(1)
                .isAfter(LocalDate.now());
    }

    /**
     * ✅ 1 oy ≤ yosh < 1 yil
     * ("1 ойдан 1 ёшгача")
     */
    public static boolean isAgeFrom1MonthTo1Year(Form058 f) {
        if (f == null || f.getPatient() == null || f.getPatient().getBirthDate() == null) return false;
        LocalDate birthDate = f.getPatient().getBirthDate();
        LocalDate now = LocalDate.now();

        boolean atLeast1Month = !birthDate.plusMonths(1).isAfter(now);  // ≥ 1 month
        boolean lessThan1Year = birthDate.plusYears(1).isAfter(now);    // < 1 year

        return atLeast1Month && lessThan1Year;
    }

    /**
     * ✅ Custom dynamic month range check
     * Example: 0–1 month → isAgeBetweenMonths(f, 0, 1)
     */
    public static boolean isAgeBetweenMonths(Form058 f, int minMonths, int maxMonths) {
        if (f == null || f.getPatient() == null || f.getPatient().getBirthDate() == null) return false;
        LocalDate birthDate = f.getPatient().getBirthDate();
        LocalDate now = LocalDate.now();

        return !birthDate.plusMonths(minMonths).isAfter(now) &&
                birthDate.plusMonths(maxMonths + 1).isAfter(now);
    }

    /**
     * ✅ Checks if patient lives in rural area (qishloq).
     */
    public static boolean isRural(Form058 f) {
        return f != null &&
                f.getPatient() != null &&
                f.getPatient().getPopulationTypeCode() != null &&
                f.getPatient().getPopulationTypeCode().equalsIgnoreCase("VILLAGE_RESIDENT");
    }
}
