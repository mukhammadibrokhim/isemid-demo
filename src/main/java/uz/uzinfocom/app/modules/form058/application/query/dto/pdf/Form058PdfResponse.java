package uz.uzinfocom.app.modules.form058.application.query.dto.pdf;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.modules.form058.application.query.dto.detail.Form058DateDetailResponse;
import uz.uzinfocom.app.modules.form058.application.query.dto.detail.Form058DiagnosisDetailResponse;
import uz.uzinfocom.app.modules.form058.application.query.dto.detail.Form058LocationDetailResponse;
import uz.uzinfocom.app.modules.form058.domain.enums.FormStatus;

import java.util.UUID;

@Schema(description = """
        Сведения для печатной формы №058 (экстренное извещение). Все коды приведены к
        человекочитаемым наименованиям (регион, район, пол, семейное положение, профессия,
        место возникновения заболевания и т.д.) - в отличие от /{id}, который возвращает
        сырые коды справочников.
        """)
public record Form058PdfResponse(
        @Schema(description = "Идентификатор формы.")
        Long id,

        @Schema(description = "UUID формы.")
        UUID uuid,

        @Schema(description = "Текущий статус формы в её жизненном цикле.")
        FormStatus status,

        @Schema(description = "Наименование организации-отправителя (муассаса номи).")
        String institutionName,

        @Schema(description = "Наименование санитарно-эпидемиологической организации-получателя.")
        String sanepidOrganizationName,

        @Schema(description = "Диагностические сведения (первичный и итоговый диагноз).")
        Form058DiagnosisDetailResponse diagnosisInfo,

        @Schema(description = "Признак подтверждения диагноза лабораторными исследованиями.")
        Boolean labConfirmation,

        @Schema(description = "Сведения о пациенте.")
        Form058PdfPatientResponse patient,

        @Schema(description = "Постоянный адрес проживания пациента.")
        Form058PdfAddressResponse permanentAddress,

        @Schema(description = "Текущий (временный) адрес проживания пациента.")
        Form058PdfAddressResponse currentAddress,

        @Schema(description = """
                Место работы пациента (если сведения предоставлены). Null для пациентов без
                трудовой занятости, например для маленьких детей - см. также educationalInstitution.
                """)
        Form058PdfWorkplaceResponse workplace,

        @Schema(description = """
                Место учёбы пациента - школа, детское учреждение и т.д. (если сведения
                предоставлены). Не является взаимоисключающим с workplace: у работающего
                учащегося могут быть заполнены оба поля.
                """)
        Form058PdfWorkplaceResponse educationalInstitution,

        @Schema(description = "Ключевые даты формы.")
        Form058DateDetailResponse dateInfo,

        @Schema(description = "Наименование организации госпитализации.")
        String hospitalPlaceName,

        @Schema(description = "Наименование места возникновения заболевания.")
        String diseasePlaceName,

        @Schema(description = "Предполагаемая причина заболевания/отравления.")
        String diseaseCause,

        @Schema(description = "Принятые противоэпидемические меры и дополнительные сведения.")
        String epidemicMeasures,

        @Schema(description = """
                ФИО лица, сообщившего о случае заболевания (отправитель извещения) - сотрудник,
                создавший форму. Текстовое поле notifierFullName, введённое при создании формы,
                используется только как запасной вариант, если автор записи не определён.
                """)
        String notifierFullName,

        @Schema(description = """
                ФИО лица, принявшего извещение - сотрудник, которому назначена карта, привязанная
                к форме (принимающая сторона считается получившей извещение в момент привязки
                карты). Null, если к форме ещё не привязана ни одна карта.
                """)
        String receiverFullName,

        @Schema(description = "Код формы журнала регистрации.")
        String journalFormCode,

        @Schema(description = "Дополнительный комментарий к форме.")
        String comment,

        @Schema(description = "Географическое место выявления заболевания, включая широту/долготу.")
        Form058LocationDetailResponse location
) {
}
