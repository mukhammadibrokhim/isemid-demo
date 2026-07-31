package uz.uzinfocom.app.platform.iam.application.role.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Действие, назначенное праву доступа роли.")
public record RolePermissionActionResponse(

        @Schema(description = "Идентификатор действия.", example = "1")
        Long id,

        @Schema(description = "Уникальный код действия.", example = "READ")
        String code,

        @Schema(description = "Локализованное описание действия.")
        String description
) {
}
