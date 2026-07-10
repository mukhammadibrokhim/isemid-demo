package uz.uzinfocom.app.modules.card.application.query.dto.detail.card161;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "Профилактическое мероприятие, проведённое на дому.")
public record HomePreventiveMeasureResponse(
        @Schema(description = "Идентификатор записи.")
        Long id,

        @Schema(description = "Лицо, уведомлённое о профилактическом мероприятии.")
        String notifiedPerson,

        @Schema(description = "Ожидаемое время наступления заболевания (интеграция с DMED).")
        LocalDateTime nextDiseaseTime,

        @Schema(description = "Наименование введённого препарата (интеграция с DMED).")
        String drugName,

        @Schema(description = "Доза введённого препарата (интеграция с DMED).")
        String dose,

        @Schema(description = "Серия препарата (интеграция с DMED).")
        String series,

        @Schema(description = "Время получения результата (интеграция с ЛИС).")
        LocalDateTime resultTime,

        @Schema(description = "Дата получения профилактического препарата.")
        LocalDate receivedDate,

        @Schema(description = "Результат лабораторного исследования (интеграция с ЛИС).")
        String lisResult,

        @Schema(description = "Результат наблюдения за состоянием после мероприятия.")
        String observationResult
) {
}
