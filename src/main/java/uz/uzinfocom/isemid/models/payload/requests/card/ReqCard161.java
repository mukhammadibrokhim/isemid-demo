package uz.uzinfocom.isemid.models.payload.requests.card;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

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
public class ReqCard161 extends ReqCard {
    private String callerType;
    private Boolean isResident;
    private String residentialTreatmentFacility;
    private LocalDate diseaseDetectedDate;
    private String region;
    private String district;
    private String polyclinic;
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

    private String diagnosisVerified;
    //LIS bilan integratsiya uchun polya qoshish kerak.
    // emlash uz bialn integratsiya uchun polyalar.
    // DMED bilan integratsiya uchun polyalar.

    private LocalDate estimatedInfectionDateFrom;
    private LocalDate estimatedInfectionDateTo;


    //    private List<String> riskFactorCodes;
    private List<ReqCard161RiskFactor> riskFactors;
    //    private String addressLocation;
//    private String seasonTime;
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
    private Boolean hasOtherInsects;
    private Boolean hasRodents;
    private String importantCausesOfDisease;

    private String visitedObjectsCode;

    private Boolean denselyPopulated;
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

    private String preventiveMeasuresCode; //catalog qoshialdi
    private String drugType;

    private String conductedAt;
    private String conductedLocationCode;
    private String executors;
    private String executionMonitoringResult;
    private String hospitalName;
    //catalog
    private String infectionLocationCode;
    private String probableInfectionLocationCode;
    private String infectionSourceNotFoundCode;
    private String personFullName;
    private String infectionSourceDiseasePeriodCode;
    private String animalTypeCode;
    private String mainProbableInfectionFactorCode;
    private List<String> infectionCausingConditionsCode;
    private String outbreakInfectionCode;
    private String caseStatusCode;
    private String epidemiologist;
    private String epidemiologistAssistant;
}
