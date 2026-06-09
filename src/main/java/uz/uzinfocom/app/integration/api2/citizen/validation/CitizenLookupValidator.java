package uz.uzinfocom.app.integration.api2.citizen.validation;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.integration.api2.api.dto.FieldValidationError;
import uz.uzinfocom.app.integration.api2.citizen.domain.CitizenLookupRequest;
import uz.uzinfocom.app.integration.api2.citizen.exception.CitizenLookupValidationException;
import uz.uzinfocom.app.integration.api2.citizen.exception.UnsupportedCitizenLookupTypeException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Component
public class CitizenLookupValidator {

    private static final Pattern NNUZB_PATTERN = Pattern.compile("\\d{14}");

    public CitizenLookupRequest validate(CitizenLookupRequest request) {
        if (request.type() == null) {
            throw new UnsupportedCitizenLookupTypeException();
        }

        return switch (request.type()) {
            case NNUZB -> validateNnuzb(request);
            case PPN -> validatePassport(request);
            case CZ -> validateChild(request);
        };
    }

    private CitizenLookupRequest validateNnuzb(CitizenLookupRequest request) {
        List<FieldValidationError> errors = new ArrayList<>();
        String nnuzb = trim(request.nnuzb());

        if (!StringUtils.hasText(nnuzb)) {
            errors.add(new FieldValidationError("nnuzb", "validation.required"));
        } else if (!NNUZB_PATTERN.matcher(nnuzb).matches()) {
            errors.add(new FieldValidationError("nnuzb", "validation.nnuzb.format"));
        }

        validateBirthDate(request.birthDate(), errors);
        throwIfInvalid(errors);

        return new CitizenLookupRequest(
                request.type(),
                nnuzb,
                null,
                request.birthDate(),
                null,
                null
        );
    }

    private CitizenLookupRequest validatePassport(CitizenLookupRequest request) {
        List<FieldValidationError> errors = new ArrayList<>();
        String document = trim(request.document());

        if (!StringUtils.hasText(document)) {
            errors.add(new FieldValidationError("document", "validation.required"));
        }

        validateBirthDate(request.birthDate(), errors);
        throwIfInvalid(errors);

        return new CitizenLookupRequest(
                request.type(),
                null,
                document.toUpperCase(Locale.ROOT),
                request.birthDate(),
                null,
                null
        );
    }

    private CitizenLookupRequest validateChild(CitizenLookupRequest request) {
        List<FieldValidationError> errors = new ArrayList<>();
        String series = trim(request.series());
        String number = trim(request.number());

        if (!StringUtils.hasText(series)) {
            errors.add(new FieldValidationError("series", "validation.required"));
        }

        if (!StringUtils.hasText(number)) {
            errors.add(new FieldValidationError("number", "validation.required"));
        }

        throwIfInvalid(errors);

        return new CitizenLookupRequest(
                request.type(),
                null,
                null,
                null,
                series,
                number
        );
    }

    private void validateBirthDate(LocalDate birthDate, List<FieldValidationError> errors) {
        if (birthDate == null) {
            errors.add(new FieldValidationError("birth_date", "validation.birth_date.required"));
            return;
        }

        if (birthDate.isAfter(LocalDate.now())) {
            errors.add(new FieldValidationError("birth_date", "validation.birth_date.future"));
        }
    }

    private void throwIfInvalid(List<FieldValidationError> errors) {
        if (!errors.isEmpty()) {
            throw new CitizenLookupValidationException(errors);
        }
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
