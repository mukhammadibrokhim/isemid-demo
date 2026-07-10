package uz.uzinfocom.app.modules.card.application.query.dto.detail;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.modules.card.application.query.dto.detail.card_tube.ContactMonitoringResponse;
import uz.uzinfocom.app.modules.card.application.query.dto.detail.card_tube.InfectionSourceResponse;
import uz.uzinfocom.app.modules.card.application.query.dto.detail.card_tube.TBHistoryResponse;
import uz.uzinfocom.app.modules.card.application.query.dto.detail.card_tube.XRayResponse;
import uz.uzinfocom.app.modules.card.domain.enums.CardStatus;
import uz.uzinfocom.app.modules.card.domain.enums.CardType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Карта туберкулёзного диспансерного больного (Card Tube) — полные сведения.")
public record CardTubeDetailResponse(
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

        @Schema(description = "Дата первичной постановки на диспансерный учёт.")
        LocalDate primaryDispensaryDate,

        @Schema(description = "Идентификатор диспансерного учёта.")
        String dispensaryId,

        @Schema(description = "Код диагноза по МКБ-10.")
        String mkb10Code,

        @Schema(description = "Наименование диагноза по МКБ-10.")
        String mkb10Name,

        @Schema(description = "Дата первого обнаружения микобактерий туберкулёза (МБТ).")
        LocalDate firstMBDate,

        @Schema(description = "Метод обнаружения микобактерий туберкулёза.")
        String mbDetectionMethod,

        @Schema(description = "Дата регистрации пациента с бактериовыделением.")
        LocalDate mbPatientRegDate,

        @Schema(description = "Код причины оставления пациента на домашнем режиме (по справочнику).")
        String homeStayReasonCode,

        @Schema(description = "Дата выписки из стационара.")
        LocalDate dischargeDate,

        @Schema(description = "Наименование вакцины (БЦЖ).")
        String vaccinationName,

        @Schema(description = "Серийный номер вакцины.")
        String serialNumber,

        @Schema(description = "Дата и время проведения вакцинации.")
        LocalDateTime vaccinationDate,

        @Schema(description = "Объём введённой дозы препарата.")
        Integer doseVolume,

        @Schema(description = "Признак проведения вакцинации по установленному графику.")
        Boolean scheduled,

        @Schema(description = "Список результатов рентгенографии грудной клетки, выполненных до выявления МБТ.")
        List<XRayResponse> preMBTChestXRay,

        @Schema(description = "Список сведений о ранее перенесённых случаях туберкулёза.")
        List<TBHistoryResponse> previousTBHistory,

        @Schema(description = "Диспансерная группа учёта.")
        String dispensaryGroup,

        @Schema(description = "Код сопутствующего диагноза диспансерной группы по МКБ-10.")
        String dgMkb10Code,

        @Schema(description = "Наименование сопутствующего диагноза диспансерной группы по МКБ-10.")
        String dgMkb10Name,

        @Schema(description = "Даты профилактических осмотров за последние 2 года.")
        List<LocalDate> last2YearsCheckupDates,

        @Schema(description = "Дата начала повторного курса лечения.")
        LocalDate retreatmentStartDate,

        @Schema(description = "Дата окончания повторного курса лечения.")
        LocalDate retreatmentEndDate,

        @Schema(description = "Дата снятия с диспансерного учёта.")
        String dismissalDate,

        @Schema(description = "Дата направления информации по месту работы/учёбы пациента.")
        LocalDate infoSentToWorkplaceDate,

        @Schema(description = "Лицо, получившее информацию по месту работы/учёбы.")
        String receivedBy,

        @Schema(description = "Дата направления информации в поликлинику по месту жительства.")
        LocalDate infoSentToClinicDate,

        @Schema(description = "Коды видов организованного питания пациента (по справочнику).")
        List<String> nutritionTypesCode,

        @Schema(description = "Код условий труда пациента (по справочнику).")
        String workConditionCode,

        @Schema(description = "Код уровня семейного бюджета пациента (по справочнику).")
        String familyBudgetCode,

        @Schema(description = "Код вредных привычек пациента (по справочнику).")
        String harmfulHabitCode,

        @Schema(description = "Список возможных источников заражения (туберкулёзный контакт).")
        List<InfectionSourceResponse> possibleInfectionSources,

        @Schema(description = "Код жилищных условий пациента (по справочнику).")
        String housingConditionCode,

        @Schema(description = "Количество комнат в жилом помещении.")
        Integer roomCount,

        @Schema(description = "Количество этажей в доме.")
        Integer floorCount,

        @Schema(description = "Наличие лифта в доме.")
        Boolean hasElevator,

        @Schema(description = "Общее количество контактных лиц.")
        Integer totalContact,

        @Schema(description = "Количество контактных лиц по месту проживания.")
        Integer householdContact,

        @Schema(description = "Количество взрослых среди контактных лиц.")
        Integer adultCount,

        @Schema(description = "Количество подростков среди контактных лиц.")
        Integer teenagerCount,

        @Schema(description = "Количество детей до 14 лет среди контактных лиц.")
        Integer childrenUnder14Count,

        @Schema(description = "Количество беременных женщин среди контактных лиц.")
        Integer pregnantWomenCount,

        @Schema(description = "Количество работников пищевой промышленности/детских учреждений среди контактных лиц.")
        Integer foodChildcareWorkerCount,

        @Schema(description = "Количество комнат, занимаемых семьёй пациента.")
        Integer familyRoomCount,

        @Schema(description = "Площадь комнаты пациента, кв. м.")
        Integer roomAreaSqM,

        @Schema(description = "Общая площадь жилого помещения, кв. м.")
        Integer totalAreaSqM,

        @Schema(description = "Площадь изолированной комнаты пациента, кв. м.")
        Integer isolatedRoomAreaSqM,

        @Schema(description = "Количество лиц, совместно проживающих с пациентом.")
        Integer roommatesCount,

        @Schema(description = "Количество детей среди совместно проживающих лиц.")
        Integer roommateChildrenCount,

        @Schema(description = "Код санитарно-гигиенической оценки жилого помещения (по справочнику).")
        String sanitaryHygienicAssessmentCode,

        @Schema(description = "Код вида отопления жилого помещения (по справочнику).")
        String heatingTypeCode,

        @Schema(description = "Код вида канализации жилого помещения (по справочнику).")
        String sewerageTypeCode,

        @Schema(description = "Наличие вентиляции в жилом помещении.")
        Boolean hasVentilation,

        @Schema(description = "Код необходимости проведения ремонта жилого помещения (по справочнику).")
        String needsRenovationCode,

        @Schema(description = "Код степени пригодности жилья для проживания (по справочнику).")
        String habitabilityCode,

        @Schema(description = "Дата фактического улучшения жилищных условий.")
        LocalDate housingImprovementDate,

        @Schema(description = "Отличие текущих жилищных условий от прежних.")
        String previousHousingDifference,

        @Schema(description = "Соблюдение пациентом правил кашлевой гигиены.")
        Boolean followsCoughPrecaution,

        @Schema(description = "Наличие плевательницы у пациента.")
        Boolean hasSpittoon,

        @Schema(description = "Количество выданных пациенту плевательниц.")
        Integer spittoonCount,

        @Schema(description = "Использование плевательницы на рабочем месте.")
        Boolean usesSpittoonAtWork,

        @Schema(description = "Использование плевательницы дома.")
        Boolean usesSpittoonAtHome,

        @Schema(description = "Использование плевательницы в общественных местах.")
        Boolean usesSpittoonInPubPlace,

        @Schema(description = "Код способа обеззараживания мокроты (по справочнику).")
        String sputumDisposalMethodCode,

        @Schema(description = "ФИО лица, осуществляющего уход/контроль за пациентом.")
        String fullName,

        @Schema(description = "Код степени родства данного лица с пациентом (по справочнику).")
        String kinshipDegreeCode,

        @Schema(description = "Код получения дезинфицирующих средств (по справочнику).")
        String receivesDisinfectantCode,

        @Schema(description = "Количество выдаваемых дезинфицирующих средств в месяц.")
        Integer disinfectantAmountPerMonth,

        @Schema(description = "Организация, предоставляющая дезинфицирующие средства.")
        String disinfectantProvider,

        @Schema(description = "Периодичность патронажных посещений (числовое значение).")
        Integer visitIntervalValue,

        @Schema(description = "Единица измерения периодичности патронажных посещений (например, недели).")
        String visitIntervalUnit,

        @Schema(description = "Периодичность посещений врача-фтизиатра (числовое значение).")
        Integer ftbVisitIntervalVal,

        @Schema(description = "Единица измерения периодичности посещений врача-фтизиатра.")
        String ftbVisitIntervalUnit,

        @Schema(description = "Список сведений о наблюдении за контактными лицами.")
        List<ContactMonitoringResponse> contactMonitoringList,

        @Schema(description = "Код плана оздоровления пациента (по справочнику).")
        String recoveryPlanCode,

        @Schema(description = "Дата начала периода наблюдения/мероприятия.")
        LocalDate startDate,

        @Schema(description = "Дата окончания периода наблюдения/мероприятия.")
        LocalDate endDate
) implements CardDetailResponse {
}
