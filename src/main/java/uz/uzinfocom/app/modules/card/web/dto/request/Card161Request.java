package uz.uzinfocom.app.modules.card.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.modules.card.domain.enums.CardType;
import uz.uzinfocom.app.modules.card.web.dto.request.card161.Card161RiskFactorRequest;
import uz.uzinfocom.app.modules.card.web.dto.request.card161.ContactPersonRequest;
import uz.uzinfocom.app.modules.card.web.dto.request.card161.EmergencyProphylaxisRequest;
import uz.uzinfocom.app.modules.card.web.dto.request.card161.EnvironmentalLabTestRequest;
import uz.uzinfocom.app.modules.card.web.dto.request.card161.EnvironmentalSourceRequest;
import uz.uzinfocom.app.modules.card.web.dto.request.card161.HomePreventiveMeasureRequest;
import uz.uzinfocom.app.modules.card.web.dto.request.card161.InfectionSourceDetailRequest;
import uz.uzinfocom.app.modules.card.web.dto.request.card161.InfectionSourceRequest;
import uz.uzinfocom.app.modules.card.web.dto.request.card161.OutbreakDisinfectionMeasureRequest;
import uz.uzinfocom.app.modules.card.web.dto.request.card161.ScreenedGroupRequest;
import uz.uzinfocom.app.modules.card.web.dto.request.card161.VaccinationRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Карта 161 — карта эпидемиологического расследования инфекционного заболевания.")
public record Card161Request(
        @Schema(description = "Тип обращения (кто сообщил о случае заболевания).")
        @Size(max = 100) String callerType,

        @Schema(description = "Признак постоянного проживания пациента в данном регионе.")
        Boolean isResident,

        @Schema(description = "Медицинское учреждение по месту проживания при лечении на дому.")
        @Size(max = 500) String residentialTreatmentFacility,

        @Schema(description = "Дата и время выявления заболевания.")
        LocalDateTime diseaseDetectedDate,

        @Schema(description = "Код региона по классификатору административно-территориального деления.")
        @Size(max = 20) String regionCode,

        @Schema(description = "Код района по классификатору административно-территориального деления.")
        @Size(max = 20) String districtCode,

        @Schema(description = "Идентификатор поликлиники (медицинской организации), направившей сведения.")
        Long polyclinicId,

        @Schema(description = "Первичные симптомы, зафиксированные при выявлении заболевания.")
        @Size(max = 1000) String initialSymptoms,

        @Schema(description = "Код способа выявления заболевания (по справочнику).")
        @Size(max = 64) String detectedCode,

        @Schema(description = "Дата проведения эпидемиологического обследования.")
        LocalDateTime epidemiologicalExamDate,

        @Schema(description = "Дата окончания медицинского наблюдения за пациентом/контактными лицами.")
        LocalDateTime observationEndDate,

        @Schema(description = "Дата установления окончательного диагноза.")
        LocalDateTime finalDiagnosisDate,

        @Schema(description = "Код способа доставки пациента в медицинское учреждение (по справочнику).")
        @Size(max = 64) String deliveryMethodCode,

        @Schema(description = "Код причины, по которой домашний режим изоляции был исключён.")
        @Size(max = 64) String homeStayExclusionReasonCode,

        @Schema(description = "Код причины позднего обращения/госпитализации пациента.")
        @Size(max = 64) String lateAdmissionReasonCode,

        @Schema(description = "Код способа подтверждения диагноза (по справочнику).")
        @Size(max = 64) String diagnosisVerifiedCode,

        @Schema(description = "Список сведений о вакцинации пациента.")
        @Valid List<VaccinationRequest> vaccinations,

        @Schema(description = "Начало предполагаемого периода заражения.")
        LocalDate estimatedInfectionDateFrom,

        @Schema(description = "Конец предполагаемого периода заражения.")
        LocalDate estimatedInfectionDateTo,

        @Schema(description = "Список факторов риска, связанных со случаем заболевания.")
        @Valid List<Card161RiskFactorRequest> riskFactors,

        @Schema(description = "Список возможных источников заражения (лиц/доноров).")
        @Valid List<InfectionSourceRequest> possibleInfectionSources,

        @Schema(description = "Список объектов окружающей среды (источники воды и пищи), подлежащих обследованию.")
        @Valid List<EnvironmentalSourceRequest> environmentalSources,

        @Schema(description = "Код жилищных условий пациента (по справочнику).")
        @Size(max = 64) String livingConditionCode,

        @Schema(description = "Количество лиц, проживающих совместно с пациентом.")
        Integer numberOfPeople,

        @Schema(description = "Количество комнат в жилом помещении.")
        Integer numberOfRooms,

        @Schema(description = "Площадь жилого помещения.")
        @Size(max = 64) String area,

        @Schema(description = "Код источника водоснабжения (по справочнику).")
        @Size(max = 64) String waterSupplyCode,

        @Schema(description = "Код способа утилизации жидких бытовых отходов (по справочнику).")
        @Size(max = 64) String liquidWasteDisposalTypeCode,

        @Schema(description = "Код способа утилизации твёрдых бытовых отходов (по справочнику).")
        @Size(max = 64) String solidWasteDisposalTypeCode,

        @Schema(description = "Код санитарного состояния жилого помещения (по справочнику).")
        @Size(max = 64) String roomConditionCode,

        @Schema(description = "Код состояния придомовой территории (по справочнику).")
        @Size(max = 64) String yardConditionCode,

        @Schema(description = "Код санитарного состояния прилегающей местности (по справочнику).")
        @Size(max = 64) String areaConditionCode,

        @Schema(description = "Наличие вшей в жилом помещении.")
        Boolean hasLice,

        @Schema(description = "Наличие иных насекомых-вредителей в жилом помещении.")
        Boolean hasOtherInsects,

        @Schema(description = "Наличие грызунов в жилом помещении.")
        Boolean hasRodents,

        @Schema(description = "Код основных факторов, способствовавших возникновению заболевания (по справочнику).")
        @Size(max = 64) String importantCausesOfDiseaseCode,

        @Schema(description = "Код посещённых пациентом объектов (по справочнику).")
        @Size(max = 64) String visitedObjectsCode,

        @Schema(description = "Степень плотности заселения территории проживания.")
        @Size(max = 64) String denselyPopulated,

        @Schema(description = "Статус изоляции пациента.")
        @Size(max = 255) String isolationStatus,

        @Schema(description = "Состояние системы водоснабжения по месту проживания.")
        @Size(max = 255) String waterSupplyStatus,

        @Schema(description = "Состояние санитарного содержания территории/помещения.")
        @Size(max = 255) String sanitaryMaintenance,

        @Schema(description = "Состояние канализационной системы по месту проживания.")
        @Size(max = 255) String sewerageStatus,

        @Schema(description = "Условия хранения продуктов питания.")
        @Size(max = 255) String foodStorage,

        @Schema(description = "Условия приготовления пищи.")
        @Size(max = 255) String foodPreparation,

        @Schema(description = "Факторы, способствующие распространению заболевания.")
        @Size(max = 1000) String diseaseCausingFactors,

        @Schema(description = "Список результатов лабораторных исследований объектов окружающей среды.")
        @Valid List<EnvironmentalLabTestRequest> environmentalLabTests,

        @Schema(description = "Список контактных лиц, подлежащих наблюдению.")
        @Valid List<ContactPersonRequest> contactPersonDetails,

        @Schema(description = "Список обследованных групп населения (скрининг).")
        @Valid List<ScreenedGroupRequest> screenedGroups,

        @Schema(description = "Список профилактических мероприятий, проведённых на дому.")
        @Valid List<HomePreventiveMeasureRequest> homePreventiveMeasures,

        @Schema(description = "Список дезинфекционных мероприятий, проведённых в очаге заболевания.")
        @Valid List<OutbreakDisinfectionMeasureRequest> outbreakDisinfectionMeasures,

        @Schema(description = "Наименование стационарного лечебного учреждения.")
        @Size(max = 255) String hospitalName,

        @Schema(description = "Код места заражения (по справочнику).")
        @Size(max = 64) String infectionLocationCode,

        @Schema(description = "Код предполагаемого места заражения (по справочнику).")
        @Size(max = 64) String probableInfectionLocationCode,

        @Schema(description = "Признак того, что источник заражения не установлен.")
        Boolean isInfectionSourceMissing,

        @Schema(description = "Детальные сведения об источнике заражения (заполняется, если источник не найден либо им является животное).")
        @Valid InfectionSourceDetailRequest infectionSourceDetail,

        @Schema(description = "Код основного предполагаемого фактора заражения (по справочнику).")
        @Size(max = 64) String mainProbableInfectionFactorCode,

        @Schema(description = "Коды условий, способствовавших заражению (по справочнику), может быть несколько.")
        List<String> infectionCausingConditionCode,

        @Schema(description = "Код очаговой инфекции (по справочнику).")
        @Size(max = 64) String outbreakInfectionCode,

        @Schema(description = "Код текущего статуса случая заболевания (по справочнику).")
        @Size(max = 64) String caseStatusCode,

        @Schema(description = "ФИО врача-эпидемиолога, ответственного за расследование случая.")
        @Size(max = 255) String epidemiologist,

        @Schema(description = "ФИО помощника врача-эпидемиолога.")
        @Size(max = 255) String epidemiologistAssistant,

        // Поля листа-приложения №178 (зоонозное заболевание) — заполняются только для случаев зоонозного заболевания.
        @Schema(description = "Признак того, что пациенту оказана экстренная профилактическая/антирабическая помощь.")
        Boolean emergencyProphylaxisGiven,

        @Schema(description = "Список сведений о проведении экстренной профилактической/антирабической помощи.")
        @Valid List<EmergencyProphylaxisRequest> emergencyProphylaxisTreatments,

        @Schema(description = "Клиническая форма заболевания.")
        @Size(max = 255) String clinicalForm,

        @Schema(description = "Коды локализации повреждения (укуса), может быть несколько (по справочнику).")
        List<String> injuryLocationCodes,

        @Schema(description = "Код степени тяжести течения заболевания (по справочнику).")
        @Size(max = 64) String diseaseSeverityCode,

        @Schema(description = "Признак профессиональной принадлежности заболевания.")
        Boolean isOccupationalDisease,

        @Schema(description = "Сведения об источнике заболевания.")
        @Size(max = 1000) String diseaseSourceInfo,

        @Schema(description = "Код принадлежности животного (по справочнику).")
        @Size(max = 64) String animalOwnershipCode,

        @Schema(description = "Код результата наблюдения за животным (по справочнику).")
        @Size(max = 64) String animalObservationResultCode,

        @Schema(description = "Код результата лабораторного исследования животного (по справочнику).")
        @Size(max = 64) String animalLabTestResultCode
) implements CardRequest {

    public Card161Request {
        vaccinations = immutableCopy(vaccinations);
        riskFactors = immutableCopy(riskFactors);
        possibleInfectionSources = immutableCopy(possibleInfectionSources);
        environmentalSources = immutableCopy(environmentalSources);
        environmentalLabTests = immutableCopy(environmentalLabTests);
        contactPersonDetails = immutableCopy(contactPersonDetails);
        screenedGroups = immutableCopy(screenedGroups);
        homePreventiveMeasures = immutableCopy(homePreventiveMeasures);
        outbreakDisinfectionMeasures = immutableCopy(outbreakDisinfectionMeasures);
        infectionCausingConditionCode = immutableCopy(infectionCausingConditionCode);
        emergencyProphylaxisTreatments = immutableCopy(emergencyProphylaxisTreatments);
        injuryLocationCodes = immutableCopy(injuryLocationCodes);
    }

    @Override
    public CardType type() {
        return CardType.CARD161;
    }

    private static <T> List<T> immutableCopy(List<T> values) {
        return values == null ? List.of() : List.copyOf(values);
    }
}
