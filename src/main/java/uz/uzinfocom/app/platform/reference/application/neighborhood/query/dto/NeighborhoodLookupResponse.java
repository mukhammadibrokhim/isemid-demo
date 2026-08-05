package uz.uzinfocom.app.platform.reference.application.neighborhood.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Махалля для справочного выбора (select).")
public record NeighborhoodLookupResponse(
        @Schema(description = "Внутренний идентификатор махалли.", example = "1")
        Long id,
        @Schema(description = "Уникальный код махалли.", example = "AN-202001")
        String code,
        @Schema(description = "Код родительского района для данной махалли.", example = "AN-202")
        String parentCode,
        @Schema(description = "Идентификатор СОАТО махалли.", example = "1703202001")
        Integer soatoId,
        @Schema(description = "Идентификатор СОАТО родительского района.", example = "1703202")
        Integer parentSoatoId,
        @Schema(description = "ИНН (TIN) махалли.", example = "202853324")
        String tin,
        @Schema(description = "Наименование махалли, локализованное по текущей локали запроса.")
        String name
) {
}
