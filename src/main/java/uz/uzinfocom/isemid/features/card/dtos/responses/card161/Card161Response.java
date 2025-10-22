package uz.uzinfocom.isemid.features.card.dtos.responses.card161;

import lombok.*;
import lombok.experimental.SuperBuilder;
import uz.uzinfocom.isemid.features.card.dtos.responses.CardResponse;
import uz.uzinfocom.isemid.features.classifier.dtos.responses.CatalogMinResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class Card161Response extends CardResponse {
    private String callerType;
    private Boolean isResident;
    private String residentialTreatmentFacility;
    private LocalDateTime diseaseDetectedDate;
    private CatalogMinResponse region;
    private CatalogMinResponse district;
    private Long polyclinicId;
    private String initialSymptoms;
    //detected catalogga qoshish kerak
    private CatalogMinResponse detected;
    private LocalDateTime epidemiologicalExamDate;
    private LocalDateTime observationEndDate;
    private LocalDateTime finalDiagnosisDate;
    //catalogga qoshish kerak
    private CatalogMinResponse deliveryMethod;
    private CatalogMinResponse homeStayExclusionReason;
    private CatalogMinResponse lateAdmissionReason;

    private CatalogMinResponse diagnosisVerified;
    //LIS bilan integratsiya uchun polya qoshish kerak.
    // emlash uz bialn integratsiya uchun polyalar.
    // DMED bilan integratsiya uchun polyalar.

    private List<VaccinationResponse> vaccinations;

    private LocalDate estimatedInfectionDateFrom;
    private LocalDate estimatedInfectionDateTo;

    @Builder.Default
    private List<RiskFactorResponse> riskFactors = new ArrayList<>();

    @Builder.Default
    private List<InfectionSourceResponse> possibleInfectionSources = new ArrayList<>();

    @Builder.Default
    private List<EnvironmentalSourceResponse> environmentalSources = new ArrayList<>();
    @Builder.Default
    private List<EnvironmentalLabTestResponse> environmentalLabTests = new ArrayList<>();

    //catalog qoshish kerak
    private CatalogMinResponse livingCondition;
    private Integer numberOfPeople;
    private Integer numberOfRooms;
    private String area;

    //catalog qoshish kerak
    private CatalogMinResponse waterSupply;
    private CatalogMinResponse liquidWasteDisposalType;
    private CatalogMinResponse solidWasteDisposalType;
    private CatalogMinResponse roomCondition;
    private CatalogMinResponse yardCondition;
    private CatalogMinResponse areaCondition;

    private Boolean hasLice; //bitlash
    private Boolean hasOtherInsects;
    private Boolean hasRodents;
    private CatalogMinResponse importantCausesOfDisease;

    private CatalogMinResponse visitedObjects;

    private String denselyPopulated;
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

    @Builder.Default
    private List<ContactPersonResponse> contactPersonDetails = new ArrayList<>();

    @Builder.Default
    private List<ScreenedGroupResponse> screenedGroups = new ArrayList<>();

    @Builder.Default
    private List<HomePreventiveMeasureResponse> homePreventiveMeasures = new ArrayList<>();

    @Builder.Default
    private List<OutbreakDisinfectionMeasureResponse> outbreakDisinfectionMeasures = new ArrayList<>();

    private String hospitalName;
    //catalog
    private CatalogMinResponse infectionLocation;
    private CatalogMinResponse probableInfectionLocation;
    private Boolean isInfectionSourceMissing;
    private InfectionSourceDetailResponse infectionSourceDetail;
    @Builder.Default
    private List<CatalogMinResponse> mainProbableInfectionFactors = new ArrayList<>();

    @Builder.Default
    private List<CatalogMinResponse> infectionCausingConditions = new ArrayList<>();
    private CatalogMinResponse outbreakInfection;
    private CatalogMinResponse infectionPlace;
    private CatalogMinResponse activityPlace;
    private String epidemiologist;
    private String epidemiologistAssistant;
    private String comment;
}
