package uz.uzinfocom.isemid.features.card.models.card_tube;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import uz.uzinfocom.isemid.features.card.models.Card;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class CardTube extends Card {
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

    @OneToMany(mappedBy = "cardTube", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"cardTube"})
    @OrderBy("id DESC")
    @Builder.Default
    private List<XRay> preMBTChestXRay = new ArrayList<>();

    @OneToMany(mappedBy = "cardTube", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"cardTube"})
    @OrderBy("id DESC")
    @Builder.Default
    private List<TBHistory> previousTBHistory = new ArrayList<>();

    private String dispensaryGroup;
    private String dgMkb10Code;
    private String dgMkb10Name;

    @ElementCollection
    @CollectionTable(name = "card_tube_checkup_dates", joinColumns = @JoinColumn(name = "card_tube_id"))
    @Column(name = "checkup_date")
    private List<LocalDate> last2YearsCheckupDates = new ArrayList<>();

    private LocalDate retreatmentStartDate;
    private LocalDate retreatmentEndDate;
    private String dismissalDate;
    private LocalDate infoSentToWorkplaceDate;
    private String receivedBy;
    private LocalDate infoSentToClinicDate;

    @ElementCollection
    @CollectionTable(
            name = "card_tube_nutrition_type",
            joinColumns = @JoinColumn(name = "card_tube_id")
    )
    @Column(name = "code")  // Bu yerda `code` qiymatini saqlaymiz
    private List<String> nutritionTypesCode = new ArrayList<>();

    private String workConditionCode;
    private String familyBudgetCode;
    private String harmfulHabitCode;

    @OneToMany(mappedBy = "cardTube", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"cardTube"})
    @OrderBy("id DESC")
    @Builder.Default
    private List<InfectionSource> possibleInfectionSources = new ArrayList<>();
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

    @OneToMany(mappedBy = "cardTube", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"cardTube"})
    @OrderBy("id DESC")
    @Builder.Default
    private List<ContactMonitoring> contactMonitoringList = new ArrayList<>();

    private String recoveryPlanCode;
    private LocalDate startDate;
    private LocalDate endDate;
}
