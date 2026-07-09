package uz.uzinfocom.app.modules.card.web.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.modules.card.domain.enums.CardType;
import uz.uzinfocom.app.modules.card.web.dto.request.card161.Card161RiskFactorRequest;
import uz.uzinfocom.app.modules.card.web.dto.request.card161.ContactPersonRequest;
import uz.uzinfocom.app.modules.card.web.dto.request.card161.EnvironmentalLabTestRequest;
import uz.uzinfocom.app.modules.card.web.dto.request.card161.EnvironmentalSourceRequest;
import uz.uzinfocom.app.modules.card.web.dto.request.card161.HomePreventiveMeasureRequest;
import uz.uzinfocom.app.modules.card.web.dto.request.card161.InfectionSourceDetailRequest;
import uz.uzinfocom.app.modules.card.web.dto.request.card161.InfectionSourceRequest;
import uz.uzinfocom.app.modules.card.web.dto.request.card161.OutbreakDisinfectionMeasureRequest;
import uz.uzinfocom.app.modules.card.web.dto.request.card161.ScreenedGroupRequest;
import uz.uzinfocom.app.modules.card.web.dto.request.card161.VaccinationRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record Card161Request(
        @Size(max = 100) String callerType,
        Boolean isResident,
        @Size(max = 500) String residentialTreatmentFacility,
        LocalDateTime diseaseDetectedDate,
        @Size(max = 20) String regionCode,
        @Size(max = 20) String districtCode,
        Long polyclinicId,
        @Size(max = 1000) String initialSymptoms,
        @Size(max = 64) String detectedCode,
        LocalDateTime epidemiologicalExamDate,
        LocalDateTime observationEndDate,
        LocalDateTime finalDiagnosisDate,
        @Size(max = 64) String deliveryMethodCode,
        @Size(max = 64) String homeStayExclusionReasonCode,
        @Size(max = 64) String lateAdmissionReasonCode,
        @Size(max = 64) String diagnosisVerifiedCode,
        @Valid List<VaccinationRequest> vaccinations,
        LocalDate estimatedInfectionDateFrom,
        LocalDate estimatedInfectionDateTo,
        @Valid List<Card161RiskFactorRequest> riskFactors,
        @Valid List<InfectionSourceRequest> possibleInfectionSources,
        @Valid List<EnvironmentalSourceRequest> environmentalSources,
        @Size(max = 64) String livingConditionCode,
        Integer numberOfPeople,
        Integer numberOfRooms,
        @Size(max = 64) String area,
        @Size(max = 64) String waterSupplyCode,
        @Size(max = 64) String liquidWasteDisposalTypeCode,
        @Size(max = 64) String solidWasteDisposalTypeCode,
        @Size(max = 64) String roomConditionCode,
        @Size(max = 64) String yardConditionCode,
        @Size(max = 64) String areaConditionCode,
        Boolean hasLice,
        Boolean hasOtherInsects,
        Boolean hasRodents,
        @Size(max = 64) String importantCausesOfDiseaseCode,
        @Size(max = 64) String visitedObjectsCode,
        @Size(max = 64) String denselyPopulated,
        @Size(max = 255) String isolationStatus,
        @Size(max = 255) String waterSupplyStatus,
        @Size(max = 255) String sanitaryMaintenance,
        @Size(max = 255) String sewerageStatus,
        @Size(max = 255) String foodStorage,
        @Size(max = 255) String foodPreparation,
        @Size(max = 1000) String diseaseCausingFactors,
        @Valid List<EnvironmentalLabTestRequest> environmentalLabTests,
        @Valid List<ContactPersonRequest> contactPersonDetails,
        @Valid List<ScreenedGroupRequest> screenedGroups,
        @Valid List<HomePreventiveMeasureRequest> homePreventiveMeasures,
        @Valid List<OutbreakDisinfectionMeasureRequest> outbreakDisinfectionMeasures,
        @Size(max = 255) String hospitalName,
        @Size(max = 64) String infectionLocationCode,
        @Size(max = 64) String probableInfectionLocationCode,
        Boolean isInfectionSourceMissing,
        @Valid InfectionSourceDetailRequest infectionSourceDetail,
        @Size(max = 64) String mainProbableInfectionFactorCode,
        List<String> infectionCausingConditionCode,
        @Size(max = 64) String outbreakInfectionCode,
        @Size(max = 64) String caseStatusCode,
        @Size(max = 255) String epidemiologist,
        @Size(max = 255) String epidemiologistAssistant
) implements CardRequest {

    public Card161Request {
        vaccinations = immutableCopy(vaccinations);
        riskFactors = immutableCopy(riskFactors);
        possibleInfectionSources = immutableCopy(possibleInfectionSources);
        environmentalSources = immutableCopy(environmentalSources);
        environmentalLabTests = immutableCopy(environmentalLabTests);
        contactPersonDetails = immutableCopy(contactPersonDetails);
        screenedGroups = immutableCopy(screenedGroups);
        homePreventiveMeasures = immutableCopy(homePreventiveMeasures);
        outbreakDisinfectionMeasures = immutableCopy(outbreakDisinfectionMeasures);
        infectionCausingConditionCode = immutableCopy(infectionCausingConditionCode);
    }

    @Override
    public CardType type() {
        return CardType.CARD161;
    }

    private static <T> List<T> immutableCopy(List<T> values) {
        return values == null ? List.of() : List.copyOf(values);
    }
}
