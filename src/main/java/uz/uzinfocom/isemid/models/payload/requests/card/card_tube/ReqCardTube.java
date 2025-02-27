package uz.uzinfocom.isemid.models.payload.requests.card.card_tube;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;
import uz.uzinfocom.isemid.models.entities.value_set.Catalog;
import uz.uzinfocom.isemid.models.payload.requests.card.ReqCard;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Request object for CARD_TUBE type card")
public class ReqCardTube extends ReqCard {
    private LocalDate primaryDispensaryDate;
    private String dispensaryId;
    private String mkb10Code;
    private String mkb10Name;
    private LocalDate firstMBDate;
    private String mbDetectionMethod;
    private LocalDate mbPatientRegDate;

    private String homeStayReasonCode;
    private LocalDate dischargeDate;
    //vaccination
    private String vaccinationName;   // Preparat nomi
    private String serialNumber;      // Seriya raqami
    private LocalDateTime vaccinationDate; // Vaqti (GMT+5 Toshkent)
    private int doseVolume;           // Doza hajmi
    private boolean scheduled;

    private List<ReqXRay> preMBTChestXRay;
    private List<ReqTBHistory> previousTBHistory;

    private String dispensaryGroup;
    private String dgMkb10Code;
    private String dgMkb10Name;
    private List<LocalDate> last2YearsCheckupDates;
    private LocalDate retreatmentStartDate;
    private LocalDate retreatmentEndDate;
    private String dismissalDate;
    private LocalDate infoSentToWorkplaceDate;
    private String receivedBy;
    private LocalDate infoSentToClinicDate;

    private List<String> nutritionTypeCodes;
    private String workConditionCode;
    private String familyBudgetCode;
    private String harmfulHabitCode;

    private List<ReqInfectionSource> possibleInfectionSources;
    private String housingConditionCode;
    private Integer roomCount;
    private Integer floorCount;
    private Boolean hasElevator;
    private Integer totalContact;
    private Integer householdContact;
    private Integer adultCount;
    private Integer teenagerCount;
    private Integer childrenUnder14Count;
    private Integer pregnantWomenCount;
    private Integer foodChildcareWorkerCount;
    private Integer familyRoomCount;
    private Integer roomAreaSqM;
    private Integer totalAreaSqM;
    private Integer isolatedRoomAreaSqM;
    private Integer roommatesCount;
    private Integer roommateChildrenCount;
    private String sanitaryHygienicAssessmentCode;
    private String heatingTypeCode;
    private String sewerageTypeCode;
    private Boolean hasVentilation;
    private String needsRenovationCode;
    private String habitabilityCode;
    private LocalDate housingImprovementDate;
    private String previousHousingDifference;
    private Boolean followsCoughPrecaution;
    private Boolean hasSpittoon;
    private Integer spittoonCount;
    private Boolean usesSpittoonAtWork;
    private Boolean usesSpittoonAtHome;
    private Boolean usesSpittoonInPubPlace;
    private String sputumDisposalMethodCode;

    private String fullName;
    private String kinshipDegreeCode;

    private String receivesDisinfectantCode;
    private Integer disinfectantAmountPerMonth;
    private String disinfectantProvider;

    private Integer visitIntervalValue; // Masalan: 2
    private String visitIntervalUnit;   // Masalan: "weeks"

    private Integer ftbVisitIntervalVal; // Masalan: 2
    private String ftbVisitIntervalUnit;   // Masalan: "weeks"

    private List<ReqContactMonitoring> contactMonitoringList;
    private String recoveryPlanCode;
    private LocalDate startDate;
    private LocalDate endDate;
}
