package uz.uzinfocom.app.integration.inbound.dmed.form058.web;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.modules.form058.web.dto.request.LocationRequest;
import uz.uzinfocom.app.modules.patient.web.request.PatientRequest;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DMED-specific form058 submission contract — a fixed, flat shape (as opposed
 * to the generic {@code /integration/v1/{source}/form-058} endpoint's
 * entity-mirroring nested structure), kept as its own dedicated request type
 * since DMED already integrates against this exact contract and it must not
 * shift under it. Field-for-field identical to the original inbound request
 * shape, minus {@code senderOrganizationId}: the sending organization is
 * never taken from the request body, only from the authenticated caller (see
 * {@code InboundCallerContext}). See {@code DmedForm058Validator} for the
 * additional cross-field checks applied on top of these annotations.
 */
@Schema(description = "Запрос на создание формы №058 через интеграционный API DMED.")
public record DmedCreateForm058Request(
        @Schema(description = "Код диагноза по МКБ-10.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "{validation.form058.mkb10-code.required}")
        @Size(max = 20, message = "{validation.form058.mkb10-code.size}")
        String mkb10Code,

        @Schema(description = "Наименование диагноза по МКБ-10.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "{validation.form058.mkb10-name.required}")
        @Size(max = 512, message = "{validation.form058.mkb10-name.size}")
        String mkb10Name,

        @Schema(description = "Лимит использования данного кода МКБ-10 (при наличии ограничений).")
        @PositiveOrZero(message = "{validation.form058.mkb10-usage-limit.positive_or_zero}")
        Integer mkb10UsageLimit,

        @Schema(description = "Итоговый (подтверждённый) код диагноза по МКБ-10, если он уже известен "
                + "на момент отправки и отличается от предварительного mkb10Code. Если не указан, "
                + "принимается равным mkb10Code.")
        String finalMkb10Code,

        @Schema(description = "Итоговое (подтверждённое) наименование диагноза по МКБ-10 — см. finalMkb10Code.")
        String finalMkb10Name,

        @Schema(description = "Сведения о пациенте, по которому регистрируется случай.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @Valid
        @NotNull(message = "{validation.form058.patient.required}")
        PatientRequest patient,

        @Schema(description = "Признак лабораторного подтверждения диагноза.")
        Boolean labConfirmation,

        @Schema(description = "Идентификатор места госпитализации.")
        UUID hospitalPlaceId,

        @Schema(description = "Дата и время поступления пациента.")
        @PastOrPresent(message = "{validation.form058.admission-date.past_or_present}")
        LocalDateTime admissionDate,

        @Schema(description = "Дата и время начала заболевания.", requiredMode = Schema.RequiredMode.REQUIRED)
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        @NotNull(message = "{validation.form058.disease-date.required}")
        @PastOrPresent(message = "{validation.form058.disease-date.past_or_present}")
        LocalDateTime diseaseDate,

        @Schema(description = "Дата и время первого обращения пациента за медицинской помощью.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        @NotNull(message = "{validation.form058.first-visit-date.required}")
        @PastOrPresent(message = "{validation.form058.first-visit-date.past_or_present}")
        LocalDateTime firstVisitDate,

        @Schema(description = "Дата и время установления диагноза.")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        @PastOrPresent(message = "{validation.form058.diagnosis-date.past_or_present}")
        LocalDateTime diagnosisDate,

        @Schema(description = "Дата и время осмотра пациента, по итогам которого заполнена форма.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        @NotNull(message = "{validation.form058.visit-date.required}")
        @PastOrPresent(message = "{validation.form058.visit-date.past_or_present}")
        LocalDateTime visitDate,

        @Schema(description = "Идентификатор организации-получателя формы.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "{validation.form058.receiver-organization.required}")
        UUID receiverOrganizationId,

        @Schema(description = "Географические координаты и описание места выявления заболевания.")
        @Valid
        LocationRequest location,

        @Schema(description = "Код места возникновения заболевания (по справочнику).",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "{validation.form058.disease-place-code.required}")
        @Size(max = 64, message = "{validation.form058.disease-place.size}")
        String diseasePlaceCode,

        @Schema(description = "Дата и время первичного сообщения о случае заболевания.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        @NotNull(message = "{validation.form058.initial-report-date-time.required}")
        @PastOrPresent(message = "{validation.form058.initial-report-date-time.past_or_present}")
        LocalDateTime initialReportDateTime,

        @Schema(description = "Предполагаемая причина заболевания.")
        @Size(max = 2000, message = "{validation.form058.disease-cause.size}")
        String diseaseCause,

        @Schema(description = "Принятые противоэпидемические меры.")
        @Size(max = 2000, message = "{validation.form058.epidemic-measures.size}")
        String epidemicMeasures,

        @Schema(description = "ФИО лица, сообщившего о случае заболевания.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "{validation.form058.notifier-full-name.required}")
        @Size(max = 255, message = "{validation.form058.notifier-full-name.size}")
        String notifierFullName,

        @Schema(description = "Код формы журнала регистрации (по справочнику).", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "{validation.form058.journal-form-code.required}")
        @Size(max = 64, message = "{validation.form058.journal-form-code.size}")
        String journalFormCode,

        @Schema(description = "Дополнительный комментарий к форме.")
        @Size(max = 2000, message = "{validation.form058.comment.size}")
        String comment
) {
}
