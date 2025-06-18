package uz.uzinfocom.isemid.features.card.dtos.responses.card161;

import lombok.*;
import lombok.experimental.SuperBuilder;
import uz.uzinfocom.isemid.features.card.dtos.responses.CardResponse;
import uz.uzinfocom.isemid.features.classifier.dtos.responses.CatalogResponseDto;

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
    private CatalogResponseDto region;
    private CatalogResponseDto district;
    private Long polyclinicId;
    private String initialSymptoms;
    //detected catalogga qoshish kerak
    private CatalogResponseDto detected;
    private LocalDateTime epidemiologicalExamDate;
    private LocalDateTime observationEndDate;
    private LocalDateTime finalDiagnosisDate;
    //catalogga qoshish kerak
    private CatalogResponseDto deliveryMethod;
    private CatalogResponseDto homeStayExclusionReason;
    private CatalogResponseDto lateAdmissionReason;

    private CatalogResponseDto diagnosisVerified;
    //LIS bilan integratsiya uchun polya qoshish kerak.
    // emlash uz bialn integratsiya uchun polyalar.
    // DMED bilan integratsiya uchun polyalar.

    private List<VaccinationResponse> vaccinations;

    private LocalDate estimatedInfectionDateFrom;
    private LocalDate estimatedInfectionDateTo;

    @Builder.Default
    private List<Card161RiskFactorResponse> riskFactors = new ArrayList<>();

    @Builder.Default
    private List<InfectionSourceResponse> possibleInfectionSources = new ArrayList<>();

    @Builder.Default
    private List<EnvironmentalSourceResponse> environmentalSources = new ArrayList<>();
    @Builder.Default
    private List<EnvironmentalLabTestResponse> environmentalLabTests = new ArrayList<>();

    //catalog qoshish kerak
    private CatalogResponseDto livingCondition;
    private Integer numberOfPeople;
    private Integer numberOfRooms;
    private String area;

    //catalog qoshish kerak
    private CatalogResponseDto waterSupply;
    private CatalogResponseDto liquidWasteDisposalType;
    private CatalogResponseDto solidWasteDisposalType;
    private CatalogResponseDto roomCondition;
    private CatalogResponseDto yardCondition;
    private CatalogResponseDto areaCondition;

    private Boolean hasLice; //bitlash
    private Boolean hasOtherInsects;
    private Boolean hasRodents;
    private CatalogResponseDto importantCausesOfDisease;

    private CatalogResponseDto visitedObjects;

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
    private CatalogResponseDto infectionLocation;
    private CatalogResponseDto probableInfectionLocation;
    private Boolean isInfectionSourceMissing;
    private InfectionSourceDetailResponse infectionSourceDetail;
    private CatalogResponseDto mainProbableInfectionFactor;

    @Builder.Default
    private List<CatalogResponseDto> infectionCausingConditions = new ArrayList<>();
    private CatalogResponseDto outbreakInfection;
    private CatalogResponseDto caseStatus;
    private String epidemiologist;
    private String epidemiologistAssistant;
}
