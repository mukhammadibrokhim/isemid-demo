package uz.uzinfocom.app.modules.reference.application.neighborhood.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Детальный ответ по махалле вместе с родительским районом и регионом (получение по идентификатору).")
public record NeighborhoodDetailResponse(
        @Schema(description = "Внутренний идентификатор махалли.", example = "1")
        Long id,
        @Schema(description = "Уникальный код махалли.", example = "AN-202001")
        String code,
        @Schema(description = "Код родительского района для данной махалли.", example = "AN-202")
        String parentCode,
        @Schema(description = "Наименование родительского района (локализовано по текущему языку запроса).", example = "Алтынкульский район")
        String parentName,
        @Schema(description = "Код региона, в который входит родительский район.", example = "UZ-AN")
        String grandParentCode,
        @Schema(description = "Наименование региона родительского района (локализовано по текущему языку запроса).", example = "Андижанская область")
        String grandParentName,
        @Schema(description = "ИНН (TIN) махалли.", example = "202853324")
        String tin,
        @Schema(description = "Наименование махалли на узбекском языке (латиница).", example = "Dalvarzin")
        String nameUz,
        @Schema(description = "Наименование махалли на узбекском языке (кириллица).", example = "Далварзин")
        String nameUzCyril,
        @Schema(description = "Наименование махалли на русском языке.", example = "Далварзин")
        String nameRu,
        @Schema(description = "Наименование махалли на каракалпакском языке.", example = "Dalvarzin")
        String nameKaa,
        @Schema(description = "Признак мягкого удаления.", example = "false")
        Boolean deleted
) {
}
