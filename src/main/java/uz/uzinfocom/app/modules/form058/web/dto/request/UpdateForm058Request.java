package uz.uzinfocom.app.modules.form058.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.modules.patient.web.request.PatientRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Запрос на обновление формы №058.")
public record UpdateForm058Request(
        @Schema(description = "Код диагноза по МКБ-10.")
        @Size(max = 20, message = "{validation.form058.mkb10-code.size}")
        String mkb10Code,

        @Schema(description = "Наименование диагноза по МКБ-10.")
        @Size(max = 512, message = "{validation.form058.mkb10-name.size}")
        String mkb10Name,

        @Schema(description = "Дата начала заболевания.")
        @PastOrPresent(message = "{validation.form058.disease-date.past_or_present}")
        LocalDate diseaseDate,

        @Schema(description = "Дата первого обращения пациента за медицинской помощью.")
        @PastOrPresent(message = "{validation.form058.first-visit-date.past_or_present}")
        LocalDate firstVisitDate,

        @Schema(description = "Дата осмотра пациента, по итогам которого заполнена форма.")
        @PastOrPresent(message = "{validation.form058.visit-date.past_or_present}")
        LocalDate visitDate,

        @Schema(description = "Дата и время первичного сообщения о случае заболевания.")
        @PastOrPresent(message = "{validation.form058.initial-report-date-time.past_or_present}")
        LocalDateTime initialReportDateTime,

        @Schema(description = "Идентификатор организации-получателя формы.")
        UUID receiverOrganizationId,

        @Schema(description = "Идентификатор места госпитализации.")
        @Positive(message = "{validation.form058.hospital-place-id.positive}")
        Long hospitalPlaceId,

        @Schema(description = "Код места возникновения заболевания (по справочнику).")
        @Size(max = 64, message = "{validation.form058.disease-place.size}")
        String diseasePlaceCode,

        @Schema(description = "ФИО лица, сообщившего о случае заболевания.")
        @Size(max = 255, message = "{validation.form058.notifier-full-name.size}")
        String notifierFullName,

        @Schema(description = "Код формы журнала регистрации (по справочнику).")
        @Size(max = 64, message = "{validation.form058.journal-form-code.size}")
        String journalFormCode,

        @Schema(description = "Дополнительный комментарий к форме.")
        @Size(max = 2000, message = "{validation.form058.comment.size}")
        String comment,

        @Schema(description = "Сведения о пациенте для обновления.")
        @Valid
        PatientRequest patient,

        @Schema(description = "Географическое место выявления заболевания.")
        @Valid
        LocationRequest location
) {
}
