package uz.uzinfocom.app.modules.card.web.dto.request.card161;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.platform.persistence.sync.ChildRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Field names shared with {@code ImmunizationItem} ({@code GET /v1/dhp/immunization})
 * match it one-to-one, so a record fetched from DHP can be submitted here as is.
 */
@Schema(description = "Сведения о вакцинации пациента.")
public record VaccinationRequest(
        @Schema(description = "Идентификатор записи. Не указывается (null) при добавлении новой записи; "
                + "указывается для обновления уже существующей.")
        Long id,

        @Schema(description = "Код подтверждения факта вакцинации (по справочнику).")
        @Size(max = 64) String vaccinationVerifiedCode,

        @Schema(description = "Наименование вакцины.")
        @Size(max = 255) String vaccinationName,

        @Schema(description = "Серийный номер препарата.")
        @Size(max = 100) String serialNumber,

        @Schema(description = "Дата и время проведения вакцинации.")
        LocalDateTime vaccinationDate,

        @Schema(description = "Объём введённой дозы препарата (допускается дробное значение, например 0.5).")
        @PositiveOrZero @Digits(integer = 7, fraction = 3) BigDecimal doseVolume,

        @Schema(description = "Признак того, что вакцинация проведена по установленному графику.")
        Boolean scheduled,

        @Schema(description = "Идентификатор исходного ресурса Immunization в DHP FHIR, если запись получена из DHP.")
        @Size(max = 64) String fhirId,

        @Schema(description = "Код вакцины (из DHP FHIR).")
        @Size(max = 64) String vaccineCode,

        @Schema(description = "Срок годности препарата.")
        LocalDate expirationDate,

        @Schema(description = "Единица измерения дозы (например, ml).")
        @Size(max = 32) String doseUnit,

        @Schema(description = "Порядковый номер дозы в схеме вакцинации.")
        @PositiveOrZero Integer doseNumber,

        @Schema(description = "Заболевания, против которых проведена вакцинация.")
        List<@Size(max = 255) String> targetDiseases,

        @Schema(description = "Исполнитель (медицинский работник или организация).")
        @Size(max = 255) String performerName
) implements ChildRequest {
}
