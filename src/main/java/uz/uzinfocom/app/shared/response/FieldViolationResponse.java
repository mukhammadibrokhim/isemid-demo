package uz.uzinfocom.app.shared.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Описание ошибки валидации одного поля.")
public record FieldViolationResponse(
        @Schema(description = "Имя поля.", example = "name")
        String field,

        @Schema(description = "Описание ошибки.", example = "name is required")
        String message
) {
}
