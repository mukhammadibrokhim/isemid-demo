package uz.uzinfocom.app.modules.act.application.query.dto.detail.embedded;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Условия (особые условия отбора либо условия хранения/доставки).")
public record ConditionInfoResponse(
        @Schema(description = "Идентификатор условия (по справочнику).")
        Integer conditionId,

        @Schema(description = "Описание условия (узб.).")
        String descriptionUz,

        @Schema(description = "Описание условия (рус.).")
        String descriptionRu
) {
}
