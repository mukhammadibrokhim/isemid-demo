package uz.uzinfocom.app.modules.form058.application.query.dto.detail;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Клинические сведения формы №058.")
public record Form058ClinicalDetailResponse(
        @Schema(description = "Признак лабораторного подтверждения диагноза.")
        Boolean labConfirmation,

        @Schema(description = "Идентификатор места госпитализации.")
        Long hospitalPlaceId
) {
}
