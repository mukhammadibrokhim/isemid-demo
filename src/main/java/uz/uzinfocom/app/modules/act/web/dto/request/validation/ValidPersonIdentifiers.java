package uz.uzinfocom.app.modules.act.web.dto.request.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Every {@link PersonIdentifier} of the annotated {@link HasPersonIdentifiers}
 * is optional, but type and value come together, and an {@code NNUZB} value is
 * exactly 14 digits. Violations are attached to the offending property, so the
 * path reads e.g. {@code kitchenUtensils[0].identifierValueOfChef}.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PersonIdentifiersValidator.class)
public @interface ValidPersonIdentifiers {

    String message() default "{validation.invalid_value}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
