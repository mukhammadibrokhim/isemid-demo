package uz.uzinfocom.app.modules.patient.domain.model;

import java.time.LocalDate;
import java.time.Period;

/**
 * Patient age as exposed by the API. {@code months} is filled only for
 * children under one year (0-11) and is {@code null} otherwise.
 */
public record PatientAge(Integer years, Integer months) {

    /**
     * @return the age on {@code onDate}, or {@code null} when it cannot be computed
     * (a date is missing, or the birth date is after {@code onDate})
     */
    public static PatientAge between(LocalDate birthDate, LocalDate onDate) {
        if (birthDate == null || onDate == null || birthDate.isAfter(onDate)) {
            return null;
        }

        Period period = Period.between(birthDate, onDate);
        return new PatientAge(period.getYears(), period.getYears() == 0 ? period.getMonths() : null);
    }
}
