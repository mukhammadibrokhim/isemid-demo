package uz.uzinfocom.app.integration.api2.legalentity.validation;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.integration.api2.api.dto.FieldValidationError;
import uz.uzinfocom.app.integration.api2.legalentity.exception.LegalEntityValidationException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class LegalEntityLookupValidator {

    private static final Pattern TIN_PATTERN = Pattern.compile("\\d{9}");

    public String validateTin(String rawTin) {
        List<FieldValidationError> errors = new ArrayList<>();
        String tin = rawTin == null ? null : rawTin.trim();

        if (!StringUtils.hasText(tin)) {
            errors.add(new FieldValidationError("tin", "validation.required"));
        } else if (!TIN_PATTERN.matcher(tin).matches()) {
            errors.add(new FieldValidationError("tin", "validation.tin.format"));
        }

        if (!errors.isEmpty()) {
            throw new LegalEntityValidationException(errors);
        }

        return tin;
    }
}
