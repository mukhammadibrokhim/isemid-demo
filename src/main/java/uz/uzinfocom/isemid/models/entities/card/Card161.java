package uz.uzinfocom.isemid.models.entities.card;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Card161 extends Card {
    @Column(length = 500)
    private String firstDaySymptoms;
    private String diagnosed;
    private LocalDateTime surveyDateTime;
    private LocalDateTime surveillanceEndDate;
    private LocalDateTime finalDiagnosisDate;
    //value-set
    private String transportationMethod;
    private String noHomeStayReason;
    private String delayedTransferReasons;
    private Boolean isLaboratoryTestConducted;

    private LocalDate estimatedInfectionDate;

    private String infectionRiskFactors;
    private String seasonTime;
    private String fullName;
    private String diagnosisAndClinicalForm;
    private String cohabitationTimeAndPlace;
    private String contactType;
    private String donorResidence;
    private String testResult;
    private String foodAndWaterSourcesType;
    private String sampleCollectionTimeAndPlace;
    private String usageTimeAndPlace;
    private String storageConditions;
    private String qualityAssessmentByPatientAndOthers;
    private String livingConditions;
    private Integer numberOfPeople;
    private Integer numberOfRooms;
    private String area;
    private String waterSupply;
    private String liquidWasteManagement;
    private String solidWasteManagement;
    private String rooms;
    private String yard;
    private String territory;
    private String inspectionDate;
    private String objectName;
    private String infectiousAgentsType;
    private String animalType;
    private String material;
    private Integer sampleQuantity;
    private String inspectionTypeAndResult;
    private String inspectorFullName;
    private Integer inspectorAge;
    private String inspectorResidence;
    private String inspectorOccupationAndWorkplace;
    private String vaccinationInformation;
    private String preventiveMeasuresDescription;
    private String jointMeasurements;
    private String equipmentName;
    private String equipmentLocation;
    private Integer contactedIndividualsCount;
    private String specialProphylacticMeasuresRequired;
    private String treatedWithSpecialProphylacticMeasures;
    private String laboratoryTestsConducted;
    private String notificationsSentTo;
    private String nextDiseasePrevention;
    private String laboratoryInspection;
    private String observationResult;
    private String measuresTaken;
    private String medicationType;
    private String timeOfAdministration;
    private String placeOfAdministration;
    private String executors;
    private String hospitalName;
    private String diseaseOriginLocation;
    private String possibleDiseaseOriginLocation;
    private String diseaseSourceNotFound;
    private String personFullName;
    private String diseaseOriginPeriod;
    private String animalSpecies;
    private String possibleInfectionSource;
    private String factorsCausingInfection;
    private String infectionInAquaticEnvironment;
    private String situationDescription;
    private String epidemiologist;
    private String assistantEpidemiologist;
}
