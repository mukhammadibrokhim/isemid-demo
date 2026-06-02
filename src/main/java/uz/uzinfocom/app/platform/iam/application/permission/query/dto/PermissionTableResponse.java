package uz.uzinfocom.app.platform.iam.application.permission.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Краткая информация о праве доступа для табличного списка.")
public record PermissionTableResponse(
        @Schema(description = "Уникальный идентификатор записи.", example = "1")
        Long id,
        @Schema(description = "Субъект права доступа.", example = "users")
        String subject,
        @Schema(description = "Описание права доступа на узбекском языке.")
        String descriptionUz,
        @Schema(description = "Описание права доступа на русском языке.")
        String descriptionRu,
        @Schema(description = "Описание права доступа на узбекском языке кириллицей.")
        String descriptionUzCyril,
        @Schema(description = "Описание права доступа на каракалпакском языке.")
        String descriptionKaa,
        @Schema(description = "Признак активности записи.", example = "true")
        Boolean active
) {
}
