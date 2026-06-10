package uz.uzinfocom.app.platform.iam.application.shared.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Краткая информация о пользователе, выполнившем действие.")
public record AuditUserResponse(

        @Schema(
                description = "Внутренний идентификатор пользователя.",
                example = "1"
        )
        Long id,

        @Schema(
                description = "Имя пользователя.",
                example = "Алексей"
        )
        String firstName,

        @Schema(
                description = "Фамилия пользователя.",
                example = "Иванов"
        )
        String lastName,

        @Schema(
                description = "Отчество пользователя.",
                example = "Сергеевич",
                nullable = true
        )
        String middleName
) {
}
