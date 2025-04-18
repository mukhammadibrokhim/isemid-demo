package uz.uzinfocom.isemid.features.card.dtos.responses.card161;

import lombok.*;
import lombok.experimental.SuperBuilder;
import uz.uzinfocom.isemid.features.card.dtos.responses.CardResponse;
import uz.uzinfocom.isemid.features.classifier.dtos.responses.CatalogRespDto;

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
    private CatalogRespDto region;
    private CatalogRespDto district;
    private Long polyclinicId;
    private String initialSymptoms;
    //detected catalogga qoshish kerak
    private CatalogRespDto detected;
    private LocalDateTime epidemiologicalExamDate;
    private LocalDateTime observationEndDate;
    private LocalDateTime finalDiagnosisDate;
    //catalogga qoshish kerak
    private CatalogRespDto deliveryMethod;
    private CatalogRespDto homeStayExclusionReason;
    private CatalogRespDto lateAdmissionReason;

    private CatalogRespDto diagnosisVerified;
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
    private CatalogRespDto livingCondition;
    private Integer numberOfPeople;
    private Integer numberOfRooms;
    private String area;

    //catalog qoshish kerak
    private CatalogRespDto waterSupply;
    private CatalogRespDto liquidWasteDisposalType;
    private CatalogRespDto solidWasteDisposalType;
    private CatalogRespDto roomCondition;
    private CatalogRespDto yardCondition;
    private CatalogRespDto areaCondition;

    private Boolean hasLice; //bitlash
    private Boolean hasOtherInsects;
    private Boolean hasRodents;
    private CatalogRespDto importantCausesOfDisease;

    private CatalogRespDto visitedObjects;

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
    private CatalogRespDto infectionLocation;
    private CatalogRespDto probableInfectionLocation;
    private Boolean isInfectionSourceMissing;
    private InfectionSourceDetailResponse infectionSourceDetail;
    private CatalogRespDto mainProbableInfectionFactor;

    @Builder.Default
    private List<CatalogRespDto> infectionCausingConditions = new ArrayList<>();
    private CatalogRespDto outbreakInfection;
    private CatalogRespDto caseStatus;
    private String epidemiologist;
    private String epidemiologistAssistant;
}
