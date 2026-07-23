package uz.uzinfocom.app.modules.act.application.query.dto.detail.embedded;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Тип и категория объекта исследования.")
public record ResearchItemTypeInfoResponse(
        @Schema(description = "Идентификатор типа исследования (по справочнику).")
        Integer researchTypeId,

        @Schema(description = "Наименование типа исследования (узб.).")
        String researchTypeNameUz,

        @Schema(description = "Наименование типа исследования (рус.).")
        String researchTypeNameRu,

        @Schema(description = "Идентификатор категории (по справочнику).")
        Integer categoryId,

        @Schema(description = "Наименование категории (узб.).")
        String categoryNameUz,

        @Schema(description = "Наименование категории (рус.).")
        String categoryNameRu,

        @Schema(description = "Идентификатор типа объекта (по справочнику).")
        Integer itemTypeId,

        @Schema(description = "Наименование типа объекта (узб.).")
        String itemTypeNameUz,

        @Schema(description = "Наименование типа объекта (рус.).")
        String itemTypeNameRu
) {
}
