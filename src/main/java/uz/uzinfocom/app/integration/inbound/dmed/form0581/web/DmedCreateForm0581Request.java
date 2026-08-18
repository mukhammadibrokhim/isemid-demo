package uz.uzinfocom.app.integration.inbound.dmed.form0581.web;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.integration.inbound.common.web.IntegrationPatientRequest;
import uz.uzinfocom.app.modules.form0581.web.dto.request.AnimalOwnerRequest;
import uz.uzinfocom.app.modules.form0581.web.dto.request.OtherInjuredPersonRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DMED-specific form058-1 submission contract — a fixed, flat shape (as
 * opposed to the generic {@code /integration/v1/{source}/form-058-1}
 * endpoint's entity-mirroring nested structure), kept as its own dedicated
 * request type for the same reason as {@code DmedCreateForm058Request}.
 * Field-for-field identical to {@code InboundCreateForm0581Request}, minus
 * {@code senderOrganizationId} (never taken from the request body — see
 * {@code InboundCallerContext}) and with its {@code diagnosisInfo}/
 * {@code incidentInfo}/{@code animalInfo}/{@code reportInfo} groups
 * flattened to top-level fields. See {@code DmedForm0581Validator} for the
 * additional cross-field checks applied on top of these annotations.
 */
@Schema(description = "Запрос на создание формы №058-1 через интеграционный API DMED.")
public record DmedCreateForm0581Request(
        @Schema(description = "Код диагноза по МКБ-10.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "{validation.form0581.icd10-code.required}")
        @Size(max = 20, message = "{validation.form0581.icd10-code.size}")
        String mkb10Code,

        @Schema(description = "Наименование диагноза по МКБ-10.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "{validation.form0581.icd10-name.required}")
        @Size(max = 512, message = "{validation.form0581.icd10-name.size}")
        String mkb10Name,

        @Schema(description = "Локализация повреждения на теле пациента.")
        @Size(max = 500, message = "{validation.form0581.injury-localization.size}")
        String injuryLocalization,

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
        String injuryAddress,

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
        String animalBreed,

        @Schema(description = "Сведения о владельце животного (если он известен).")
        @Valid
        AnimalOwnerRequest animalOwnerInfo,

        @Schema(description = "Сведения о пациенте, по которому регистрируется случай.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @Valid
        @NotNull(message = "{validation.form0581.patient.required}")
        IntegrationPatientRequest patient,

        @Schema(description = "Идентификатор организации-получателя формы. Должна быть учреждением "
                + "санитарно-эпидемиологической службы.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "{validation.form0581.receiver-organization.required}")
        UUID receiverOrganizationId,

        @Schema(description = "Признак того, что в этом же происшествии пострадали и другие лица.")
        Boolean otherPeopleInjured,

        @Schema(description = "Список иных пострадавших лиц (помимо основного пациента).")
        @Valid
        List<OtherInjuredPersonRequest> otherInjuredPeople,

        @Schema(description = "Дата и время госпитализации пациента (при наличии).")
        LocalDateTime hospitalizedAt,

        @Schema(description = "Идентификатор организации госпитализации пациента (при наличии).")
        UUID hospitalOrganizationId,

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

    public DmedCreateForm0581Request {
        otherInjuredPeople = otherInjuredPeople == null ? List.of() : List.copyOf(otherInjuredPeople);
    }
}
