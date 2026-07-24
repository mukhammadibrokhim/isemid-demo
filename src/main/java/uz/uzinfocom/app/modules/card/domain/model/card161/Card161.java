package uz.uzinfocom.app.modules.card.domain.model.card161;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import uz.uzinfocom.app.modules.card.domain.annotation.CatalogCode;
import uz.uzinfocom.app.modules.card.domain.enums.CardType;
import uz.uzinfocom.app.modules.card.domain.model.Card;
import uz.uzinfocom.app.platform.iam.domain.Organization;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * "Card 161" — infectious disease investigation card. Field names/types are
 * copied from the legacy {@code Card161} entity (verified against the
 * on-disk legacy source, not the initially-pasted spec — see the plan
 * document), with the fixes listed there applied.
 */
@Getter
@Setter
@Entity
@Table(name = "card161")
public class Card161 extends Card {

    public Card161() {
        super(CardType.CARD161);
    }

    @Column(name = "caller_type", length = 100)
    private String callerType;

    @Column(name = "is_resident")
    private Boolean isResident;

    @Column(name = "residential_treatment_facility", length = 500)
    private String residentialTreatmentFacility;

    @Column(name = "disease_detected_date")
    private LocalDateTime diseaseDetectedDate;

    @Column(name = "region_code", length = 20)
    private String regionCode;

