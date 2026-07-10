package uz.uzinfocom.app.modules.form058.application.query.dto.detail;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Географическое место выявления заболевания.")
public record Form058LocationDetailResponse(
        @Schema(description = "Идентификатор записи местоположения.")
        Long id,

        @Schema(description = "Широта.")
        Double latitude,

        @Schema(description = "Долгота.")
        Double longitude,

        @Schema(description = "Текстовое описание места (адрес).")
        String address
) {
}
