package uz.uzinfocom.app.modules.card.application.query.dto.detail;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.modules.card.application.query.dto.detail.card161.Card161RiskFactorResponse;
import uz.uzinfocom.app.modules.card.application.query.dto.detail.card161.ContactPersonResponse;
import uz.uzinfocom.app.modules.card.application.query.dto.detail.card161.EnvironmentalLabTestResponse;
import uz.uzinfocom.app.modules.card.application.query.dto.detail.card161.EnvironmentalSourceResponse;
import uz.uzinfocom.app.modules.card.application.query.dto.detail.card161.HomePreventiveMeasureResponse;
import uz.uzinfocom.app.modules.card.application.query.dto.detail.card161.InfectionSourceDetailResponse;
import uz.uzinfocom.app.modules.card.application.query.dto.detail.card161.InfectionSourceResponse;
import uz.uzinfocom.app.modules.card.application.query.dto.detail.card161.OutbreakDisinfectionMeasureResponse;
import uz.uzinfocom.app.modules.card.application.query.dto.detail.card161.ScreenedGroupResponse;
import uz.uzinfocom.app.modules.card.application.query.dto.detail.card161.VaccinationResponse;
import uz.uzinfocom.app.modules.card.domain.enums.CardStatus;
import uz.uzinfocom.app.modules.card.domain.enums.CardType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Карта 161 — карта эпидемиологического расследования инфекционного заболевания (полные сведения).")
public record Card161DetailResponse(
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

        @Schema(description = "Тип обращения (кто сообщил о случае заболевания).")
        String callerType,

        @Schema(description = "Признак постоянного проживания пациента в данном регионе.")
        Boolean isResident,

        @Schema(description = "Медицинское учреждение по месту проживания при лечении на дому.")
        String residentialTreatmentFacility,

        @Schema(description = "Дата и время выявления заболевания.")
        LocalDateTime diseaseDetectedDate,

        @Schema(description = "Код региона по классификатору административно-территориального деления.")
        String regionCode,

        @Schema(description = "Код района по классификатору административно-территориального деления.")
        String districtCode,

        @Schema(description = "Идентификатор поликлиники (медицинской организации), направившей сведения.")
        Long polyclinicId,

        @Schema(description = "Первичные симптомы, зафиксированные при выявлении заболевания.")
        String initialSymptoms,

        @Schema(description = "Код способа выявления заболевания (по справочнику).")
        String detectedCode,

        @Schema(description = "Дата проведения эпидемиологического обследования.")
        LocalDateTime epidemiologicalExamDate,

        @Schema(description = "Дата окончания медицинского наблюдения за пациентом/контактными лицами.")
        LocalDateTime observationEndDate,

        @Schema(description = "Дата установления окончательного диагноза.")
        LocalDateTime finalDiagnosisDate,

        @Schema(description = "Код способа доставки пациента в медицинское учреждение (по справочнику).")
        String deliveryMethodCode,

        @Schema(description = "Код причины, по которой домашний режим изоляции был исключён.")
        String homeStayExclusionReasonCode,

        @Schema(description = "Код причины позднего обращения/госпитализации пациента.")
        String lateAdmissionReasonCode,

        @Schema(description = "Код способа подтверждения диагноза (по справочнику).")
        String diagnosisVerifiedCode,

        @Schema(description = "Список сведений о вакцинации пациента.")
        List<VaccinationResponse> vaccinations,

        @Schema(description = "Начало предполагаемого периода заражения.")
        LocalDate estimatedInfectionDateFrom,

        @Schema(description = "Конец предполагаемого периода заражения.")
        LocalDate estimatedInfectionDateTo,

        @Schema(description = "Список факторов риска, связанных со случаем заболевания.")
        List<Card161RiskFactorResponse> riskFactors,

        @Schema(description = "Список возможных источников заражения (лиц/доноров).")
        List<InfectionSourceResponse> possibleInfectionSources,

        @Schema(description = "Список объектов окружающей среды (источники воды и пищи), подлежащих обследованию.")
        List<EnvironmentalSourceResponse> environmentalSources,

        @Schema(description = "Код жилищных условий пациента (по справочнику).")
        String livingConditionCode,

        @Schema(description = "Количество лиц, проживающих совместно с пациентом.")
        Integer numberOfPeople,

        @Schema(description = "Количество комнат в жилом помещении.")
        Integer numberOfRooms,

        @Schema(description = "Площадь жилого помещения.")
        String area,

        @Schema(description = "Код источника водоснабжения (по справочнику).")
        String waterSupplyCode,

        @Schema(description = "Код способа утилизации жидких бытовых отходов (по справочнику).")
        String liquidWasteDisposalTypeCode,

        @Schema(description = "Код способа утилизации твёрдых бытовых отходов (по справочнику).")
        String solidWasteDisposalTypeCode,

        @Schema(description = "Код санитарного состояния жилого помещения (по справочнику).")
        String roomConditionCode,

        @Schema(description = "Код состояния придомовой территории (по справочнику).")
        String yardConditionCode,

        @Schema(description = "Код санитарного состояния прилегающей местности (по справочнику).")
        String areaConditionCode,

        @Schema(description = "Наличие вшей в жилом помещении.")
        Boolean hasLice,

        @Schema(description = "Наличие иных насекомых-вредителей в жилом помещении.")
        Boolean hasOtherInsects,

        @Schema(description = "Наличие грызунов в жилом помещении.")
        Boolean hasRodents,

        @Schema(description = "Код основных факторов, способствовавших возникновению заболевания (по справочнику).")
        String importantCausesOfDiseaseCode,

        @Schema(description = "Код посещённых пациентом объектов (по справочнику).")
        String visitedObjectsCode,

        @Schema(description = "Степень плотности заселения территории проживания.")
        String denselyPopulated,

        @Schema(description = "Статус изоляции пациента.")
        String isolationStatus,

        @Schema(description = "Состояние системы водоснабжения по месту проживания.")
        String waterSupplyStatus,

        @Schema(description = "Состояние санитарного содержания территории/помещения.")
        String sanitaryMaintenance,

        @Schema(description = "Состояние канализационной системы по месту проживания.")
        String sewerageStatus,

        @Schema(description = "Условия хранения продуктов питания.")
        String foodStorage,

        @Schema(description = "Условия приготовления пищи.")
        String foodPreparation,

        @Schema(description = "Факторы, способствующие распространению заболевания.")
        String diseaseCausingFactors,

        @Schema(description = "Список результатов лабораторных исследований объектов окружающей среды.")
        List<EnvironmentalLabTestResponse> environmentalLabTests,

        @Schema(description = "Список контактных лиц, подлежащих наблюдению.")
        List<ContactPersonResponse> contactPersonDetails,

        @Schema(description = "Список обследованных групп населения (скрининг).")
        List<ScreenedGroupResponse> screenedGroups,

        @Schema(description = "Список профилактических мероприятий, проведённых на дому.")
        List<HomePreventiveMeasureResponse> homePreventiveMeasures,

        @Schema(description = "Список дезинфекционных мероприятий, проведённых в очаге заболевания.")
        List<OutbreakDisinfectionMeasureResponse> outbreakDisinfectionMeasures,

        @Schema(description = "Наименование стационарного лечебного учреждения.")
        String hospitalName,

        @Schema(description = "Код места заражения (по справочнику).")
        String infectionLocationCode,

        @Schema(description = "Код предполагаемого места заражения (по справочнику).")
        String probableInfectionLocationCode,

        @Schema(description = "Признак того, что источник заражения не установлен.")
        Boolean isInfectionSourceMissing,

        @Schema(description = "Детальные сведения об источнике заражения (заполняется, если источник не найден либо им является животное).")
        InfectionSourceDetailResponse infectionSourceDetail,

        @Schema(description = "Код основного предполагаемого фактора заражения (по справочнику).")
        String mainProbableInfectionFactorCode,

        @Schema(description = "Коды условий, способствовавших заражению (по справочнику), может быть несколько.")
        List<String> infectionCausingConditionCode,

        @Schema(description = "Код очаговой инфекции (по справочнику).")
        String outbreakInfectionCode,

        @Schema(description = "Код текущего статуса случая заболевания (по справочнику).")
        String caseStatusCode,

        @Schema(description = "ФИО врача-эпидемиолога, ответственного за расследование случая.")
        String epidemiologist,

        @Schema(description = "ФИО помощника врача-эпидемиолога.")
        String epidemiologistAssistant
) implements CardDetailResponse {
}
