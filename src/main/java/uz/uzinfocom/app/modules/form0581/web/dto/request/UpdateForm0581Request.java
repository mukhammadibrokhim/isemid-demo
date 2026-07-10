package uz.uzinfocom.app.modules.form0581.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.modules.patient.web.request.PatientRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "Запрос на обновление формы №058-1. Все поля необязательны — "
        + "передаётся только то, что нужно изменить.")
public record UpdateForm0581Request(
        @Schema(description = "Код диагноза по МКБ-10.")
        @Size(max = 20, message = "{validation.form0581.mkb10-code.size}")
        String mkb10Code,

        @Schema(description = "Наименование диагноза по МКБ-10.")
        @Size(max = 512, message = "{validation.form0581.mkb10-name.size}")
        String mkb10Name,

        @Schema(description = "Локализация повреждения на теле пациента.")
        @Size(max = 500, message = "{validation.form0581.injury-localization.size}")
        String injuryLocalization,

        @Schema(description = "Дата и время получения укуса/травмы.")
        @PastOrPresent(message = "{validation.form0581.injury-date-time.past_or_present}")
        LocalDateTime injuryDateTime,

        @Schema(description = "Дата и время обращения в травматологический пункт (ДПУ).")
        @PastOrPresent(message = "{validation.form0581.dpu-visit-date-time.past_or_present}")
        LocalDateTime dpuVisitDateTime,

        @Schema(description = "Код региона, где произошёл укус (по справочнику).")
        @Size(max = 64, message = "{validation.form0581.injury-region-code.size}")
        String injuryRegionCode,

        @Schema(description = "Код района, где произошёл укус (по справочнику).")
        @Size(max = 64, message = "{validation.form0581.injury-district-code.size}")
        String injuryDistrictCode,

        @Schema(description = "Адрес места происшествия.")
        @Size(max = 1000, message = "{validation.form0581.injury-address.size}")
        String injuryAddress,

        @Schema(description = "Код категории животного (по справочнику).")
        @Size(max = 64, message = "{validation.form0581.animal-category-code.size}")
        String animalCategoryCode,

        @Schema(description = "Окрас животного.")
        @Size(max = 255, message = "{validation.form0581.animal-color.size}")
        String animalColor,

        @Schema(description = "Вид животного.")
        @Size(max = 255, message = "{validation.form0581.animal-type.size}")
        String animalType,

        @Schema(description = "Порода животного.")
        @Size(max = 255, message = "{validation.form0581.animal-breed.size}")
        String animalBreed,

        @Schema(description = "Сведения о владельце животного.")
        @Valid
        AnimalOwnerRequest animalOwner,

        @Schema(description = "Идентификатор организации-получателя формы. Должна быть учреждением "
                + "санитарно-эпидемиологической службы.")
        UUID receiverOrganizationId,

        @Schema(description = "Признак того, что в этом же происшествии пострадали и другие лица.")
        Boolean otherPeopleInjured,

        @Schema(description = "Список иных пострадавших лиц. Если поле не передано — существующий список "
                + "не изменяется; если передано (в том числе пустым списком) — полностью заменяет прежний.")
        @Valid
        List<OtherInjuredPersonRequest> otherInjuredPeople,

        @Schema(description = "Дата и время госпитализации пациента.")
        LocalDateTime hospitalizedAt,

        @Schema(description = "Идентификатор организации госпитализации пациента.")
        UUID hospitalOrganizationId,

        @Schema(description = "Сведения об оказанной антирабической помощи.")
        @Size(max = 2000, message = "{validation.form0581.antirabic-assistance-info.size}")
        String antirabicAssistanceInfo,

        @Schema(description = "ФИО лица, сообщившего о случае.")
        @Size(max = 255, message = "{validation.form0581.notifier-full-name.size}")
        String notifierFullName,

        @Schema(description = "ФИО лица, принявшего сообщение в организации-получателе.")
        @Size(max = 255, message = "{validation.form0581.receiver-full-name.size}")
        String receiverFullName,

        @Schema(description = "Дата и время отправки сообщения.")
        LocalDateTime messageSentAt,

        @Schema(description = "Сведения о пациенте для обновления.")
        @Valid
        PatientRequest patient
) {
}
