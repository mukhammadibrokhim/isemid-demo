package uz.uzinfocom.app.modules.card.web.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.modules.card.domain.enums.CardType;
import uz.uzinfocom.app.modules.card.web.dto.request.card_tube.ContactMonitoringRequest;
import uz.uzinfocom.app.modules.card.web.dto.request.card_tube.InfectionSourceRequest;
import uz.uzinfocom.app.modules.card.web.dto.request.card_tube.TBHistoryRequest;
import uz.uzinfocom.app.modules.card.web.dto.request.card_tube.XRayRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record CardTubeRequest(
        LocalDate primaryDispensaryDate,
        @Size(max = 64) String dispensaryId,
        @Size(max = 64) String mkb10Code,
        @Size(max = 500) String mkb10Name,
        LocalDate firstMBDate,
        @Size(max = 255) String mbDetectionMethod,
        LocalDate mbPatientRegDate,
        @Size(max = 64) String homeStayReasonCode,
        LocalDate dischargeDate,
        @Size(max = 255) String vaccinationName,
        @Size(max = 100) String serialNumber,
        LocalDateTime vaccinationDate,
        Integer doseVolume,
        Boolean scheduled,
        @Valid List<XRayRequest> preMBTChestXRay,
        @Valid List<TBHistoryRequest> previousTBHistory,
        @Size(max = 255) String dispensaryGroup,
        @Size(max = 64) String dgMkb10Code,
        @Size(max = 500) String dgMkb10Name,
        List<LocalDate> last2YearsCheckupDates,
        LocalDate retreatmentStartDate,
        LocalDate retreatmentEndDate,
        @Size(max = 64) String dismissalDate,
        LocalDate infoSentToWorkplaceDate,
        @Size(max = 255) String receivedBy,
        LocalDate infoSentToClinicDate,
        List<String> nutritionTypesCode,
        @Size(max = 64) String workConditionCode,
        @Size(max = 64) String familyBudgetCode,
        @Size(max = 64) String harmfulHabitCode,
        @Valid List<InfectionSourceRequest> possibleInfectionSources,
        @Size(max = 64) String housingConditionCode,
        Integer roomCount,
        Integer floorCount,
        Boolean hasElevator,
        Integer totalContact,
        Integer householdContact,
        Integer adultCount,
        Integer teenagerCount,
        Integer childrenUnder14Count,
        Integer pregnantWomenCount,
        Integer foodChildcareWorkerCount,
        Integer familyRoomCount,
        Integer roomAreaSqM,
        Integer totalAreaSqM,
        Integer isolatedRoomAreaSqM,
        Integer roommatesCount,
        Integer roommateChildrenCount,
        @Size(max = 64) String sanitaryHygienicAssessmentCode,
        @Size(max = 64) String heatingTypeCode,
        @Size(max = 64) String sewerageTypeCode,
        Boolean hasVentilation,
        @Size(max = 64) String needsRenovationCode,
        @Size(max = 64) String habitabilityCode,
        LocalDate housingImprovementDate,
        @Size(max = 500) String previousHousingDifference,
        Boolean followsCoughPrecaution,
        Boolean hasSpittoon,
        Integer spittoonCount,
        Boolean usesSpittoonAtWork,
        Boolean usesSpittoonAtHome,
        Boolean usesSpittoonInPubPlace,
        @Size(max = 64) String sputumDisposalMethodCode,
        @Size(max = 255) String fullName,
        @Size(max = 64) String kinshipDegreeCode,
        @Size(max = 64) String receivesDisinfectantCode,
        Integer disinfectantAmountPerMonth,
        @Size(max = 255) String disinfectantProvider,
        Integer visitIntervalValue,
        @Size(max = 32) String visitIntervalUnit,
        Integer ftbVisitIntervalVal,
        @Size(max = 32) String ftbVisitIntervalUnit,
        @Valid List<ContactMonitoringRequest> contactMonitoringList,
        @Size(max = 64) String recoveryPlanCode,
        LocalDate startDate,
        LocalDate endDate
) implements CardRequest {

    public CardTubeRequest {
        preMBTChestXRay = immutableCopy(preMBTChestXRay);
        previousTBHistory = immutableCopy(previousTBHistory);
        last2YearsCheckupDates = immutableCopy(last2YearsCheckupDates);
        nutritionTypesCode = immutableCopy(nutritionTypesCode);
        possibleInfectionSources = immutableCopy(possibleInfectionSources);
        contactMonitoringList = immutableCopy(contactMonitoringList);
    }

    @Override
    public CardType type() {
        return CardType.CARD_TUBE;
    }

    private static <T> List<T> immutableCopy(List<T> values) {
        return values == null ? List.of() : List.copyOf(values);
    }
}
