package uz.uzinfocom.app.modules.form058.web.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.modules.patient.web.request.PatientRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record UpdateForm058Request(
        @Size(max = 20, message = "{validation.form058.mkb10-code.size}")
        String mkb10Code,

        @Size(max = 512, message = "{validation.form058.mkb10-name.size}")
        String mkb10Name,

        @PastOrPresent(message = "{validation.form058.disease-date.past_or_present}")
        LocalDate diseaseDate,

        @PastOrPresent(message = "{validation.form058.first-visit-date.past_or_present}")
        LocalDate firstVisitDate,

        @PastOrPresent(message = "{validation.form058.visit-date.past_or_present}")
        LocalDate visitDate,

        @PastOrPresent(message = "{validation.form058.initial-report-date-time.past_or_present}")
        LocalDateTime initialReportDateTime,

        UUID receiverOrganizationId,

        @Positive(message = "{validation.form058.hospital-place-id.positive}")
        Long hospitalPlaceId,

        @Size(max = 64, message = "{validation.form058.disease-place.size}")
        String diseasePlaceCode,

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
}
