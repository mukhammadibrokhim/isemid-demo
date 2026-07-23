package uz.uzinfocom.app.modules.act.application.query.dto.detail.embedded;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Цель отбора пробы.")
public record PurposeResponse(
        @Schema(description = "Идентификатор цели (по справочнику).")
        Integer purposeId,

        @Schema(description = "Наименование цели отбора (узб.).")
        String samplingPurposeUz,

        @Schema(description = "Наименование цели отбора (рус.).")
        String samplingPurposeRu,

        @Schema(description = "Код цели отбора по LOINC.")
        String samplingPurposeLoinc
) {
}
