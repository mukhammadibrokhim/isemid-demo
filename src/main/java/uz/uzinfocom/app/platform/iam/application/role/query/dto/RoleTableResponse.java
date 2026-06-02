package uz.uzinfocom.app.platform.iam.application.role.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Краткая информация о роли для табличного списка.")
public record RoleTableResponse(
        @Schema(description = "Уникальный идентификатор записи.", example = "1")
        Long id,
        @Schema(description = "Наименование роли.", example = "ROLE_ADMIN")
        String name,
        @Schema(description = "Признак активности записи.", example = "true")
        Boolean active,
        @Schema(description = "Описание роли на узбекском языке.")
        String descriptionUz,
        @Schema(description = "Описание роли на русском языке.")
        String descriptionRu,
        @Schema(description = "Описание роли на узбекском языке кириллицей.")
        String descriptionUzCyril,
        @Schema(description = "Описание роли на каракалпакском языке.")
        String descriptionKaa
) {
}
