package uz.uzinfocom.app.modules.form058.web.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.modules.patient.web.request.PatientRequest;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Grouped to mirror the {@code Form058} entity's own embedded structure
 * ({@code diagnosisInfo}, {@code clinicalInfo}, {@code dateInfo},
 * {@code epidemicInfo}, {@code reportInfo}) rather than a flat field list —
 * the same shape the generic inbound-integration endpoint
 * ({@code InboundCreateForm058Request}) already uses. DMED's own dedicated
 * endpoint ({@code DmedCreateForm058Request}) is the one exception that keeps
 * the old flat shape, since DMED already integrates against it.
 */
@Schema(description = "Запрос на создание формы №058 — экстренного извещения об инфекционном заболевании.")
public record CreateForm058Request(
        @Schema(description = "Диагноз по МКБ-10.", requiredMode = Schema.RequiredMode.REQUIRED)
        @Valid
        @NotNull(message = "{validation.form058.diagnosis-info.required}")
        DiagnosisInfo diagnosisInfo,

        @Schema(description = "Сведения о пациенте, по которому регистрируется случай.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @Valid
        @NotNull(message = "{validation.form058.patient.required}")
        PatientRequest patient,

        @Schema(description = "Клинические сведения.", requiredMode = Schema.RequiredMode.REQUIRED)
        @Valid
        @NotNull(message = "{validation.form058.clinical-info.required}")
        ClinicalInfo clinicalInfo,

        @Schema(description = "Даты, связанные со случаем заболевания.", requiredMode = Schema.RequiredMode.REQUIRED)
        @Valid
        @NotNull(message = "{validation.form058.date-info.required}")
        DateInfo dateInfo,

        @Schema(description = "Идентификатор организации-отправителя формы.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "{validation.form058.sender-organization.required}")
        UUID senderOrganizationId,

        @Schema(description = "Идентификатор организации-получателя формы.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "{validation.form058.receiver-organization.required}")
        UUID receiverOrganizationId,

        @Schema(description = "Географические координаты и описание места выявления заболевания.")
        @Valid
        LocationRequest location,

        @Schema(description = "Сведения об эпидемиологической обстановке.", requiredMode = Schema.RequiredMode.REQUIRED)
        @Valid
        @NotNull(message = "{validation.form058.epidemic-info.required}")
        EpidemicInfo epidemicInfo,

        @Schema(description = "Сведения об извещении.", requiredMode = Schema.RequiredMode.REQUIRED)
        @Valid
        @NotNull(message = "{validation.form058.report-info.required}")
        ReportInfo reportInfo
) {

    @Schema(description = "Диагноз по МКБ-10.")
    public record DiagnosisInfo(
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
            Integer mkb10UsageLimit
    ) {
    }

    @Schema(description = "Клинические сведения.")
    public record ClinicalInfo(
            @Schema(description = "Признак лабораторного подтверждения диагноза.")
            Boolean labConfirmation,

            @Schema(description = "Идентификатор места госпитализации.")
            UUID hospitalPlaceId
    ) {
    }

    @Schema(description = "Даты, связанные со случаем заболевания.")
    public record DateInfo(
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

            @Schema(description = "Дата и время первичного сообщения о случае заболевания.",
                    requiredMode = Schema.RequiredMode.REQUIRED)
            @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
            @NotNull(message = "{validation.form058.initial-report-date-time.required}")
            @PastOrPresent(message = "{validation.form058.initial-report-date-time.past_or_present}")
            LocalDateTime initialReportDateTime
    ) {
    }

    @Schema(description = "Сведения об эпидемиологической обстановке.")
    public record EpidemicInfo(
            @Schema(description = "Код места возникновения заболевания (по справочнику).",
                    requiredMode = Schema.RequiredMode.REQUIRED)
            @NotBlank(message = "{validation.form058.disease-place-code.required}")
            @Size(max = 64, message = "{validation.form058.disease-place.size}")
            String diseasePlaceCode,

            @Schema(description = "Предполагаемая причина заболевания.")
            @Size(max = 2000, message = "{validation.form058.disease-cause.size}")
            String diseaseCause,

            @Schema(description = "Принятые противоэпидемические меры.")
            @Size(max = 2000, message = "{validation.form058.epidemic-measures.size}")
            String epidemicMeasures
    ) {
    }

    @Schema(description = "Сведения об извещении.")
    public record ReportInfo(
            @Schema(description = "ФИО лица, сообщившего о случае заболевания.",
                    requiredMode = Schema.RequiredMode.REQUIRED)
            @NotBlank(message = "{validation.form058.notifier-full-name.required}")
            @Size(max = 255, message = "{validation.form058.notifier-full-name.size}")
            String notifierFullName,

            @Schema(description = "Код формы журнала регистрации (по справочнику).",
                    requiredMode = Schema.RequiredMode.REQUIRED)
            @NotBlank(message = "{validation.form058.journal-form-code.required}")
            @Size(max = 64, message = "{validation.form058.journal-form-code.size}")
            String journalFormCode,

            @Schema(description = "Дополнительный комментарий к форме.")
            @Size(max = 2000, message = "{validation.form058.comment.size}")
            String comment
    ) {
    }
}
