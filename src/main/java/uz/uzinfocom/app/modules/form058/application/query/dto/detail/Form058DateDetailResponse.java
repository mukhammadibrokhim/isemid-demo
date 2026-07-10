package uz.uzinfocom.app.modules.form058.application.query.dto.detail;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "Ключевые даты формы №058.")
public record Form058DateDetailResponse(
        @Schema(description = "Дата поступления пациента.")
        LocalDate admissionDate,

        @Schema(description = "Дата начала заболевания.")
        LocalDate diseaseDate,

        @Schema(description = "Дата первого обращения пациента за медицинской помощью.")
        LocalDate firstVisitDate,

        @Schema(description = "Дата установления диагноза.")
        LocalDate diagnosisDate,

        @Schema(description = "Дата осмотра пациента, по итогам которого заполнена форма.")
        LocalDate visitDate,

        @Schema(description = "Дата и время первичного сообщения о случае заболевания.")
        LocalDateTime initialReportDateTime
) {
}
