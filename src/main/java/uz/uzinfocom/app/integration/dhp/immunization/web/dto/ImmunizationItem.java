package uz.uzinfocom.app.integration.dhp.immunization.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Field names {@code vaccinationName}, {@code serialNumber},
 * {@code vaccinationDate} and {@code doseVolume} deliberately match
 * {@code VaccinationResponse} (Card161), so the frontend can prefill a
 * vaccination row straight from this record.
 */
@Schema(description = "Одна запись о вакцинации.")
public record ImmunizationItem(
        @Schema(description = "Идентификатор ресурса Immunization в DHP FHIR.")
        String fhirId,

        @Schema(description = "Статус записи FHIR: completed / not-done.")
        String status,

        @Schema(description = "Код вакцины (первый coding из vaccineCode).")
        String vaccineCode,

        @Schema(description = "Наименование вакцины.")
        String vaccinationName,

        @Schema(description = "Серия (номер партии) препарата.")
        String serialNumber,

        @Schema(description = "Дата и время проведения вакцинации.")
        LocalDateTime vaccinationDate,

        @Schema(description = "Срок годности препарата.")
        LocalDate expirationDate,

        @Schema(description = "Объём введённой дозы.")
        BigDecimal doseVolume,

        @Schema(description = "Единица измерения дозы (например, ml).")
        String doseUnit,

        @Schema(description = "Порядковый номер дозы в схеме вакцинации.")
        Integer doseNumber,

        @Schema(description = "Заболевания, против которых проведена вакцинация.")
        List<String> targetDiseases,

        @Schema(description = "Исполнитель (медицинский работник или организация), если указан.")
        String performerName
) {
}
