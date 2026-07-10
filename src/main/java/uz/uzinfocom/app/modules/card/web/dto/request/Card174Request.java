package uz.uzinfocom.app.modules.card.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.modules.card.domain.enums.CardType;
import uz.uzinfocom.app.modules.card.web.dto.request.card174.InfectionMonitoringRequest;
import uz.uzinfocom.app.modules.card.web.dto.request.card174.OutbreakControlMeasureRequest;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "Карта 174 — карта эпизоотолого-эпидемиологического расследования зоонозного заболевания.")
public record Card174Request(
        @Schema(description = "Порядковый номер документа.")
        Integer serialDocNumber,

        @Schema(description = "Код диагноза по МКБ-10.")
        @Size(max = 64) String mkb10Code,

        @Schema(description = "Наименование диагноза по МКБ-10.")
        @Size(max = 500) String mkb10Name,

        @Schema(description = "Вид возбудителя заболевания.")
        @Size(max = 255) String pathogenType,

        @Schema(description = "Дата получения сведений о случае.")
        LocalDate dataObtainedDate,

        @Schema(description = "Дата направления сообщения в ветеринарную службу.")
        LocalDate reportToVeterinaryDepartmentDate,

        @Schema(description = "Предварительный диагноз у животного.")
        @Size(max = 500) String animalPrimaryDiagnosis,

        @Schema(description = "Предварительный диагноз у человека.")
        @Size(max = 500) String humanPrimaryDiagnosis,

        @Schema(description = "Дата проведения расследования.")
        LocalDate investigationDate,

        @Schema(description = "Год последнего зарегистрированного случая заболевания на данной территории.")
        LocalDate lastDiseaseYear,

        @Schema(description = "Дата заражения животного в текущем случае.")
        LocalDate currentAnimalInfectionDate,

        @Schema(description = "Место локализации очага заболевания.")
        @Size(max = 500) String outbreakLocalization,

        @Schema(description = "ФИО владельца животного.")
        @Size(max = 255) String animalOwner,

        @Schema(description = "Адрес владельца животного.")
        @Size(max = 500) String ownerAddress,

        @Schema(description = "Код вида поражённого животного (по справочнику).")
        @Size(max = 64) String affectedAnimalTypeCode,

        @Schema(description = "Количество поражённых животных.")
        Integer affectedAnimalCount,

        @Schema(description = "Код формы собственности на животное (по справочнику).")
        @Size(max = 64) String animalOwnershipCode,

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
        @Size(max = 255) String animalType,

        @Schema(description = "Дата проведения лабораторного исследования.")
        LocalDate testDate,

        @Schema(description = "Количество отобранных проб для исследования.")
        Integer testSampleCount,

        @Schema(description = "Метод проведённого исследования.")
        @Size(max = 255) String testingMethod,

        @Schema(description = "Результат проведённого исследования.")
        @Size(max = 500) String testResult,

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
        @Valid List<InfectionMonitoringRequest> infectionMonitoring,

        @Schema(description = "Код вида установленного карантина (по справочнику).")
        @Size(max = 64) String quarantineTypeCode,

        @Schema(description = "Дата начала карантина.")
        LocalDate quarantineStartDate,

        @Schema(description = "Дата окончания карантина.")
        LocalDate quarantineEndDate,

        @Schema(description = "Код способа утилизации животного (по справочнику).")
        @Size(max = 64) String animalDisposalMethodCode,

        @Schema(description = "Дата утилизации животного.")
        LocalDate animalDisposalDate,

        @Schema(description = "Принятые меры предосторожности.")
        @Size(max = 1000) String precautionaryMeasures,

        @Schema(description = "Сведения об отлове бродячих животных.")
        @Size(max = 500) String strayAnimalCapture,

        @Schema(description = "Сведения об отстреле диких животных.")
        @Size(max = 500) String wildAnimalCulling,

        @Schema(description = "Код проведённой дератизации (по справочнику).")
        @Size(max = 64) String deratizationCode,

        @Schema(description = "Площадь территории, охваченной дератизацией.")
        Double deratizationArea,

        @Schema(description = "Лица, проводившие обследование очага.")
        @Size(max = 500) String inspectors,

        @Schema(description = "Сведения об изоляции пострадавших/контактных лиц.")
        @Size(max = 500) String isolation,

        @Schema(description = "Сведения о сдаче мяса заражённого животного на исследование.")
        @Size(max = 500) String meatSubmission,

        @Schema(description = "Сведения о проведённом лечении.")
        @Size(max = 500) String treatment,

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
        @Size(max = 500) String locationOfEvent,

        @Schema(description = "Результаты контроля выполнения проведённых мероприятий.")
        @Size(max = 1000) String executionControlResults,

        @Schema(description = "Список мероприятий по ликвидации очага заболевания.")
        @Valid List<OutbreakControlMeasureRequest> outbreakControlMeasures,

        @Schema(description = "Дополнительная информация о принятых мерах.")
        @Size(max = 1000) String additionalMeasuresInfo
) implements CardRequest {

    public Card174Request {
        diseaseFactorCodes = immutableCopy(diseaseFactorCodes);
        affectedAnimalCodes = immutableCopy(affectedAnimalCodes);
        infectionMonitoring = immutableCopy(infectionMonitoring);
        disinfectionTransmissionFactorCodes = immutableCopy(disinfectionTransmissionFactorCodes);
        eliminationMethodCodes = immutableCopy(eliminationMethodCodes);
        outbreakControlMeasures = immutableCopy(outbreakControlMeasures);
    }

    @Override
    public CardType type() {
        return CardType.CARD174;
    }

    private static <T> List<T> immutableCopy(List<T> values) {
        return values == null ? List.of() : List.copyOf(values);
    }
}
