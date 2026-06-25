package uz.uzinfocom.app.modules.form058.web.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.modules.patient.web.request.PatientRequest;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record CreateForm058Request(
        @NotBlank(message = "{validation.form058.mkb10-code.required}")
        @Size(max = 20, message = "{validation.form058.mkb10-code.size}")
        String mkb10Code,

        @NotBlank(message = "{validation.form058.mkb10-name.required}")
        @Size(max = 512, message = "{validation.form058.mkb10-name.size}")
        String mkb10Name,

        @NotNull(message = "{validation.form058.disease-date.required}")
        LocalDate diseaseDate,

        @NotNull(message = "{validation.form058.first-visit-date.required}")
        LocalDate firstVisitDate,

        @NotNull(message = "{validation.form058.visit-date.required}")
        LocalDate visitDate,

        @NotNull(message = "{validation.form058.initial-report-date-time.required}")
        Instant initialReportDateTime,

        @NotNull(message = "{validation.form058.sender-organization.required}")
        UUID senderOrganizationId,

        @NotNull(message = "{validation.form058.receiver-organization.required}")
        UUID receiverOrganizationId,

        Long hospitalPlaceId,

        @NotBlank(message = "{validation.form058.disease-place.required}")
        @Size(max = 512, message = "{validation.form058.disease-place.size}")
        String diseasePlace,

        @NotBlank(message = "{validation.form058.notifier-full-name.required}")
        @Size(max = 255, message = "{validation.form058.notifier-full-name.size}")
        String notifierFullName,

        @NotBlank(message = "{validation.form058.journal-form-code.required}")
        @Size(max = 64, message = "{validation.form058.journal-form-code.size}")
        String journalFormCode,

        @Size(max = 2000, message = "{validation.form058.comment.size}")
        String comment,

        @NotNull(message = "{validation.form058.patient.required}")
        @Valid
        PatientRequest patient,

        @Valid
        LocationRequest location
) {

}
