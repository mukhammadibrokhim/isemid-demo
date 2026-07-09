package uz.uzinfocom.app.modules.card.domain.model.card_tube;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import uz.uzinfocom.app.modules.card.domain.annotation.CatalogCode;
import uz.uzinfocom.app.modules.card.domain.enums.CardType;
import uz.uzinfocom.app.modules.card.domain.model.Card;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * "Card TB" — tuberculosis dispensary registration/monitoring card. Field
 * names are copied from the legacy {@code CardTube} entity (verified
 * against the on-disk legacy source; note {@code dispensaryId} is a
 * {@code String} there, not a {@code Long}), with fixes applied: the
 * legacy vaccination fields {@code doseVolume}/{@code scheduled} were
 * primitive {@code int}/{@code boolean} and are now nullable wrappers.
 */
@Getter
@Setter
@Entity
@Table(name = "card_tube")
public class CardTube extends Card {

    public CardTube() {
        super(CardType.CARD_TUBE);
    }

    @Column(name = "primary_dispensary_date")
    private LocalDate primaryDispensaryDate;

    @Column(name = "dispensary_id", length = 64)
    private String dispensaryId;

    @CatalogCode("mkb10")
    @Column(name = "mkb10_code", length = 64)
    private String mkb10Code;

    @Column(name = "mkb10_name", length = 500)
    private String mkb10Name;

    @Column(name = "first_mb_date")
    private LocalDate firstMBDate;

    @Column(name = "mb_detection_method", length = 255)
    private String mbDetectionMethod;

    @Column(name = "mb_patient_reg_date")
    private LocalDate mbPatientRegDate;

    @CatalogCode("card-tube-home-stay-reason")
    @Column(name = "home_stay_reason_code", length = 64)
    private String homeStayReasonCode;

    @Column(name = "discharge_date")
    private LocalDate dischargeDate;

    @Column(name = "vaccination_name", length = 255)
    private String vaccinationName;

    @Column(name = "serial_number", length = 100)
    private String serialNumber;

    @Column(name = "vaccination_date")
    private LocalDateTime vaccinationDate;

    /** Legacy fix: was a primitive {@code int}. */
    @Column(name = "dose_volume")
    private Integer doseVolume;

    /** Legacy fix: was a primitive {@code boolean}. */
    @Column(name = "scheduled")
    private Boolean scheduled;

