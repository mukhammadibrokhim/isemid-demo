package uz.uzinfocom.app.modules.form058.web.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.modules.patient.web.request.PatientRequest;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateForm058Request(
        @NotBlank(message = "{validation.form058.mkb10-code.required}")
        @Size(max = 20, message = "{validation.form058.mkb10-code.size}")
        String mkb10Code,

        @NotBlank(message = "{validation.form058.mkb10-name.required}")
        @Size(max = 512, message = "{validation.form058.mkb10-name.size}")
        String mkb10Name,

        @PositiveOrZero(message = "{validation.form058.mkb10-usage-limit.positive_or_zero}")
        Integer mkb10UsageLimit,

        @Valid
        @NotNull(message = "{validation.form058.patient.required}")
        PatientRequest patient,

        Boolean labConfirmation,

        UUID hospitalPlaceId,

        @PastOrPresent(message = "{validation.form058.admission-date.past_or_present}")
        LocalDateTime admissionDate,


        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        @NotNull(message = "{validation.form058.disease-date.required}")
        @PastOrPresent(message = "{validation.form058.disease-date.past_or_present}")
        LocalDateTime diseaseDate,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        @NotNull(message = "{validation.form058.first-visit-date.required}")
        @PastOrPresent(message = "{validation.form058.first-visit-date.past_or_present}")
        LocalDateTime firstVisitDate,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        @PastOrPresent(message = "{validation.form058.diagnosis-date.past_or_present}")
        LocalDateTime diagnosisDate,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        @NotNull(message = "{validation.form058.visit-date.required}")
        @PastOrPresent(message = "{validation.form058.visit-date.past_or_present}")
        LocalDateTime visitDate,

        @NotNull(message = "{validation.form058.sender-organization.required}")
        UUID senderOrganizationId,

        @NotNull(message = "{validation.form058.receiver-organization.required}")
        UUID receiverOrganizationId,

        @Valid
        LocationRequest location,

        @NotBlank(message = "{validation.form058.disease-place-code.required}")
        @Size(max = 64, message = "{validation.form058.disease-place.size}")
        String diseasePlaceCode,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        @NotNull(message = "{validation.form058.initial-report-date-time.required}")
        @PastOrPresent(message = "{validation.form058.initial-report-date-time.past_or_present}")
        LocalDateTime initialReportDateTime,

        @Size(max = 2000, message = "{validation.form058.disease-cause.size}")
        String diseaseCause,

        @Size(max = 2000, message = "{validation.form058.epidemic-measures.size}")
        String epidemicMeasures,

        @NotBlank(message = "{validation.form058.notifier-full-name.required}")
        @Size(max = 255, message = "{validation.form058.notifier-full-name.size}")
        String notifierFullName,

        @NotBlank(message = "{validation.form058.journal-form-code.required}")
        @Size(max = 64, message = "{validation.form058.journal-form-code.size}")
        String journalFormCode,

        @Size(max = 2000, message = "{validation.form058.comment.size}")
        String comment
) {
}
