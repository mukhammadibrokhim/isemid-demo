package uz.uzinfocom.isemid.models.entities.card;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import uz.uzinfocom.isemid.models.entities.value_set.Catalog;

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
public class Card161 extends Card {
    private String callerType;
    private Boolean isResident;
    private String residentialTreatmentFacility;
    private LocalDate diseaseDetectedDate;
    private String region;
    private String district;
    private String polyclinic;
    private String initialSymptoms;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "detected", referencedColumnName = "code")
    @JsonIgnoreProperties({"type", "id", "createdAt", "updatedAt", "createdBy", "updatedBy"})
    private Catalog detected;

    private LocalDateTime epidemiologicalExamDate;
    private LocalDateTime observationEndDate;
    private LocalDateTime finalDiagnosisDate;
    //catalogga qoshish kerak

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "delivery_method", referencedColumnName = "code")
    @JsonIgnoreProperties({"type", "id", "createdAt", "updatedAt", "createdBy", "updatedBy"})
    private Catalog deliveryMethod;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(referencedColumnName = "code")
    @JsonIgnoreProperties({"type", "id", "createdAt", "updatedAt", "createdBy", "updatedBy"})
    private Catalog homeStayExclusionReason;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(referencedColumnName = "code")
    @JsonIgnoreProperties({"type", "id", "createdAt", "updatedAt", "createdBy", "updatedBy"})
    private Catalog lateAdmissionReason;

    private String diagnosisVerified;
    //LIS bilan integratsiya uchun polya qoshish kerak.
    // emlash uz bialn integratsiya uchun polyalar.
    // DMED bilan integratsiya uchun polyalar.

    private LocalDate estimatedInfectionDateFrom;
    private LocalDate estimatedInfectionDateTo;

