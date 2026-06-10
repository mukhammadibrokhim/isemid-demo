package uz.uzinfocom.app.platform.iam.application.role.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.platform.iam.application.shared.dto.AuditResponse;

import java.util.List;

@Schema(description = "Подробная информация о роли.")
public record RoleDetailResponse(

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

        @Schema(description = "Описание роли на узбекском языке.")
        String descriptionUz,

        @Schema(description = "Описание роли на узбекском языке в кириллице.")
        String descriptionUzCyril,

        @Schema(description = "Описание роли на русском языке.")
        String descriptionRu,

        @Schema(description = "Описание роли на каракалпакском языке.")
        String descriptionKaa,

        @Schema(description = "Информация об аудите.")
        AuditResponse audit,

        @Schema(description = "Разрешения, назначенные роли.")
        List<RolePermissionResponse> permissions
) {
}