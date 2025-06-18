package uz.uzinfocom.isemid.features.card.dtos.requests.card174;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import uz.uzinfocom.isemid.features.card.dtos.requests.CardRequest;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Request object for CARD174 type card")
public class Card174Request extends CardRequest {

    private Integer serialDocNumber;
    private String mkb10Code;
    private String mkb10Name;
    private String pathogenType;
    private LocalDate dataObtainedDate;
    private LocalDate reportToVeterinaryDepartmentDate;
    private String animalPrimaryDiagnosis;
    private String humanPrimaryDiagnosis;
    private LocalDate investigationDate;
    private LocalDate lastDiseaseYear;
    private LocalDate currentAnimalInfectionDate;
    private String outbreakLocalization;
    private String animalOwner;
    private String ownerAddress;
    private boolean rodentIncrease;
    private boolean vectorIncrease;
    private boolean wildRodentsIncrease;
    private boolean synanthropicRodentsIncrease;
    private boolean bloodSuckingArthropodsIncrease;
    private boolean epizootologyExistence;
    private String animalType;
    private LocalDate testDate;
    private Integer testSampleCount;
    private String testingMethod;
    private String testResult;
    private Integer affectedHumans;
    private Integer includingIndustrialConditions;
    private Integer includingWhoApplied;
    private Integer includingIdentified;
    private Integer treatedHumans;
    private Integer affectedInOutbreak;
    private LocalDate quarantineStartDate;
    private LocalDate quarantineEndDate;
    private LocalDate animalDisposalDate;
    private String precautionaryMeasures;
    private String strayAnimalCapture;
    private String wildAnimalCulling;
    private Double deratizationArea;
    private String inspectors;
    private String isolation;
    private String meatSubmission;
    private String treatment;
    private Boolean measureTaken;
    private Integer disinfectedFactorAmount;
    private LocalDate disinfectionDate;
    private String locationOfEvent;
    private String executionControlResults;
    private String additionalMeasuresInfo;
    private Integer affectedAnimalCount;


    private String affectedAnimalTypeCode;
    private String animalOwnershipCode;
    private Boolean isAreaExotic;
    private String animalDisposalMethodCode;
    private String deratizationCode;
    private String quarantineTypeCode;


    private List<InfectionMonitoringRequest> infectionMonitoring;
    private List<OutbreakControlMeasureRequest> outbreakControlMeasures;
    private List<String> disinfectionTransmissionFactorsCode;
    private List<String> eliminationMethodCodes;
    private List<String> diseaseFactorCodes;

}