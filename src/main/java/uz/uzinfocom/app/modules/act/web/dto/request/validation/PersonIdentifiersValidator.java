package uz.uzinfocom.app.modules.act.web.dto.request.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.integration.api2.citizen.domain.CitizenLookupType;

import java.util.regex.Pattern;

public class PersonIdentifiersValidator implements ConstraintValidator<ValidPersonIdentifiers, HasPersonIdentifiers> {

    private static final Pattern NNUZB_PATTERN = Pattern.compile("\\d{14}");

    @Override
    public boolean isValid(HasPersonIdentifiers target, ConstraintValidatorContext context) {
        if (target == null) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        boolean valid = true;
        for (PersonIdentifier identifier : target.personIdentifiers()) {
            boolean hasType = identifier.type() != null;
            boolean hasValue = StringUtils.hasText(identifier.value());
            if (hasValue && !hasType) {
                valid = reject(context, identifier.typeProperty(), "{validation.identifier.type.required}");
            } else if (hasType && !hasValue) {
                valid = reject(context, identifier.valueProperty(), "{validation.identifier.value.required}");
            } else if (identifier.type() == CitizenLookupType.NNUZB
                    && !NNUZB_PATTERN.matcher(identifier.value()).matches()) {
                valid = reject(context, identifier.valueProperty(), "{validation.nnuzb.format}");
            }
        }
        return valid;
    }

    private static boolean reject(ConstraintValidatorContext context, String property, String message) {
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(property)
                .addConstraintViolation();
        return false;
    }
}
