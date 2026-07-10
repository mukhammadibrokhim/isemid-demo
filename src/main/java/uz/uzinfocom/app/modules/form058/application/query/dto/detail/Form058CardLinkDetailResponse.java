package uz.uzinfocom.app.modules.form058.application.query.dto.detail;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Сведения о привязке карт к форме №058.")
public record Form058CardLinkDetailResponse(
        @Schema(description = "Признак наличия привязанных к форме карт.")
        Boolean hasLinkedCards,

        @Schema(description = "Идентификатор привязанной карты (устаревшее поле для одиночной привязки).")
        Long assignedCardId
) {
}