    @Column(name = "district_code", length = 20)
    private String districtCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "polyclinic_id", insertable = false, updatable = false)
    private Organization polyclinic;

    @Column(name = "polyclinic_id")
    private Long polyclinicId;

    @Column(name = "initial_symptoms", length = 1000)
    private String initialSymptoms;

    @CatalogCode("card161-detected")
    @Column(name = "detected_code", length = 64)
    private String detectedCode;

    @Column(name = "epidemiological_exam_date")
    private LocalDateTime epidemiologicalExamDate;

    @Column(name = "observation_end_date")
    private LocalDateTime observationEndDate;

    @Column(name = "final_diagnosis_date")
    private LocalDateTime finalDiagnosisDate;

    @CatalogCode("card161-delivery-method")
    @Column(name = "delivery_method_code", length = 64)
    private String deliveryMethodCode;

    @CatalogCode("card161-home-stay-exclusion-reason")
    @Column(name = "home_stay_exclusion_reason_code", length = 64)
    private String homeStayExclusionReasonCode;

    @CatalogCode("card161-late-admission-reason")
    @Column(name = "late_admission_reason_code", length = 64)
    private String lateAdmissionReasonCode;

    @CatalogCode("card161-diagnosis-verified")
    @Column(name = "diagnosis_verified_code", length = 64)
    private String diagnosisVerifiedCode;

    @OneToMany(mappedBy = "card161", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id DESC")
    private List<Vaccination> vaccinations = new ArrayList<>();

    @Column(name = "estimated_infection_date_from")
    private LocalDate estimatedInfectionDateFrom;

    @Column(name = "estimated_infection_date_to")
    private LocalDate estimatedInfectionDateTo;

    @OneToMany(mappedBy = "card161", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id DESC")
    private List<Card161RiskFactor> riskFactors = new ArrayList<>();

    @OneToMany(mappedBy = "card161", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id DESC")
    private List<InfectionSource> possibleInfectionSources = new ArrayList<>();

    @OneToMany(mappedBy = "card161", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id DESC")
    private List<EnvironmentalSource> environmentalSources = new ArrayList<>();

    @CatalogCode("card161-living-condition")
    @Column(name = "living_condition_code", length = 64)
    private String livingConditionCode;

    @Column(name = "number_of_people")
    private Integer numberOfPeople;

    @Column(name = "number_of_rooms")
    private Integer numberOfRooms;

    @Column(name = "area", length = 64)
    private String area;

    @CatalogCode("card161-water-supply")
    @Column(name = "water_supply_code", length = 64)
    private String waterSupplyCode;

    @CatalogCode("card161-liquid-waste-disposal-type")
    @Column(name = "liquid_waste_disposal_type_code", length = 64)
    private String liquidWasteDisposalTypeCode;

    @CatalogCode("card161-solid-waste-disposal-type")
    @Column(name = "solid_waste_disposal_type_code", length = 64)
    private String solidWasteDisposalTypeCode;

    @CatalogCode("card161-room-condition")
    @Column(name = "room_condition_code", length = 64)
    private String roomConditionCode;

    @CatalogCode("card161-yard-condition")
    @Column(name = "yard_condition_code", length = 64)
    private String yardConditionCode;

    @CatalogCode("card161-area-condition")
    @Column(name = "area_condition_code", length = 64)
    private String areaConditionCode;

    /** Legacy fix: was a primitive {@code boolean}. */
    @Column(name = "has_lice")
    private Boolean hasLice;

    /** Legacy fix: was a primitive {@code boolean}. */
    @Column(name = "has_other_insects")
    private Boolean hasOtherInsects;

    /** Legacy fix: was a primitive {@code boolean}. */
    @Column(name = "has_rodents")
    private Boolean hasRodents;

    @CatalogCode("card161-important-causes-of-disease")
    @Column(name = "important_causes_of_disease_code", length = 64)
    private String importantCausesOfDiseaseCode;

    @CatalogCode("card161-visited-objects")
    @Column(name = "visited_objects_code", length = 64)
    private String visitedObjectsCode;

    @Column(name = "densely_populated", length = 64)
    private String denselyPopulated;

    @Column(name = "isolation_status", length = 255)
    private String isolationStatus;

    @Column(name = "water_supply_status", length = 255)
    private String waterSupplyStatus;

    @Column(name = "sanitary_maintenance", length = 255)
    private String sanitaryMaintenance;

    @Column(name = "sewerage_status", length = 255)
    private String sewerageStatus;

    @Column(name = "food_storage", length = 255)
    private String foodStorage;

    @Column(name = "food_preparation", length = 255)
    private String foodPreparation;

    @Column(name = "disease_causing_factors", length = 1000)
    private String diseaseCausingFactors;

    @OneToMany(mappedBy = "card161", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id DESC")
    private List<EnvironmentalLabTest> environmentalLabTests = new ArrayList<>();

    @OneToMany(mappedBy = "card161", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id DESC")
    private List<ContactPerson> contactPersonDetails = new ArrayList<>();

    @OneToMany(mappedBy = "card161", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id DESC")
    private List<ScreenedGroup> screenedGroups = new ArrayList<>();

    @OneToMany(mappedBy = "card161", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id DESC")
    private List<HomePreventiveMeasure> homePreventiveMeasures = new ArrayList<>();

    @OneToMany(mappedBy = "card161", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id DESC")
    private List<OutbreakDisinfectionMeasure> outbreakDisinfectionMeasures = new ArrayList<>();

    @Column(name = "hospital_name", length = 255)
    private String hospitalName;

    @CatalogCode("card161-infection-location")
    @Column(name = "infection_location_code", length = 64)
    private String infectionLocationCode;

    @CatalogCode("card161-probable-infection-location")
    @Column(name = "probable_infection_location_code", length = 64)
    private String probableInfectionLocationCode;

    @Column(name = "is_infection_source_missing")
    private Boolean isInfectionSourceMissing;

    /**
     * Legacy fix: {@code InfectionSourceDetail.card161_id} had no unique
     * constraint. Ownership stays on the detail side but is now enforced as
     * truly 1:1 there — see {@link InfectionSourceDetail}.
     */
    @OneToOne(mappedBy = "card161", cascade = CascadeType.ALL, orphanRemoval = true)
    private InfectionSourceDetail infectionSourceDetail;

    @CatalogCode("card161-main-probable-infection-factor")
    @Column(name = "main_probable_infection_factor_code", length = 64)
    private String mainProbableInfectionFactorCode;

    @ElementCollection
    @CollectionTable(name = "card161_indirection_causing", joinColumns = @JoinColumn(name = "card161_id"))
    @Column(name = "catalog_code")
    private List<String> infectionCausingConditionCode = new ArrayList<>();

    @CatalogCode("card161-outbreak-infection")
    @Column(name = "outbreak_infection_code", length = 64)
    private String outbreakInfectionCode;

    @CatalogCode("card161-case-status")
    @Column(name = "case_status_code", length = 64)
    private String caseStatusCode;

    @Column(name = "epidemiologist", length = 255)
    private String epidemiologist;

    @Column(name = "epidemiologist_assistant", length = 255)
    private String epidemiologistAssistant;

    /**
     * The following fields belong to the "Ilova varag'i №178" — the
     * zoonotic-disease attachment sheet filled in alongside this card's main
     * form when the underlying case is a zoonotic disease (e.g. a
     * rabies/animal-bite exposure).
     */
    @Column(name = "emergency_prophylaxis_given")
    private Boolean emergencyProphylaxisGiven;

    @OneToMany(mappedBy = "card161", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id DESC")
    private List<EmergencyProphylaxis> emergencyProphylaxisTreatments = new ArrayList<>();

    @Column(name = "clinical_form", length = 255)
    private String clinicalForm;

    @ElementCollection
    @CollectionTable(name = "card161_injury_location", joinColumns = @JoinColumn(name = "card161_id"))
    @Column(name = "catalog_code")
    private List<String> injuryLocationCodes = new ArrayList<>();

    @CatalogCode("card161-disease-severity")
    @Column(name = "disease_severity_code", length = 64)
    private String diseaseSeverityCode;

    @Column(name = "is_occupational_disease")
    private Boolean isOccupationalDisease;

    @Column(name = "disease_source_info", length = 1000)
    private String diseaseSourceInfo;

    @CatalogCode("card161-animal-ownership")
    @Column(name = "animal_ownership_code", length = 64)
    private String animalOwnershipCode;

    @CatalogCode("card161-animal-observation-result")
    @Column(name = "animal_observation_result_code", length = 64)
    private String animalObservationResultCode;

    @CatalogCode("card161-animal-lab-test-result")
    @Column(name = "animal_lab_test_result_code", length = 64)
    private String animalLabTestResultCode;
}
