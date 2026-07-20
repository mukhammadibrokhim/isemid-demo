package uz.uzinfocom.app.platform.dashboard.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Пользователи в рамках доступа текущей организации — текущий снимок, не временной ряд.")
public record UsersResponse(
        @Schema(description = "Общее количество активных пользователей.")
        long total,

        @Schema(description = "Разбивка по ролям.")
        List<RoleCountResponse> byRole
) {
}
