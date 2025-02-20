package uz.uzinfocom.isemid.models.entities.card.card161;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import uz.uzinfocom.isemid.models.entities.card.Card;
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
    private LocalDateTime diseaseDetectedDate;
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

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(referencedColumnName = "code")
    @JsonIgnoreProperties({"type", "id", "createdAt", "updatedAt", "createdBy", "updatedBy"})
    private Catalog diagnosisVerified;
    //LIS bilan integratsiya uchun polya qoshish kerak.
    // emlash uz bialn integratsiya uchun polyalar.
    // DMED bilan integratsiya uchun polyalar.

    @OneToMany(mappedBy = "card161", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"card161"})
    @Builder.Default
    List<Vaccination> vaccinations = new ArrayList<>();

    private LocalDate estimatedInfectionDateFrom;
    private LocalDate estimatedInfectionDateTo;

    @OneToMany(mappedBy = "card161", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"card161"})
    @Builder.Default
    private List<Card161RiskFactor> riskFactors = new ArrayList<>();

    @OneToMany(mappedBy = "card161", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"card161"})
    @Builder.Default
    private List<InfectionSource> possibleInfectionSources = new ArrayList<>();

    @OneToMany(mappedBy = "card161", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"card161"})
    @Builder.Default
    private List<EnvironmentalSource> environmentalSources = new ArrayList<>();


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

    private String denselyPopulated;
    private String isolationStatus;
    private String waterSupplyStatus;
    private String sanitaryMaintenance;
    private String foodStorage;
    private String foodPreparation;
    private String diseaseCausingFactors;


    @OneToMany(mappedBy = "card161", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"card161"})
    @Builder.Default
    private List<EnvironmentalLabTest> environmentalLabTests = new ArrayList<>();

    @OneToMany(mappedBy = "card161", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"card161"})
    @Builder.Default
    private List<ContactPerson> contactPersonListDetails = new ArrayList<>();

    @OneToMany(mappedBy = "card161", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"card161"})
    @Builder.Default
    private List<ScreenedGroup> screenedGroups = new ArrayList<>();

    @OneToMany(mappedBy = "card161", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"card161"})
    @Builder.Default
    private List<HomePreventiveMeasure> homePreventiveMeasures = new ArrayList<>();

    @OneToMany(mappedBy = "card161", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"card161"})
    @Builder.Default
    private List<OutbreakDisinfectionMeasure> outbreakDisinfectionMeasures = new ArrayList<>();


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
            name = "card161_indirection_coursing",
            joinColumns = @JoinColumn(name = "card161_id"),
            inverseJoinColumns = @JoinColumn(name = "catalog_code"))
    @Builder.Default
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

