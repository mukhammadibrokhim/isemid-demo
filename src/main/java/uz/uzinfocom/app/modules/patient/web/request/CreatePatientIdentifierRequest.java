package uz.uzinfocom.app.modules.patient.web.request;

import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreatePatientIdentifierRequest(

        @Size(max = 64)
        String type,

        @Size(max = 255)
        String value,

        LocalDate periodStart,

        LocalDate periodEnd

) {
}
