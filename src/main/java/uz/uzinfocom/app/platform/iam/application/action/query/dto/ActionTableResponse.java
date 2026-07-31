package uz.uzinfocom.app.platform.iam.application.action.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Краткая информация о действии для табличного списка.")
public record ActionTableResponse(
        @Schema(description = "Уникальный идентификатор записи.", example = "1")
        Long id,
        @Schema(description = "Код действия.", example = "READ")
        String code,
        @Schema(description = "Описание действия на узбекском языке.")
        String descriptionUz,
        @Schema(description = "Описание действия на русском языке.")
        String descriptionRu,
        @Schema(description = "Описание действия на узбекском языке кириллицей.")
        String descriptionUzCyril,
        @Schema(description = "Описание действия на каракалпакском языке.")
        String descriptionKaa,
        @Schema(description = "Признак активности записи.", example = "true")
        Boolean active
) {
}
