package uz.uzinfocom.app.platform.iam.application.role.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.platform.iam.application.permission.query.dto.PermissionDetailResponse;

import java.util.List;

@Schema(description = "Детальная информация о роли с назначенными правами доступа.")
public record RoleDetailResponse(
        @Schema(description = "Уникальный идентификатор записи.", example = "1")
        Long id,
        @Schema(description = "Наименование роли.", example = "ROLE_ADMIN")
        String name,
        @Schema(description = "Описание роли на узбекском языке.")
        String descriptionUz,
        @Schema(description = "Описание роли на узбекском языке кириллицей.")
        String descriptionUzCyril,
        @Schema(description = "Описание роли на русском языке.")
        String descriptionRu,
        @Schema(description = "Описание роли на каракалпакском языке.")
        String descriptionKaa,
        @Schema(description = "Права доступа, назначенные роли.")
        List<PermissionDetailResponse> permissions
) {
}
