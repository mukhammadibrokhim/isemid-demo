package uz.uzinfocom.app.platform.iam.application.user.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Краткие сведения о пользователе (для встраивания в другие ответы, например, в качестве ссылки на автора записи).")
public record UserMiniResponse(
        @Schema(description = "Идентификатор пользователя.")
        Long id,

        @Schema(description = "UUID пользователя.")
        UUID uuid,

        @Schema(description = "Имя пользователя для входа (логин).")
        String username,

        @Schema(description = "Имя.")
        String firstName,

        @Schema(description = "Фамилия.")
        String lastName,

        @Schema(description = "Отчество.")
        String middleName,

        @Schema(description = "Полное имя (ФИО одной строкой).")
        String fullName
) {
}
