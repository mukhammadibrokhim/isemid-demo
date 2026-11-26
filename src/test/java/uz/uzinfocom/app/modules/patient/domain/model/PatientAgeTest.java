package uz.uzinfocom.app.modules.patient.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class PatientAgeTest {

    private static final LocalDate ON_DATE = LocalDate.of(2026, 11, 26);

    @Test
    void givesOnlyFullYearsFromOneYear() {
        assertThat(PatientAge.between(LocalDate.of(1994, 7, 30), ON_DATE))
                .isEqualTo(new PatientAge(32, null));
        assertThat(PatientAge.between(LocalDate.of(2025, 11, 26), ON_DATE))
                .isEqualTo(new PatientAge(1, null));
    }

    @Test
    void givesMonthsForChildrenUnderOneYear() {
        assertThat(PatientAge.between(LocalDate.of(2025, 11, 27), ON_DATE))
                .isEqualTo(new PatientAge(0, 11));
        assertThat(PatientAge.between(ON_DATE, ON_DATE))
                .isEqualTo(new PatientAge(0, 0));
    }

    @Test
    void isUnknownWithoutUsableDates() {
        assertThat(PatientAge.between(null, ON_DATE)).isNull();
        assertThat(PatientAge.between(ON_DATE, null)).isNull();
        assertThat(PatientAge.between(ON_DATE.plusDays(1), ON_DATE)).isNull();
    }
}
