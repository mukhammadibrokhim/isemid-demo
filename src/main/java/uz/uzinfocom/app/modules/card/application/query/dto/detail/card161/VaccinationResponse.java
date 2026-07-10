package uz.uzinfocom.app.modules.card.application.query.dto.detail.card161;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Сведения о вакцинации пациента.")
public record VaccinationResponse(
        @Schema(description = "Идентификатор записи.")
        Long id,

        @Schema(description = "Код подтверждения факта вакцинации (по справочнику).")
        String vaccinationVerifiedCode,

        @Schema(description = "Наименование вакцины.")
        String vaccinationName,

        @Schema(description = "Серийный номер препарата.")
        String serialNumber,

        @Schema(description = "Дата и время проведения вакцинации.")
        LocalDateTime vaccinationDate,

        @Schema(description = "Объём введённой дозы препарата.")
        Integer doseVolume,

        @Schema(description = "Признак того, что вакцинация проведена по установленному графику.")
        Boolean scheduled
) {
}
