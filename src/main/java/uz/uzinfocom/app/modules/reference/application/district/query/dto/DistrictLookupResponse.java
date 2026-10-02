package uz.uzinfocom.app.modules.reference.application.district.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Район для справочного выбора (select).")
public record DistrictLookupResponse(
        @Schema(description = "Внутренний идентификатор района.", example = "1")
        Long id,
        @Schema(description = "Уникальный код района.", example = "AN-202")
        String code,
        @Schema(description = "Код родительского региона для данного района.", example = "UZ-AN")
        String parentCode,
        @Schema(description = "Идентификатор СОАТО района.", example = "1703202")
        Integer soatoId,
        @Schema(description = "Идентификатор СОАТО родительского региона.", example = "1700000")
        Integer parentSoatoId,
        @Schema(description = "Наименование района, локализованное по текущей локали запроса.")
        String name
) {
}
