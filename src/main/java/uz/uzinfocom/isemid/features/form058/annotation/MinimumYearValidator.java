package uz.uzinfocom.isemid.features.form058.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;

public class MinimumYearValidator implements ConstraintValidator<MinYear, LocalDate> {
    private int minYear;

    @Override
    public void initialize(MinYear constraintAnnotation) {
        this.minYear = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(LocalDate date, ConstraintValidatorContext context) {
        if (date == null) {
            return true; // Let @NotNull handle null checks if needed
        }
        return date.getYear() >= minYear;
    }
}
