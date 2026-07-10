package uz.uzinfocom.app.modules.card.application.query.dto.detail.card_tube;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Результат рентгенографии грудной клетки, выполненной до выявления МБТ.")
public record XRayResponse(
        @Schema(description = "Идентификатор записи.")
        Long id,

        @Schema(description = "Дата проведения рентгенографии.")
        LocalDate xrayDate,

        @Schema(description = "Место (учреждение) проведения рентгенографии.")
        String xrayPlace,

        @Schema(description = "Результат рентгенографии.")
        String result
) {
}
