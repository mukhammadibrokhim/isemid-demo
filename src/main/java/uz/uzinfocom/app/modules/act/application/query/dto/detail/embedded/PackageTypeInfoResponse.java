package uz.uzinfocom.app.modules.act.application.query.dto.detail.embedded;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Тип упаковки пробы.")
public record PackageTypeInfoResponse(
        @Schema(description = "Идентификатор типа упаковки (по справочнику).")
        Integer packageTypeId,

        @Schema(description = "Наименование типа упаковки (узб.).")
        String packageTypeUz,

        @Schema(description = "Наименование типа упаковки (рус.).")
        String packageTypeRu
) {
}
