package uz.uzinfocom.app.modules.form058.web.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record UpdateForm058Request(
        @Size(max = 20, message = "{validation.form058.mkb10-code.size}")
        String mkb10Code,

        @Size(max = 512, message = "{validation.form058.mkb10-name.size}")
        String mkb10Name,

        LocalDate diseaseDate,

        LocalDate firstVisitDate,

        LocalDate visitDate,

        Instant initialReportDateTime,

        UUID receiverOrganizationId,

        Long hospitalPlaceId,

        @Size(max = 512, message = "{validation.form058.disease-place.size}")
        String diseasePlace,

        @Size(max = 255, message = "{validation.form058.notifier-full-name.size}")
        String notifierFullName,

        @Size(max = 64, message = "{validation.form058.journal-form-code.size}")
        String journalFormCode,

        @Size(max = 2000, message = "{validation.form058.comment.size}")
        String comment,

        @Valid
        PatientRequest patient,

        @Valid
        LocationRequest location
) {

    public record PatientRequest(
            @Pattern(regexp = "\\d{14}", message = "{validation.nnuzb.format}")
            String nnuzb,

            @Pattern(regexp = "\\d{14}", message = "{validation.form058.patient.pinfl.format}")
            String pinfl,

            @Size(max = 255, message = "{validation.form058.patient.full-name.size}")
            String fullName,

            LocalDate birthDate,

            @Size(max = 32, message = "{validation.form058.patient.gender.size}")
            String gender,

            @Size(max = 64, message = "{validation.form058.patient.phone.size}")
            String phone
    ) {
    }
}
