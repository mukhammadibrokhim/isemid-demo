package uz.uzinfocom.app.shared.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.List;

@Schema(description = "Стандартная модель ошибки API.")
public record ErrorResponse(
        @Schema(description = "Признак успешного выполнения запроса.", example = "false")
        boolean success,

        @Schema(description = "Код ошибки.", example = "VALIDATION_FAILED")
        String code,

        @Schema(description = "Сообщение об ошибке.", example = "Некорректный запрос.")
        String message,

        @Schema(description = "Идентификатор трассировки запроса.", example = "9f1c2a3b4d")
        String traceId,

        @Schema(description = "Путь запроса.", example = "/v1/users")
        String path,

        @Schema(description = "Дата и время формирования ошибки.")
        OffsetDateTime timestamp,

        @Schema(description = "Список ошибок валидации по полям.")
        List<FieldViolationResponse> violations
) {
    public static ErrorResponse of(
            String code,
            String message,
            String traceId,
            String path,
            List<FieldViolationResponse> violations
    ) {
        return new ErrorResponse(
                false,
                code,
                message,
                traceId,
                path,
                OffsetDateTime.now(),
                violations == null ? List.of() : List.copyOf(violations)
        );
    }
}
