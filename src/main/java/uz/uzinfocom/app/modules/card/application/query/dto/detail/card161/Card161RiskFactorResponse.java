package uz.uzinfocom.app.modules.card.application.query.dto.detail.card161;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Фактор риска, связанный со случаем заболевания.")
public record Card161RiskFactorResponse(
        @Schema(description = "Идентификатор записи.")
        Long id,

        @Schema(description = "Код фактора риска (по справочнику).")
        String riskFactorCode,

        @Schema(description = "Адрес/местоположение, связанное с фактором риска.")
        String addressLocation,

        @Schema(description = "Сезон/период проявления фактора риска.")
        String seasonTime
) {
}
