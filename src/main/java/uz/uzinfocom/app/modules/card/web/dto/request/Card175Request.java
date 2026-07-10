package uz.uzinfocom.app.modules.card.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.modules.card.domain.enums.CardType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Карта 175 — карта эпидемиологического расследования случая укуса/повреждения животным.")
public record Card175Request(
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
        @Size(max = 255) String pathogenType,

        @Schema(description = "Код места, откуда прибыл пациент (по справочнику).")
        @Size(max = 64) String wherePatientComeCode,

        @Schema(description = "Код способа обращения пациента (по справочнику).")
        @Size(max = 64) String patientComeCode,

        @Schema(description = "Код статуса выписки из стационара (по справочнику).")
        @Size(max = 64) String hospitalDischargeStatusCode,

        @Schema(description = "Код вида транспортировки пациента (по справочнику).")
        @Size(max = 64) String transportTypeCode,

        @Schema(description = "Код причины исключения из режима домашней изоляции (по справочнику).")
        @Size(max = 64) String reasonOfLeavingHomeCode,

        @Schema(description = "Код причины поздней госпитализации (по справочнику).")
        @Size(max = 64) String reasonOfLateHospitalizationCode,

        @Schema(description = "Код способа подтверждения диагноза (по справочнику).")
        @Size(max = 64) String diagnosisConfirmedCode,

        @Schema(description = "Код сведений о вакцинации пациента (по справочнику).")
        @Size(max = 64) String informationAboutVaccinationCode,

        @Schema(description = "Сведения о последней проведённой вакцинации.")
        @Size(max = 500) String informationAboutLastVaccination,

        @Schema(description = "Краткое эпидемиологическое заключение по случаю.")
        @Size(max = 1000) String briefEpidemiologicalComment,

        @Schema(description = "Код первичного диагноза (по справочнику).")
        @Size(max = 64) String initialDiagnosisCode,

        @Schema(description = "Код способа выявления пациента (по справочнику).")
        @Size(max = 64) String patientIdentifiedCode,

        @Schema(description = "Место обращения пациента за медицинской помощью.")
        @Size(max = 500) String placeOfApplication,

        @Schema(description = "Код проведённых профилактических и лечебных мероприятий (по справочнику).")
        @Size(max = 64) String preventionAndAidCode,

        @Schema(description = "Наименование введённого препарата.")
        @Size(max = 255) String nameOfMedicine,

        @Schema(description = "Количество введённого препарата.")
        Long quantityOfMedicine,

        @Schema(description = "Серия/номер препарата.")
        Long serialNumber,

        @Schema(description = "Количество проведённых вакцинаций.")
        Long vaccinationCount,

        @Schema(description = "Клиническая форма заболевания.")
        @Size(max = 255) String clinicalForm,

        @Schema(description = "Локализация повреждения/укуса на теле пациента (может быть несколько).")
        List<String> partOfInjury,

        @Schema(description = "Код степени тяжести заболевания (по справочнику).")
        @Size(max = 64) String severityOfIllnessCode,

        @Schema(description = "Код связи заболевания с профессиональной деятельностью пациента (по справочнику).")
        @Size(max = 64) String relevanceOfDiseaseToProfessionCode,

        @Schema(description = "Код вида распространителя заболевания (по справочнику).")
        @Size(max = 64) String diseaseSpreaderTypeCode,

        @Schema(description = "Код принадлежности распространителя заболевания (по справочнику).")
        @Size(max = 64) String ownerOfDiseaseSpreaderCode,

        @Schema(description = "Код результата наблюдения за животным-распространителем (по справочнику).")
        @Size(max = 64) String observationResultOfAnimalsCode,

        @Schema(description = "Код способа проверки/уточнения диагноза (по справочнику).")
        @Size(max = 64) String checkingDiagnosisCode,

        @Schema(description = "Коды условий передачи заболевания (по справочнику), может быть несколько.")
        List<String> diseaseTransmissionConditionCode,

        @Schema(description = "Коды основных факторов передачи возбудителя (по справочнику), может быть несколько.")
        List<String> pathogenMainFactor,

        @Schema(description = "Коды мер, принятых по месту проживания пациента (по справочнику), может быть несколько.")
        List<String> takenMeasuresFromResidence
) implements CardRequest {

    public Card175Request {
        partOfInjury = immutableCopy(partOfInjury);
        diseaseTransmissionConditionCode = immutableCopy(diseaseTransmissionConditionCode);
        pathogenMainFactor = immutableCopy(pathogenMainFactor);
        takenMeasuresFromResidence = immutableCopy(takenMeasuresFromResidence);
    }

    @Override
    public CardType type() {
        return CardType.CARD175;
    }

    private static <T> List<T> immutableCopy(List<T> values) {
        return values == null ? List.of() : List.copyOf(values);
    }
}
