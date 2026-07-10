package uz.uzinfocom.app.platform.reference.application.region.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Детальный ответ по региону.")
public record RegionResponse(
        @Schema(description = "Внутренний идентификатор региона.", example = "1")
        Long id,
        @Schema(description = "Уникальный код региона.", example = "UZ-AN")
        String code,
        @Schema(description = "Код родительской страны для данного региона.", example = "UZ")
        String parentCode,
        @Schema(description = "Наименование региона на узбекском языке (латиница).", example = "Andijon viloyati")
        String nameUz,
        @Schema(description = "Наименование региона на узбекском языке (кириллица).", example = "Андижон вилояти")
        String nameUzCyril,
        @Schema(description = "Наименование региона на русском языке.", example = "Андижанская область")
        String nameRu,
        @Schema(description = "Наименование региона на каракалпакском языке.", example = "Andijan wálayatı")
        String nameKaa,
        @Schema(description = "Признак мягкого удаления.", example = "false")
        Boolean deleted
) {
}
