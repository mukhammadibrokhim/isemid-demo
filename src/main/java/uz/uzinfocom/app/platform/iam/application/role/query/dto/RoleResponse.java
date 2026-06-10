package uz.uzinfocom.app.platform.iam.application.role.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Роль пользователя с правами доступа.")
public record RoleResponse(

        @Schema(
                description = "Идентификатор роли.",
                example = "1"
        )
        Long id,

        @Schema(
                description = "Системное наименование роли.",
                example = "isemid_epidemiologist"
        )
        String name,

        @Schema(
                description = "Признак активности роли.",
                example = "true"
        )
        Boolean active,

        @Schema(description = "Разрешения, назначенные роли.")
        List<RolePermissionResponse> permissions
) {
}