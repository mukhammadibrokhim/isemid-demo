package uz.uzinfocom.app.modules.patient.application.query.dto.detail;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Полное (немаскированное) значение документа пациента, выданное по явному запросу.")
public record RevealedPatientIdentifierResponse(
        @Schema(description = "Идентификатор записи документа.")
        Long id,

        @Schema(description = "Код типа документа, удостоверяющего личность.")
        String typeCode,

        @Schema(description = "Полное значение (номер) документа.")
        String value
) {
}
