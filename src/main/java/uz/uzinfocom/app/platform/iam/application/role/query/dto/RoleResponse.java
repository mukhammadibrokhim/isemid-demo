package uz.uzinfocom.app.platform.iam.application.role.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.platform.iam.application.permission.query.dto.PermissionResponse;

import java.util.List;

@Schema(description = "Роль пользователя с правами доступа.")
public record RoleResponse(
        @Schema(description = "Уникальный идентификатор записи.", example = "1")
        Long id,
        @Schema(description = "Наименование роли.", example = "ROLE_ADMIN")
        String name,
        @Schema(description = "Признак активности записи.", example = "true")
        Boolean active,
        @Schema(description = "Права доступа роли.")
        List<PermissionResponse> permissions
) {
}
