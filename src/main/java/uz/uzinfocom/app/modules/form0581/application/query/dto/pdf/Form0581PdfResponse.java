package uz.uzinfocom.app.modules.form0581.application.query.dto.pdf;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.modules.form0581.application.query.dto.detail.Form0581DiagnosisDetailResponse;
import uz.uzinfocom.app.modules.form0581.domain.enums.Form0581Status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = """
        Сведения для печатной формы №058-1 (экстренное извещение о случае, подозрительном на
        бешенство). Все коды приведены к человекочитаемым наименованиям (регион, район, пол,
        семейное положение, профессия, категория животного и т.д.) - в отличие от /{id}, который
        возвращает сырые коды справочников.
        """)
public record Form0581PdfResponse(
        @Schema(description = "Идентификатор формы.")
        Long id,

        @Schema(description = "UUID формы.")
        UUID uuid,

        @Schema(description = "Текущий статус формы в её жизненном цикле.")
        Form0581Status status,

        @Schema(description = "Наименование организации-отправителя (муассаса номи).")
        String institutionName,

        @Schema(description = "Наименование санитарно-эпидемиологической организации-получателя.")
        String sanepidOrganizationName,

        @Schema(description = "Диагностические сведения.")
        Form0581DiagnosisDetailResponse diagnosisInfo,

        @Schema(description = "Сведения о пациенте.")
        Form0581PdfPatientResponse patient,

        @Schema(description = "Постоянный адрес проживания пациента.")
        Form0581PdfAddressResponse permanentAddress,

        @Schema(description = "Текущий (временный) адрес проживания пациента.")
        Form0581PdfAddressResponse currentAddress,

        @Schema(description = """
                Место работы пациента (если сведения предоставлены). Null для пациентов без
                трудовой занятости, например для маленьких детей - см. также educationalInstitution.
                """)
        Form0581PdfWorkplaceResponse workplace,

        @Schema(description = """
                Место учёбы пациента - школа, детское учреждение и т.д. (если сведения
                предоставлены). Не является взаимоисключающим с workplace: у работающего
                учащегося могут быть заполнены оба поля.
                """)
        Form0581PdfWorkplaceResponse educationalInstitution,

        @Schema(description = "Сведения о происшествии.")
        Form0581PdfIncidentResponse incidentInfo,

        @Schema(description = "Сведения о животном.")
        Form0581PdfAnimalResponse animalInfo,

        @Schema(description = "Сведения о владельце животного.")
        Form0581PdfAnimalOwnerResponse animalOwnerInfo,

        @Schema(description = "Признак того, что в этом же происшествии пострадали и другие лица.")
        Boolean otherPeopleInjured,

        @Schema(description = "Список иных пострадавших лиц.")
        List<Form0581PdfOtherInjuredPersonResponse> otherInjuredPeople,

        @Schema(description = "Сведения о госпитализации пациента.")
        Form0581PdfHospitalizationResponse hospitalizationInfo,

        @Schema(description = "Сведения об оказанной антирабической помощи.")
        String antirabicAssistanceInfo,

        @Schema(description = """
                ФИО лица, сообщившего о случае (отправитель извещения) - сотрудник, создавший
                форму. Текстовое поле notifierFullName, введённое при создании формы,
                используется только как запасной вариант, если автор записи не определён.
                """)
        String notifierFullName,

        @Schema(description = "ФИО лица, принявшего извещение.")
        String receiverFullName,

        @Schema(description = "Дата и время отправки извещения.")
        LocalDateTime messageSentAt
) {
}
