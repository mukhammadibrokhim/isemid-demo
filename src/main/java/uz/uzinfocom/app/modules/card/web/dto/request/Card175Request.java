package uz.uzinfocom.app.modules.card.web.dto.request;

import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.modules.card.domain.enums.CardType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record Card175Request(
        LocalDateTime timeOfEpidemiologicalInvestigation,
        LocalDate dateOfIllness,
        LocalDateTime dateOfDiagnosisOfEmergencyDay,
        LocalDateTime dateOfFinalDiagnosis,
        LocalDate dateOfDischargeFromHospital,
        LocalDate dateOfVaccination,
        @Size(max = 255) String pathogenType,
        @Size(max = 64) String wherePatientComeCode,
        @Size(max = 64) String patientComeCode,
        @Size(max = 64) String hospitalDischargeStatusCode,
        @Size(max = 64) String transportTypeCode,
        @Size(max = 64) String reasonOfLeavingHomeCode,
        @Size(max = 64) String reasonOfLateHospitalizationCode,
        @Size(max = 64) String diagnosisConfirmedCode,
        @Size(max = 64) String informationAboutVaccinationCode,
        @Size(max = 500) String informationAboutLastVaccination,
        @Size(max = 1000) String briefEpidemiologicalComment,
        @Size(max = 64) String initialDiagnosisCode,
        @Size(max = 64) String patientIdentifiedCode,
        @Size(max = 500) String placeOfApplication,
        @Size(max = 64) String preventionAndAidCode,
        @Size(max = 255) String nameOfMedicine,
        Long quantityOfMedicine,
        Long serialNumber,
        Long vaccinationCount,
        @Size(max = 255) String clinicalForm,
        List<String> partOfInjury,
        @Size(max = 64) String severityOfIllnessCode,
        @Size(max = 64) String relevanceOfDiseaseToProfessionCode,
        @Size(max = 64) String diseaseSpreaderTypeCode,
        @Size(max = 64) String ownerOfDiseaseSpreaderCode,
        @Size(max = 64) String observationResultOfAnimalsCode,
        @Size(max = 64) String checkingDiagnosisCode,
        List<String> diseaseTransmissionConditionCode,
        List<String> pathogenMainFactor,
        List<String> takenMeasuresFromResidence
) implements CardRequest {

    public Card175Request {
        partOfInjury = immutableCopy(partOfInjury);
        diseaseTransmissionConditionCode = immutableCopy(diseaseTransmissionConditionCode);
        pathogenMainFactor = immutableCopy(pathogenMainFactor);
        takenMeasuresFromResidence = immutableCopy(takenMeasuresFromResidence);
    }

    @Override
    public CardType type() {
        return CardType.CARD175;
    }

    private static <T> List<T> immutableCopy(List<T> values) {
        return values == null ? List.of() : List.copyOf(values);
    }
}
