package uz.uzinfocom.app.platform.iam.application.organization.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Пользователь организации для справочного выбора.")
public record OrganizationUserLookupResponse(
        @Schema(description = "Уникальный идентификатор пользователя.", example = "1")
        Long id,
        @Schema(description = "Уникальный UUID пользователя.")
        UUID uuid,
        @Schema(description = "Имя пользователя.")
        String firstName,
        @Schema(description = "Фамилия пользователя.")
        String lastName,
        @Schema(description = "Отчество пользователя.")
        String middleName,
        @Schema(description = "Номер телефона пользователя.")
        String phoneNumber
) {
}
