package uz.uzinfocom.app.platform.dashboard.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Количество активных пользователей с данной ролью, в рамках доступа текущей организации.")
public record RoleCountResponse(
        @Schema(description = "Наименование роли.")
        String role,

        @Schema(description = "Количество активных пользователей с этой ролью.")
        long count
) {
}
