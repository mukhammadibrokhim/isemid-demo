package uz.uzinfocom.isemid.features.form058.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MinimumYearValidator.class)
@Documented
public @interface MinYear {
    int value() default 1900;
    String message() default "Year must be {value} or later";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
