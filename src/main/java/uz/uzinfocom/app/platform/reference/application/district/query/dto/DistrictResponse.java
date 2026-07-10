package uz.uzinfocom.app.platform.reference.application.district.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Детальный ответ по району.")
public record DistrictResponse(
        @Schema(description = "Внутренний идентификатор района.", example = "1")
        Long id,
        @Schema(description = "Уникальный код района.", example = "AN-202")
        String code,
        @Schema(description = "Код родительского региона для данного района.", example = "UZ-AN")
        String parentCode,
        @Schema(description = "Наименование района на узбекском языке (латиница).", example = "Oltinko‘l tumani")
        String nameUz,
        @Schema(description = "Наименование района на узбекском языке (кириллица).", example = "Олтинкўл тумани")
        String nameUzCyril,
        @Schema(description = "Наименование района на русском языке.", example = "Алтынкульский район")
        String nameRu,
        @Schema(description = "Наименование района на каракалпакском языке.", example = "Oltinkól rayonı")
        String nameKaa,
        @Schema(description = "Признак мягкого удаления.", example = "false")
        Boolean deleted
) {
}
