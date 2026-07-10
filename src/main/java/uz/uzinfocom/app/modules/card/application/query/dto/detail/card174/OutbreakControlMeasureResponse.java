package uz.uzinfocom.app.modules.card.application.query.dto.detail.card174;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Мероприятие по ликвидации очага зоонозного заболевания.")
public record OutbreakControlMeasureResponse(
        @Schema(description = "Идентификатор записи.")
        Long id,

        @Schema(description = "Количество вакцинированных животных.")
        Integer vaccinatedAnimals,

        @Schema(description = "Количество павших/утраченных животных.")
        Integer lostAnimals,

        @Schema(description = "Количество мяса, сданного на переработку.")
        Integer meatDelivered,

        @Schema(description = "Код метода обработки/переработки (по справочнику).")
        String processingMethodCode,

        @Schema(description = "Площадь обработанной территории.")
        Integer processedArea,

        @Schema(description = "Признак того, что мероприятие фактически было проведено.")
        Boolean eventConducted
) {
}
