package uz.uzinfocom.app.modules.patient.application.query.dto.detail;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Документ, удостоверяющий личность пациента (например, паспорт или ПИНФЛ).")
public record PatientIdentifierDetailResponse(
        @Schema(description = "Идентификатор записи документа.")
        Long id,

        @Schema(description = "UUID записи документа.")
        UUID uuid,

        @Schema(description = "Код типа документа, удостоверяющего личность.")
        String typeCode,

        @Schema(description = "Значение (номер) документа.")
        String value,

        @Schema(description = "Дата начала действия документа.")
        LocalDate periodStart,

        @Schema(description = "Дата окончания действия документа.")
        LocalDate periodEnd
) {
}
