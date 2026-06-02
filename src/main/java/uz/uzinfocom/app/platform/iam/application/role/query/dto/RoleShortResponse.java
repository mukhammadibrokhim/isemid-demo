package uz.uzinfocom.app.platform.iam.application.role.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Краткая информация о роли.")
public record RoleShortResponse(
        @Schema(description = "Уникальный идентификатор записи.", example = "1")
        Long id,
        @Schema(description = "Наименование роли.", example = "ROLE_ADMIN")
        String name,
        @Schema(description = "Описание роли.")
        String description,
        @Schema(description = "Признак активности записи.", example = "true")
        Boolean active
) {
}
