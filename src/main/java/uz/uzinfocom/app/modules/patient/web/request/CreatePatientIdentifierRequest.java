package uz.uzinfocom.app.modules.patient.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreatePatientIdentifierRequest(

        @NotBlank(message = "{patient.identifier.type.required}")
        @Size(max = 30, message = "{patient.identifier.type.size}")
        String type,

        @NotBlank(message = "{patient.identifier.value.required}")
        @Size(max = 100, message = "{patient.identifier.value.size}")
        String value,

        LocalDate periodStart,

        LocalDate periodEnd

) {
}
