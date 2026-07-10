package uz.uzinfocom.app.modules.card.application.query.dto.detail.card161;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Дезинфекционное мероприятие, проведённое в очаге заболевания.")
public record OutbreakDisinfectionMeasureResponse(
        @Schema(description = "Идентификатор записи.")
        Long id,

        @Schema(description = "Код профилактического мероприятия (по справочнику).")
        String preventiveMeasuresCode,

        @Schema(description = "Тип использованного дезинфицирующего препарата.")
        String drugType,

        @Schema(description = "Место проведения мероприятия.")
        String conductedAt,

        @Schema(description = "Код места проведения мероприятия (по справочнику).")
        String conductedLocationCode,

        @Schema(description = "Исполнители мероприятия.")
        String executors,

        @Schema(description = "Результат контроля выполнения мероприятия.")
        String executionMonitoringResult
) {
}
