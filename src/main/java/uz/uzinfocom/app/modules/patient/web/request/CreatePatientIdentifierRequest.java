package uz.uzinfocom.app.modules.patient.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "Документ, удостоверяющий личность пациента (например, паспорт или ПИНФЛ).")
public record CreatePatientIdentifierRequest(

        @Schema(description = "Тип документа, удостоверяющего личность.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "{patient.identifier.type.required}")
        @Size(max = 30, message = "{patient.identifier.type.size}")
        String type,

        @Schema(description = "Значение (номер) документа.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "{patient.identifier.value.required}")
        @Size(max = 100, message = "{patient.identifier.value.size}")
        String value,

        @Schema(description = "Дата начала действия документа.")
        LocalDate periodStart,

        @Schema(description = "Дата окончания действия документа.")
        LocalDate periodEnd

) {
}
