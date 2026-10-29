package uz.uzinfocom.app.integration.api2.web.dto;

public record FieldValidationError(
        String field,
        String message
) {
}
