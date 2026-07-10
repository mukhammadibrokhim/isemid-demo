package uz.uzinfocom.app.modules.form058.application.query.dto.detail;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Эпидемиологические сведения формы №058.")
public record Form058EpidemicDetailResponse(
        @Schema(description = "Код места возникновения заболевания (по справочнику).")
        String diseasePlaceCode,

        @Schema(description = "Предполагаемая причина заболевания.")
        String diseaseCause,

        @Schema(description = "Принятые противоэпидемические меры.")
        String epidemicMeasures
) {
}
