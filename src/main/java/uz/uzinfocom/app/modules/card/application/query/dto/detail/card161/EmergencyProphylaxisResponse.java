package uz.uzinfocom.app.modules.card.application.query.dto.detail.card161;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Сведения о проведении экстренной профилактической/антирабической помощи пациенту.")
public record EmergencyProphylaxisResponse(
        @Schema(description = "Идентификатор записи.")
        Long id,

        @Schema(description = "Дата и время проведения помощи.")
        LocalDateTime treatmentDate,

        @Schema(description = "Наименование препарата.")
        String drugName,

        @Schema(description = "Доза препарата.")
        String dose,

        @Schema(description = "Серия препарата.")
        String serialNumber,

        @Schema(description = "График проведения.")
        String administrationSchedule
) {
}
