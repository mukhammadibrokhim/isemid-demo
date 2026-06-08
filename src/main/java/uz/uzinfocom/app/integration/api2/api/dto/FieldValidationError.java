package uz.uzinfocom.app.integration.api2.api.dto;

public record FieldValidationError(
        String field,
        String message
) {
}
