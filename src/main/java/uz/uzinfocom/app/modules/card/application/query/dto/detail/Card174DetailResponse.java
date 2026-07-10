package uz.uzinfocom.app.modules.card.application.query.dto.detail;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.modules.card.application.query.dto.detail.card174.InfectionMonitoringResponse;
import uz.uzinfocom.app.modules.card.application.query.dto.detail.card174.OutbreakControlMeasureResponse;
import uz.uzinfocom.app.modules.card.domain.enums.CardStatus;
import uz.uzinfocom.app.modules.card.domain.enums.CardType;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "Карта 174 — карта эпизоотолого-эпидемиологического расследования зоонозного заболевания (полные сведения).")
public record Card174DetailResponse(
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

        @Schema(description = "Порядковый номер документа.")
        Integer serialDocNumber,

        @Schema(description = "Код диагноза по МКБ-10.")
        String mkb10Code,

        @Schema(description = "Наименование диагноза по МКБ-10.")
        String mkb10Name,

        @Schema(description = "Вид возбудителя заболевания.")
        String pathogenType,

        @Schema(description = "Дата получения сведений о случае.")
        LocalDate dataObtainedDate,

        @Schema(description = "Дата направления сообщения в ветеринарную службу.")
        LocalDate reportToVeterinaryDepartmentDate,

        @Schema(description = "Предварительный диагноз у животного.")
        String animalPrimaryDiagnosis,

        @Schema(description = "Предварительный диагноз у человека.")
        String humanPrimaryDiagnosis,

        @Schema(description = "Дата проведения расследования.")
        LocalDate investigationDate,

        @Schema(description = "Год последнего зарегистрированного случая заболевания на данной территории.")
        LocalDate lastDiseaseYear,

        @Schema(description = "Дата заражения животного в текущем случае.")
        LocalDate currentAnimalInfectionDate,

        @Schema(description = "Место локализации очага заболевания.")
        String outbreakLocalization,

        @Schema(description = "ФИО владельца животного.")
        String animalOwner,

        @Schema(description = "Адрес владельца животного.")
        String ownerAddress,

        @Schema(description = "Код вида поражённого животного (по справочнику).")
        String affectedAnimalTypeCode,

        @Schema(description = "Количество поражённых животных.")
        Integer affectedAnimalCount,

        @Schema(description = "Код формы собственности на животное (по справочнику).")
        String animalOwnershipCode,

        @Schema(description = "Признак того, что местность является нехарактерной (экзотической) для данного заболевания.")
        Boolean isAreaExotic,

        @Schema(description = "Признак роста численности грызунов на территории.")
        Boolean rodentIncrease,

        @Schema(description = "Признак роста численности переносчиков инфекции.")
        Boolean vectorIncrease,

        @Schema(description = "Признак роста численности диких грызунов.")
        Boolean wildRodentsIncrease,

        @Schema(description = "Признак роста численности синантропных грызунов.")
        Boolean synanthropicRodentsIncrease,

        @Schema(description = "Признак роста численности кровососущих членистоногих.")
        Boolean bloodSuckingArthropodsIncrease,

        @Schema(description = "Признак наличия эпизоотии на территории.")
        Boolean epizootologyExistence,

        @Schema(description = "Коды факторов, способствовавших возникновению заболевания (по справочнику).")
        List<String> diseaseFactorCodes,

        @Schema(description = "Вид животного.")
        String animalType,

        @Schema(description = "Дата проведения лабораторного исследования.")
        LocalDate testDate,

        @Schema(description = "Количество отобранных проб для исследования.")
        Integer testSampleCount,

        @Schema(description = "Метод проведённого исследования.")
        String testingMethod,

        @Schema(description = "Результат проведённого исследования.")
        String testResult,

        @Schema(description = "Коды поражённых видов животных (по справочнику).")
        List<String> affectedAnimalCodes,

        @Schema(description = "Количество пострадавших людей.")
        Integer affectedHumans,

        @Schema(description = "Из них — пострадавших в производственных условиях.")
        Integer includingIndustrialConditions,

        @Schema(description = "Из них — обратившихся за медицинской помощью самостоятельно.")
        Integer includingWhoApplied,

        @Schema(description = "Из них — выявленных активно (при подворных обходах и т.п.).")
        Integer includingIdentified,

        @Schema(description = "Количество пролеченных людей.")
        Integer treatedHumans,

        @Schema(description = "Количество пострадавших непосредственно в очаге заболевания.")
        Integer affectedInOutbreak,

        @Schema(description = "Список сведений о мониторинге пострадавших лиц.")
        List<InfectionMonitoringResponse> infectionMonitoring,

        @Schema(description = "Код вида установленного карантина (по справочнику).")
        String quarantineTypeCode,

        @Schema(description = "Дата начала карантина.")
        LocalDate quarantineStartDate,

        @Schema(description = "Дата окончания карантина.")
        LocalDate quarantineEndDate,

        @Schema(description = "Код способа утилизации животного (по справочнику).")
        String animalDisposalMethodCode,

        @Schema(description = "Дата утилизации животного.")
        LocalDate animalDisposalDate,

        @Schema(description = "Принятые меры предосторожности.")
        String precautionaryMeasures,

        @Schema(description = "Сведения об отлове бродячих животных.")
        String strayAnimalCapture,

        @Schema(description = "Сведения об отстреле диких животных.")
        String wildAnimalCulling,

        @Schema(description = "Код проведённой дератизации (по справочнику).")
        String deratizationCode,

        @Schema(description = "Площадь территории, охваченной дератизацией.")
        Double deratizationArea,

        @Schema(description = "Лица, проводившие обследование очага.")
        String inspectors,

        @Schema(description = "Сведения об изоляции пострадавших/контактных лиц.")
        String isolation,

        @Schema(description = "Сведения о сдаче мяса заражённого животного на исследование.")
        String meatSubmission,

        @Schema(description = "Сведения о проведённом лечении.")
        String treatment,

        @Schema(description = "Признак того, что мероприятие фактически было проведено.")
        Boolean measureTaken,

        @Schema(description = "Коды факторов передачи, подвергнутых дезинфекции (по справочнику).")
        List<String> disinfectionTransmissionFactorCodes,

        @Schema(description = "Объём обеззараженных факторов передачи.")
        Integer disinfectedFactorAmount,

        @Schema(description = "Дата проведения дезинфекции.")
        LocalDate disinfectionDate,

        @Schema(description = "Коды использованных методов уничтожения возбудителя/переносчиков (по справочнику).")
        List<String> eliminationMethodCodes,

        @Schema(description = "Место проведения мероприятия.")
        String locationOfEvent,

        @Schema(description = "Результаты контроля выполнения проведённых мероприятий.")
        String executionControlResults,

        @Schema(description = "Список мероприятий по ликвидации очага заболевания.")
        List<OutbreakControlMeasureResponse> outbreakControlMeasures,

        @Schema(description = "Дополнительная информация о принятых мерах.")
        String additionalMeasuresInfo
) implements CardDetailResponse {
}
