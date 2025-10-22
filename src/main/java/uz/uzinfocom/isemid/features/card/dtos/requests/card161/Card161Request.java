package uz.uzinfocom.isemid.features.card.dtos.requests.card161;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import uz.uzinfocom.isemid.features.card.dtos.requests.CardRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Request object for CARD161 type card")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Card161Request extends CardRequest {
    private String callerType;
    private Boolean isResident;
    private String residentialTreatmentFacility;
    private LocalDateTime diseaseDetectedDate;
    private String regionCode;
    private String districtCode;
    private Long polyclinicId;
    private String initialSymptoms;
    //detected catalogga qoshish kerak
    private String detectedCode;
    private LocalDateTime epidemiologicalExamDate;
    private LocalDateTime observationEndDate;
    private LocalDateTime finalDiagnosisDate;
    //catalogga qoshish kerak
    private String deliveryMethodCode;
    private String homeStayExclusionReasonCode;
    private String lateAdmissionReasonsCode;

    private String diagnosisVerifiedCode;
    //LIS bilan integratsiya uchun polya qoshish kerak.
    // emlash uz bialn integratsiya uchun polyalar.
    // DMED bilan integratsiya uchun polyalar.

    private List<VaccinationRequest> vaccinations;

    private LocalDate estimatedInfectionDateFrom;
    private LocalDate estimatedInfectionDateTo;


    private List<RiskFactorRequest> riskFactors;

    private List<InfectionSourceRequest> possibleInfectionSources;

    private List<EnvironmentalSourceRequest> environmentalSources;
    private List<EnvironmentalLabTestRequest> environmentalLabTests;

    //catalog qoshish kerak
    private String livingConditionCode;
    private Integer numberOfPeople;
    private Integer numberOfRooms;
    private String area;

    //catalog qoshish kerak
    private String waterSupplyCode;
    private String liquidWasteDisposalTypeCode;
    private String solidWasteDisposalTypeCode;
    private String roomConditionCode;
    private String yardConditionCode;
    private String areaConditionCode;

    private Boolean hasLice; //bitlash
    private Boolean hasOtherInsects;
    private Boolean hasRodents;
    private String importantCausesOfDiseaseCode;

    private String visitedObjectsCode;

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

    private List<ContactPersonRequest> contactPersonDetails;
    private List<ScreenedGroupRequest> screenedGroups;

    private List<HomePreventiveMeasureRequest> homePreventiveMeasures;
    private List<OutbreakDisinfectionMeasureRequest> outbreakDisinfectionMeasures;

    private String hospitalName;
    //catalog
    private String infectionLocationCode;
    private String probableInfectionLocationCode;
    private Boolean isInfectionSourceMissing;
    private InfectionSourceDetailRequest infectionSourceDetail;
    private List<String> mainProbableInfectionFactors;
    private List<String> infectionCausingConditionsCode;
    private String outbreakInfectionCode;
    private String infectionPlaceCode;
    private String activityPlaceCode;
    private String epidemiologist;
    private String epidemiologistAssistant;
    private String comment;
}
