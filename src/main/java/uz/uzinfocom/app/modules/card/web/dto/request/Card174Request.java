package uz.uzinfocom.app.modules.card.web.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.modules.card.domain.enums.CardType;
import uz.uzinfocom.app.modules.card.web.dto.request.card174.InfectionMonitoringRequest;
import uz.uzinfocom.app.modules.card.web.dto.request.card174.OutbreakControlMeasureRequest;

import java.time.LocalDate;
import java.util.List;

public record Card174Request(
        Integer serialDocNumber,
        @Size(max = 64) String mkb10Code,
        @Size(max = 500) String mkb10Name,
        @Size(max = 255) String pathogenType,
        LocalDate dataObtainedDate,
        LocalDate reportToVeterinaryDepartmentDate,
        @Size(max = 500) String animalPrimaryDiagnosis,
        @Size(max = 500) String humanPrimaryDiagnosis,
        LocalDate investigationDate,
        LocalDate lastDiseaseYear,
        LocalDate currentAnimalInfectionDate,
        @Size(max = 500) String outbreakLocalization,
        @Size(max = 255) String animalOwner,
        @Size(max = 500) String ownerAddress,
        @Size(max = 64) String affectedAnimalTypeCode,
        Integer affectedAnimalCount,
        @Size(max = 64) String animalOwnershipCode,
        Boolean isAreaExotic,
        Boolean rodentIncrease,
        Boolean vectorIncrease,
        Boolean wildRodentsIncrease,
        Boolean synanthropicRodentsIncrease,
        Boolean bloodSuckingArthropodsIncrease,
        Boolean epizootologyExistence,
        List<String> diseaseFactorCodes,
        @Size(max = 255) String animalType,
        LocalDate testDate,
        Integer testSampleCount,
        @Size(max = 255) String testingMethod,
        @Size(max = 500) String testResult,
        List<String> affectedAnimalCodes,
        Integer affectedHumans,
        Integer includingIndustrialConditions,
        Integer includingWhoApplied,
        Integer includingIdentified,
        Integer treatedHumans,
        Integer affectedInOutbreak,
        @Valid List<InfectionMonitoringRequest> infectionMonitoring,
        @Size(max = 64) String quarantineTypeCode,
        LocalDate quarantineStartDate,
        LocalDate quarantineEndDate,
        @Size(max = 64) String animalDisposalMethodCode,
        LocalDate animalDisposalDate,
        @Size(max = 1000) String precautionaryMeasures,
        @Size(max = 500) String strayAnimalCapture,
        @Size(max = 500) String wildAnimalCulling,
        @Size(max = 64) String deratizationCode,
        Double deratizationArea,
        @Size(max = 500) String inspectors,
        @Size(max = 500) String isolation,
        @Size(max = 500) String meatSubmission,
        @Size(max = 500) String treatment,
        Boolean measureTaken,
        List<String> disinfectionTransmissionFactorCodes,
        Integer disinfectedFactorAmount,
        LocalDate disinfectionDate,
        List<String> eliminationMethodCodes,
        @Size(max = 500) String locationOfEvent,
        @Size(max = 1000) String executionControlResults,
        @Valid List<OutbreakControlMeasureRequest> outbreakControlMeasures,
        @Size(max = 1000) String additionalMeasuresInfo
) implements CardRequest {

    public Card174Request {
        diseaseFactorCodes = immutableCopy(diseaseFactorCodes);
        affectedAnimalCodes = immutableCopy(affectedAnimalCodes);
        infectionMonitoring = immutableCopy(infectionMonitoring);
        disinfectionTransmissionFactorCodes = immutableCopy(disinfectionTransmissionFactorCodes);
        eliminationMethodCodes = immutableCopy(eliminationMethodCodes);
        outbreakControlMeasures = immutableCopy(outbreakControlMeasures);
    }

    @Override
    public CardType type() {
        return CardType.CARD174;
    }

    private static <T> List<T> immutableCopy(List<T> values) {
        return values == null ? List.of() : List.copyOf(values);
    }
}
