package uz.uzinfocom.app.modules.card.application.query.dto.detail.card161;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Объект окружающей среды (источник воды/пищи), подлежащий обследованию.")
public record EnvironmentalSourceResponse(
        @Schema(description = "Идентификатор записи.")
        Long id,

        @Schema(description = "Виды источников питания и воды.")
        String foodAndWaterSourceTypes,

        @Schema(description = "Место отбора пробы.")
        String collectionLocation,

        @Schema(description = "Дата и время отбора пробы.")
        LocalDateTime collectionTime,

        @Schema(description = "Место использования продукта/воды.")
        String usageLocation,

        @Schema(description = "Дата и время использования продукта/воды.")
        LocalDateTime usageTime,

        @Schema(description = "Условия хранения продукта/воды.")
        String storageConditions,

        @Schema(description = "Отзыв о качестве продукта/воды со слов пациента и других лиц.")
        String qualityFeedbackFromPatientAndOthers
) {
}