    @OneToMany(mappedBy = "cardTube", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id DESC")
    private List<XRay> preMBTChestXRay = new ArrayList<>();

    @OneToMany(mappedBy = "cardTube", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id DESC")
    private List<TBHistory> previousTBHistory = new ArrayList<>();

    @Column(name = "dispensary_group", length = 255)
    private String dispensaryGroup;

    @CatalogCode("mkb10")
    @Column(name = "dg_mkb10_code", length = 64)
    private String dgMkb10Code;

    @Column(name = "dg_mkb10_name", length = 500)
    private String dgMkb10Name;

    @ElementCollection
    @CollectionTable(name = "card_tube_checkup_dates", joinColumns = @JoinColumn(name = "card_tube_id"))
    @Column(name = "checkup_date")
    private List<LocalDate> last2YearsCheckupDates = new ArrayList<>();

    @Column(name = "retreatment_start_date")
    private LocalDate retreatmentStartDate;

    @Column(name = "retreatment_end_date")
    private LocalDate retreatmentEndDate;

    @Column(name = "dismissal_date", length = 64)
    private String dismissalDate;

    @Column(name = "info_sent_to_workplace_date")
    private LocalDate infoSentToWorkplaceDate;

    @Column(name = "received_by", length = 255)
    private String receivedBy;

    @Column(name = "info_sent_to_clinic_date")
    private LocalDate infoSentToClinicDate;

    @ElementCollection
    @CollectionTable(name = "card_tube_nutrition_type", joinColumns = @JoinColumn(name = "card_tube_id"))
    @Column(name = "code")
    private List<String> nutritionTypesCode = new ArrayList<>();

    @CatalogCode("card-tube-work-condition")
    @Column(name = "work_condition_code", length = 64)
    private String workConditionCode;

    @CatalogCode("card-tube-family-budget")
    @Column(name = "family_budget_code", length = 64)
    private String familyBudgetCode;

    @CatalogCode("card-tube-harmful-habit")
    @Column(name = "harmful_habit_code", length = 64)
    private String harmfulHabitCode;

    @OneToMany(mappedBy = "cardTube", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id DESC")
    private List<InfectionSource> possibleInfectionSources = new ArrayList<>();

    @CatalogCode("card-tube-housing-condition")
    @Column(name = "housing_condition_code", length = 64)
    private String housingConditionCode;

    @Column(name = "room_count")
    private Integer roomCount;

    @Column(name = "floor_count")
    private Integer floorCount;

    @Column(name = "has_elevator")
    private Boolean hasElevator;

    @Column(name = "total_contact")
    private Integer totalContact;

    @Column(name = "household_contact")
    private Integer householdContact;

    @Column(name = "adult_count")
    private Integer adultCount;

    @Column(name = "teenager_count")
    private Integer teenagerCount;

    @Column(name = "children_under_14_count")
    private Integer childrenUnder14Count;

    @Column(name = "pregnant_women_count")
    private Integer pregnantWomenCount;

    @Column(name = "food_childcare_worker_count")
    private Integer foodChildcareWorkerCount;

    @Column(name = "family_room_count")
    private Integer familyRoomCount;

    @Column(name = "room_area_sq_m")
    private Integer roomAreaSqM;

    @Column(name = "total_area_sq_m")
    private Integer totalAreaSqM;

    @Column(name = "isolated_room_area_sq_m")
    private Integer isolatedRoomAreaSqM;

    @Column(name = "roommates_count")
    private Integer roommatesCount;

    @Column(name = "roommate_children_count")
    private Integer roommateChildrenCount;

    @CatalogCode("card-tube-sanitary-hygienic-assessment")
    @Column(name = "sanitary_hygienic_assessment_code", length = 64)
    private String sanitaryHygienicAssessmentCode;

    @CatalogCode("card-tube-heating-type")
    @Column(name = "heating_type_code", length = 64)
    private String heatingTypeCode;

    @CatalogCode("card-tube-sewerage-type")
    @Column(name = "sewerage_type_code", length = 64)
    private String sewerageTypeCode;

    @Column(name = "has_ventilation")
    private Boolean hasVentilation;

    @CatalogCode("card-tube-needs-renovation")
    @Column(name = "needs_renovation_code", length = 64)
    private String needsRenovationCode;

    @CatalogCode("card-tube-habitability")
    @Column(name = "habitability_code", length = 64)
    private String habitabilityCode;

    @Column(name = "housing_improvement_date")
    private LocalDate housingImprovementDate;

    @Column(name = "previous_housing_difference", length = 500)
    private String previousHousingDifference;

    @Column(name = "follows_cough_precaution")
    private Boolean followsCoughPrecaution;

    @Column(name = "has_spittoon")
    private Boolean hasSpittoon;

    @Column(name = "spittoon_count")
    private Integer spittoonCount;

    @Column(name = "uses_spittoon_at_work")
    private Boolean usesSpittoonAtWork;

    @Column(name = "uses_spittoon_at_home")
    private Boolean usesSpittoonAtHome;

    @Column(name = "uses_spittoon_in_pub_place")
    private Boolean usesSpittoonInPubPlace;

    @CatalogCode("card-tube-sputum-disposal-method")
    @Column(name = "sputum_disposal_method_code", length = 64)
    private String sputumDisposalMethodCode;

    @Column(name = "full_name", length = 255)
    private String fullName;

    @CatalogCode("card-tube-kinship-degree")
    @Column(name = "kinship_degree_code", length = 64)
    private String kinshipDegreeCode;

    @CatalogCode("card-tube-receives-disinfectant")
    @Column(name = "receives_disinfectant_code", length = 64)
    private String receivesDisinfectantCode;

    @Column(name = "disinfectant_amount_per_month")
    private Integer disinfectantAmountPerMonth;

    @Column(name = "disinfectant_provider", length = 255)
    private String disinfectantProvider;

    @Column(name = "visit_interval_value")
    private Integer visitIntervalValue;

    @Column(name = "visit_interval_unit", length = 32)
    private String visitIntervalUnit;

    @Column(name = "ftb_visit_interval_val")
    private Integer ftbVisitIntervalVal;

    @Column(name = "ftb_visit_interval_unit", length = 32)
    private String ftbVisitIntervalUnit;

    @OneToMany(mappedBy = "cardTube", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id DESC")
    private List<ContactMonitoring> contactMonitoringList = new ArrayList<>();

    @CatalogCode("card-tube-recovery-plan")
    @Column(name = "recovery_plan_code", length = 64)
    private String recoveryPlanCode;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;
}
