package uz.uzinfocom.app.modules.card.application.query.dto.detail;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.modules.card.domain.enums.CardStatus;
import uz.uzinfocom.app.modules.card.domain.enums.CardType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Карта 175 — карта эпидемиологического расследования случая укуса/повреждения животным (полные сведения).")
public record Card175DetailResponse(
        @Schema(description = "Идентификатор карты.")
        Long id,

        @Schema(description = "Тип карты.")
        CardType type,

        @Schema(description = "Текущий статус карты в её жизненном цикле.")
        CardStatus status,

        @Schema(description = "Идентификатор формы №058, к которой привязана карта.")
        Long formId,

        @Schema(description = "Идентификатор супервайзера, назначившего карту.")
        Long assignedById,

        @Schema(description = "Комментарий супервайзера (например, причина отклонения при проверке).")
        String supervisorComment,

        @Schema(description = "Комментарий прикреплённого сотрудника (например, причина отказа от карты).")
        String attachedUserComment,

        @Schema(description = "Дата завершения заполнения карты.")
        LocalDate completedDate,

        @Schema(description = "Дата и время проведения эпидемиологического расследования.")
        LocalDateTime timeOfEpidemiologicalInvestigation,

        @Schema(description = "Дата начала заболевания.")
        LocalDate dateOfIllness,

        @Schema(description = "Дата подачи экстренного извещения о диагнозе.")
        LocalDateTime dateOfDiagnosisOfEmergencyDay,

        @Schema(description = "Дата установления окончательного диагноза.")
        LocalDateTime dateOfFinalDiagnosis,

        @Schema(description = "Дата выписки из стационара.")
        LocalDate dateOfDischargeFromHospital,

        @Schema(description = "Дата проведения вакцинации.")
        LocalDate dateOfVaccination,

        @Schema(description = "Вид возбудителя заболевания.")
        String pathogenType,

        @Schema(description = "Код места, откуда прибыл пациент (по справочнику).")
        String wherePatientComeCode,

        @Schema(description = "Код способа обращения пациента (по справочнику).")
        String patientComeCode,

        @Schema(description = "Код статуса выписки из стационара (по справочнику).")
        String hospitalDischargeStatusCode,

        @Schema(description = "Код вида транспортировки пациента (по справочнику).")
        String transportTypeCode,

        @Schema(description = "Код причины исключения из режима домашней изоляции (по справочнику).")
        String reasonOfLeavingHomeCode,

        @Schema(description = "Код причины поздней госпитализации (по справочнику).")
        String reasonOfLateHospitalizationCode,

        @Schema(description = "Код способа подтверждения диагноза (по справочнику).")
        String diagnosisConfirmedCode,

        @Schema(description = "Код сведений о вакцинации пациента (по справочнику).")
        String informationAboutVaccinationCode,

        @Schema(description = "Сведения о последней проведённой вакцинации.")
        String informationAboutLastVaccination,

        @Schema(description = "Краткое эпидемиологическое заключение по случаю.")
        String briefEpidemiologicalComment,

        @Schema(description = "Код первичного диагноза (по справочнику).")
        String initialDiagnosisCode,

        @Schema(description = "Код способа выявления пациента (по справочнику).")
        String patientIdentifiedCode,

        @Schema(description = "Место обращения пациента за медицинской помощью.")
        String placeOfApplication,

        @Schema(description = "Код проведённых профилактических и лечебных мероприятий (по справочнику).")
        String preventionAndAidCode,

        @Schema(description = "Наименование введённого препарата.")
        String nameOfMedicine,

        @Schema(description = "Количество введённого препарата.")
        Long quantityOfMedicine,

        @Schema(description = "Серия/номер препарата.")
        Long serialNumber,

        @Schema(description = "Количество проведённых вакцинаций.")
        Long vaccinationCount,

        @Schema(description = "Клиническая форма заболевания.")
        String clinicalForm,

        @Schema(description = "Локализация повреждения/укуса на теле пациента (может быть несколько).")
        List<String> partOfInjury,

        @Schema(description = "Код степени тяжести заболевания (по справочнику).")
        String severityOfIllnessCode,

        @Schema(description = "Код связи заболевания с профессиональной деятельностью пациента (по справочнику).")
        String relevanceOfDiseaseToProfessionCode,

        @Schema(description = "Код вида распространителя заболевания (по справочнику).")
        String diseaseSpreaderTypeCode,

        @Schema(description = "Код принадлежности распространителя заболевания (по справочнику).")
        String ownerOfDiseaseSpreaderCode,

        @Schema(description = "Код результата наблюдения за животным-распространителем (по справочнику).")
        String observationResultOfAnimalsCode,

        @Schema(description = "Код способа проверки/уточнения диагноза (по справочнику).")
        String checkingDiagnosisCode,

        @Schema(description = "Коды условий передачи заболевания (по справочнику), может быть несколько.")
        List<String> diseaseTransmissionConditionCode,

        @Schema(description = "Коды основных факторов передачи возбудителя (по справочнику), может быть несколько.")
        List<String> pathogenMainFactor,

        @Schema(description = "Коды мер, принятых по месту проживания пациента (по справочнику), может быть несколько.")
        List<String> takenMeasuresFromResidence
) implements CardDetailResponse {
}