//    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
//    @JoinTable(
//            name = "card161_risk_factors",
//            joinColumns = @JoinColumn(name = "card161_id"),
//            inverseJoinColumns = @JoinColumn(name = "catalog_code"))
//    @JsonIgnoreProperties({"type", "id", "createdAt", "updatedAt", "createdBy", "updatedBy"})
//    private List<Catalog> riskFactors = new ArrayList<>();

    @OneToMany(mappedBy = "card161", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"card161"})
    private List<Card161RiskFactor> riskFactors = new ArrayList<>();

    private String fullName;
    private String diagnosisClinicalFormOrDonorStatus;
    private String contactInfoAndDonorResidence;
    private String testResult;
    private String foodAndWaterSourceTypes;
    private String collectionLocationAndTime;
    private String usageLocationAndTime;
    private String storageConditions;
    private String qualityFeedbackFromPatientAndOthers;

    //catalog qoshish kerak
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(referencedColumnName = "code")
    @JsonIgnoreProperties({"type", "id", "createdAt", "updatedAt", "createdBy", "updatedBy"})
    private Catalog livingCondition;
    private int numberOfPeople;
    private int numberOfRooms;
    private String area;

    //catalog qoshish kerak
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(referencedColumnName = "code")
    @JsonIgnoreProperties({"type", "id", "createdAt", "updatedAt", "createdBy", "updatedBy"})
    private Catalog waterSupply;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(referencedColumnName = "code")
    @JsonIgnoreProperties({"type", "id", "createdAt", "updatedAt", "createdBy", "updatedBy"})
    private Catalog liquidWasteDisposalType;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(referencedColumnName = "code")
    @JsonIgnoreProperties({"type", "id", "createdAt", "updatedAt", "createdBy", "updatedBy"})
    private Catalog solidWasteDisposalType;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(referencedColumnName = "code")
    @JsonIgnoreProperties({"type", "id", "createdAt", "updatedAt", "createdBy", "updatedBy"})
    private Catalog roomCondition;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(referencedColumnName = "code")
    @JsonIgnoreProperties({"type", "id", "createdAt", "updatedAt", "createdBy", "updatedBy"})
    private Catalog yardCondition;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(referencedColumnName = "code")
    @JsonIgnoreProperties({"type", "id", "createdAt", "updatedAt", "createdBy", "updatedBy"})
    private Catalog areaCondition;

    private Boolean hasLice; //bitlash
    private boolean hasOtherInsects;
    private boolean hasRodents;
    private String importantCausesOfDisease;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(referencedColumnName = "code")
    @JsonIgnoreProperties({"type", "id", "createdAt", "updatedAt", "createdBy", "updatedBy"})
    private Catalog visitedObjects;

    private boolean denselyPopulated;
    private String isolationStatus;
    private String waterSupplyStatus;
    private String sanitaryMaintenance;
    private String foodStorage;
    private String foodPreparation;
    private String diseaseCausingFactors;
    private LocalDate examinationDate;
    private String objectArthropodsAnimals;
    private String material;
    private String sampleQuantity;
    private String testTypeAndResult;
    private String contactFullName;
    private String contactAge;
    private String contactAddress;
    private String contactJobTypeAndLocation;
    private String contactImmunizationStatus;
    private String contactRestrictionMeasures;
    private String teamName;
    private String prophylacticAddress;
    private String contactCount;
    private String requiredProphylacticSubstance;
    private String treatedWithProphylacticSubstance;
    private String laboratoryTestConducted;

    private String notifiedPerson;
    private String nextDiseasePrevention;
    private String laboratoryExamination;
    private String observationResult;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(referencedColumnName = "code")
    @JsonIgnoreProperties({"type", "id", "createdAt", "updatedAt", "createdBy", "updatedBy"})
    private Catalog preventiveMeasures; //catalog qoshialdi
    private String drugType;

    private String conductedAt;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(referencedColumnName = "code")
    @JsonIgnoreProperties({"type", "id", "createdAt", "updatedAt", "createdBy", "updatedBy"})
    private Catalog conductedLocation;
    private String executors;
    private String executionMonitoringResult;
    private String hospitalName;
    //catalog
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(referencedColumnName = "code")
    @JsonIgnoreProperties({"type", "id", "createdAt", "updatedAt", "createdBy", "updatedBy"})
    private Catalog infectionLocation;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(referencedColumnName = "code")
    @JsonIgnoreProperties({"type", "id", "createdAt", "updatedAt", "createdBy", "updatedBy"})
    private Catalog probableInfectionLocation;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(referencedColumnName = "code")
    @JsonIgnoreProperties({"type", "id", "createdAt", "updatedAt", "createdBy", "updatedBy"})
    private Catalog infectionSourceNotFound;
    private String personFullName;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(referencedColumnName = "code")
    @JsonIgnoreProperties({"type", "id", "createdAt", "updatedAt", "createdBy", "updatedBy"})
    private Catalog infectionSourceDiseasePeriod;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(referencedColumnName = "code")
    @JsonIgnoreProperties({"type", "id", "createdAt", "updatedAt", "createdBy", "updatedBy"})
    private Catalog animalType;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(referencedColumnName = "code")
    @JsonIgnoreProperties({"type", "id", "createdAt", "updatedAt", "createdBy", "updatedBy"})
    private Catalog mainProbableInfectionFactor;


    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(
            name = "card161_indirection_cousing",
            joinColumns = @JoinColumn(name = "card161_id"),
            inverseJoinColumns = @JoinColumn(name = "catalog_code"))
    private List<Catalog> infectionCausingConditions = new ArrayList<>();

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(referencedColumnName = "code")
    @JsonIgnoreProperties({"type", "id", "createdAt", "updatedAt", "createdBy", "updatedBy"})
    private Catalog outbreakInfection;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(referencedColumnName = "code")
    @JsonIgnoreProperties({"type", "id", "createdAt", "updatedAt", "createdBy", "updatedBy"})
    private Catalog caseStatus;
    private String epidemiologist;
    private String epidemiologistAssistant;
}
