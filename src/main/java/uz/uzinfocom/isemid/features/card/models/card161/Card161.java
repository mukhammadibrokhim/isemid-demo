package uz.uzinfocom.isemid.features.card.models.card161;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import uz.uzinfocom.isemid.commons.models.Vaccination;
import uz.uzinfocom.isemid.features.card.models.Card;
import uz.uzinfocom.isemid.features.organization.models.Organization;

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
    private String regionCode;
    private String districtCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "polyclinic_id", insertable = false, updatable = false)
    private Organization polyclinic;

    @Column(name = "polyclinic_id")
    private Long polyclinicId;

    private String initialSymptoms;
    private String detectedCode;
    private LocalDateTime epidemiologicalExamDate;
    private LocalDateTime observationEndDate;
    private LocalDateTime finalDiagnosisDate;
    private String deliveryMethodCode;
    private String homeStayExclusionReasonCode;
    private String lateAdmissionReasonCode;
    private String diagnosisVerifiedCode;

    @Builder.Default
    @ManyToMany
    @JoinTable(
            name = "card161_vaccination",
            joinColumns = @JoinColumn(name = "card161_id"),
            inverseJoinColumns = @JoinColumn(name = "vaccination_id")
    )
    private List<Vaccination> vaccinations = new ArrayList<>();

    private LocalDate estimatedInfectionDateFrom;
    private LocalDate estimatedInfectionDateTo;

    @OneToMany(mappedBy = "card161", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"card161"})
    @OrderBy("id DESC")
    @Builder.Default
    private List<RiskFactor> riskFactors = new ArrayList<>();

    @OneToMany(mappedBy = "card161", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"card161"})
    @OrderBy("id DESC")
    @Builder.Default
    private List<InfectionSource> possibleInfectionSources = new ArrayList<>();

    @OneToMany(mappedBy = "card161", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"card161"})
    @OrderBy("id DESC")
    @Builder.Default
    private List<EnvironmentalSource> environmentalSources = new ArrayList<>();

    //catalog qoshish kerak
    private String livingConditionCode;
    private int numberOfPeople;
    private int numberOfRooms;
    private String area;

    //catalog qoshish kerak
    private String waterSupplyCode;

    private String liquidWasteDisposalTypeCode;
    private String solidWasteDisposalTypeCode;
    private String roomConditionCode;
    private String yardConditionCode;
    private String areaConditionCode;
    private Boolean hasLice; //bitlash
    private boolean hasOtherInsects;
    private boolean hasRodents;
    private String importantCausesOfDiseaseCode;
    private String visitedObjectsCode;
    private String denselyPopulated;
    private String isolationStatus;
    private String waterSupplyStatus;
    private String sanitaryMaintenance;
    private String sewerageStatus;
    private String foodStorage;
    private String foodPreparation;
    private String diseaseCausingFactors;

    @OneToMany(mappedBy = "card161", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"card161"})
    @OrderBy("id DESC")
    @Builder.Default
    private List<EnvironmentalLabTest> environmentalLabTests = new ArrayList<>();

    @OneToMany(mappedBy = "card161", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"card161"})
    @OrderBy("id DESC")
    @Builder.Default
    private List<ContactPerson> contactPersonDetails = new ArrayList<>();

    @OneToMany(mappedBy = "card161", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"card161"})
    @OrderBy("id DESC")
    @Builder.Default
    private List<ScreenedGroup> screenedGroups = new ArrayList<>();

    @OneToMany(mappedBy = "card161", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"card161"})
    @OrderBy("id DESC")
    @Builder.Default
    private List<HomePreventiveMeasure> homePreventiveMeasures = new ArrayList<>();

    @OneToMany(mappedBy = "card161", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"card161"})
    @OrderBy("id DESC")
    @Builder.Default
    private List<OutbreakDisinfectionMeasure> outbreakDisinfectionMeasures = new ArrayList<>();

    private String hospitalName;
    private String infectionLocationCode;
    private String probableInfectionLocationCode;
    private Boolean isInfectionSourceMissing;

    @OneToOne(mappedBy = "card161", cascade = CascadeType.ALL, orphanRemoval = true)
    private InfectionSourceDetail infectionSourceDetail;
    private String mainProbableInfectionFactorCode;

    @ElementCollection
    @CollectionTable(
            name = "card161_indirection_causing",
            joinColumns = @JoinColumn(name = "card161_id")
    )
    @Column(name = "catalog_code")
    private List<String> infectionCausingConditionCode = new ArrayList<>();

    private String outbreakInfectionCode;
    private String caseStatusCode;
    private String epidemiologist;
    private String epidemiologistAssistant;
    @Column(columnDefinition = "TEXT")
    private String comment;
}

