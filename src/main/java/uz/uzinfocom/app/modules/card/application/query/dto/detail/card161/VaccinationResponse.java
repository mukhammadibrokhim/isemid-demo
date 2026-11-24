package uz.uzinfocom.app.modules.card.application.query.dto.detail.card161;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
        BigDecimal doseVolume,

        @Schema(description = "Признак того, что вакцинация проведена по установленному графику.")
        Boolean scheduled,

        @Schema(description = "Идентификатор исходного ресурса Immunization в DHP FHIR; null, если запись введена вручную.")
        String fhirId,

        @Schema(description = "Код вакцины (из DHP FHIR).")
        String vaccineCode,

        @Schema(description = "Срок годности препарата.")
        LocalDate expirationDate,

        @Schema(description = "Единица измерения дозы (например, ml).")
        String doseUnit,

        @Schema(description = "Порядковый номер дозы в схеме вакцинации.")
        Integer doseNumber,

        @Schema(description = "Заболевания, против которых проведена вакцинация.")
        List<String> targetDiseases,

        @Schema(description = "Исполнитель (медицинский работник или организация).")
        String performerName
) {
}
