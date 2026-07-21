package uz.uzinfocom.app.integration.inbound.form0581.web;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.modules.form0581.web.dto.request.AnimalOwnerRequest;
import uz.uzinfocom.app.modules.form0581.web.dto.request.OtherInjuredPersonRequest;
import uz.uzinfocom.app.modules.patient.web.request.PatientRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Inbound-integration equivalent of {@code CreateForm0581Request} — same
 * business fields, minus {@code senderOrganizationId}: the sending
 * organization is never taken from the request body here, only from the
 * authenticated caller (see {@code InboundCallerContext}). Grouped to mirror
 * the {@code Form0581} entity's own embedded structure ({@code diagnosisInfo},
 * {@code incidentInfo}, {@code animalInfo}, {@code animalOwnerInfo},
 * {@code hospitalizationInfo}, {@code reportInfo}) rather than the frontend's
 * flat shape, since integration callers submit against the domain model
 * directly, not against a screen-shaped form. See
 * {@code InboundForm0581Validator} for the additional cross-field checks
 * applied on top of these annotations.
 */
@Schema(description = "Запрос на создание формы №058-1 через интеграционный API.")
public record InboundCreateForm0581Request(
        @Schema(description = "Диагноз по МКБ-10.", requiredMode = Schema.RequiredMode.REQUIRED)
        @Valid
        @NotNull(message = "{validation.form0581.diagnosis-info.required}")
        DiagnosisInfo diagnosisInfo,

        @Schema(description = "Сведения о происшествии.", requiredMode = Schema.RequiredMode.REQUIRED)
        @Valid
        @NotNull(message = "{validation.form0581.incident-info.required}")
        IncidentInfo incidentInfo,

        @Schema(description = "Сведения о животном.")
        @Valid
        AnimalInfo animalInfo,

        @Schema(description = "Сведения о владельце животного (если он известен).")
        @Valid
        AnimalOwnerRequest animalOwnerInfo,

        @Schema(description = "Сведения о пациенте, по которому регистрируется случай.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @Valid
        @NotNull(message = "{validation.form0581.patient.required}")
        PatientRequest patient,

        @Schema(description = "Идентификатор организации-получателя формы. Должна быть учреждением "
                + "санитарно-эпидемиологической службы.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "{validation.form0581.receiver-organization.required}")
        UUID receiverOrganizationId,

        @Schema(description = "Признак того, что в этом же происшествии пострадали и другие лица.")
        Boolean otherPeopleInjured,

        @Schema(description = "Список иных пострадавших лиц (помимо основного пациента).")
        @Valid
        List<OtherInjuredPersonRequest> otherInjuredPeople,

        @Schema(description = "Сведения о госпитализации.")
        @Valid
        HospitalizationInfo hospitalizationInfo,

        @Schema(description = "Сведения об извещении.", requiredMode = Schema.RequiredMode.REQUIRED)
        @Valid
        @NotNull(message = "{validation.form0581.report-info.required}")
        ReportInfo reportInfo
) {

    public InboundCreateForm0581Request {
        otherInjuredPeople = otherInjuredPeople == null ? List.of() : List.copyOf(otherInjuredPeople);
    }

    @Schema(description = "Диагноз по МКБ-10.")
    public record DiagnosisInfo(
            @Schema(description = "Код диагноза по МКБ-10.", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotBlank(message = "{validation.form0581.mkb10-code.required}")
            @Size(max = 20, message = "{validation.form0581.mkb10-code.size}")
            String mkb10Code,

            @Schema(description = "Наименование диагноза по МКБ-10.", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotBlank(message = "{validation.form0581.mkb10-name.required}")
            @Size(max = 512, message = "{validation.form0581.mkb10-name.size}")
            String mkb10Name,

            @Schema(description = "Локализация повреждения на теле пациента.")
            @Size(max = 500, message = "{validation.form0581.injury-localization.size}")
            String injuryLocalization
    ) {
    }

    @Schema(description = "Сведения о происшествии.")
    public record IncidentInfo(
            @Schema(description = "Дата и время получения укуса/травмы.", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotNull(message = "{validation.form0581.injury-date-time.required}")
            @PastOrPresent(message = "{validation.form0581.injury-date-time.past_or_present}")
            LocalDateTime injuryDateTime,

            @Schema(description = "Дата и время обращения в травматологический пункт (ДПУ).",
                    requiredMode = Schema.RequiredMode.REQUIRED)
            @NotNull(message = "{validation.form0581.dpu-visit-date-time.required}")
            @PastOrPresent(message = "{validation.form0581.dpu-visit-date-time.past_or_present}")
            LocalDateTime dpuVisitDateTime,

            @Schema(description = "Код региона, где произошёл укус (по справочнику).",
                    requiredMode = Schema.RequiredMode.REQUIRED)
            @NotBlank(message = "{validation.form0581.injury-region-code.required}")
            @Size(max = 64, message = "{validation.form0581.injury-region-code.size}")
            String injuryRegionCode,

            @Schema(description = "Код района, где произошёл укус (по справочнику).",
                    requiredMode = Schema.RequiredMode.REQUIRED)
            @NotBlank(message = "{validation.form0581.injury-district-code.required}")
            @Size(max = 64, message = "{validation.form0581.injury-district-code.size}")
            String injuryDistrictCode,

            @Schema(description = "Адрес места происшествия.")
            @Size(max = 1000, message = "{validation.form0581.injury-address.size}")
            String injuryAddress
    ) {
    }

    @Schema(description = "Сведения о животном.")
    public record AnimalInfo(
            @Schema(description = "Код категории животного (по справочнику: домашнее/дикое/безнадзорное и т. п.).")
            @Size(max = 64, message = "{validation.form0581.animal-category-code.size}")
            String animalCategoryCode,

            @Schema(description = "Окрас животного.")
            @Size(max = 255, message = "{validation.form0581.animal-color.size}")
            String animalColor,

            @Schema(description = "Вид животного (собака, кошка, лиса и т. п.).")
            @Size(max = 255, message = "{validation.form0581.animal-type.size}")
            String animalType,

            @Schema(description = "Порода животного.")
            @Size(max = 255, message = "{validation.form0581.animal-breed.size}")
            String animalBreed
    ) {
    }

    @Schema(description = "Сведения о госпитализации.")
    public record HospitalizationInfo(
            @Schema(description = "Дата и время госпитализации пациента (при наличии).")
            LocalDateTime hospitalizedAt,

            @Schema(description = "Идентификатор организации госпитализации пациента (при наличии).")
            UUID hospitalOrganizationId
    ) {
    }

    @Schema(description = "Сведения об извещении.")
    public record ReportInfo(
            @Schema(description = "Сведения об оказанной антирабической помощи.")
            @Size(max = 2000, message = "{validation.form0581.antirabic-assistance-info.size}")
            String antirabicAssistanceInfo,

            @Schema(description = "ФИО лица, сообщившего о случае.", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotBlank(message = "{validation.form0581.notifier-full-name.required}")
            @Size(max = 255, message = "{validation.form0581.notifier-full-name.size}")
            String notifierFullName,

            @Schema(description = "ФИО лица, принявшего сообщение в организации-получателе.")
            @Size(max = 255, message = "{validation.form0581.receiver-full-name.size}")
            String receiverFullName,

            @Schema(description = "Дата и время отправки сообщения.")
            LocalDateTime messageSentAt
    ) {
    }
}
