package uz.uzinfocom.app.modules.act.application.query.dto.detail.embedded;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Тип пробы.")
public record SampleTypeInfoResponse(
        @Schema(description = "Идентификатор типа пробы (по справочнику).")
        Integer sampleTypeId,

        @Schema(description = "Наименование типа пробы (узб.).")
        String sampleTypeUz,

        @Schema(description = "Наименование типа пробы (рус.).")
        String sampleTypeRu
) {
}
