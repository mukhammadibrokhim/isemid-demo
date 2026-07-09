package uz.uzinfocom.app.modules.card.application.query.dto.detail;

import uz.uzinfocom.app.modules.card.domain.enums.CardStatus;
import uz.uzinfocom.app.modules.card.domain.enums.CardType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record Card175DetailResponse(
        Long id,
        CardType type,
        CardStatus status,
        Long formId,
        Long assignedById,
        String supervisorComment,
        String attachedUserComment,
        LocalDate completedDate,

        LocalDateTime timeOfEpidemiologicalInvestigation,
        LocalDate dateOfIllness,
        LocalDateTime dateOfDiagnosisOfEmergencyDay,
        LocalDateTime dateOfFinalDiagnosis,
        LocalDate dateOfDischargeFromHospital,
        LocalDate dateOfVaccination,
        String pathogenType,
        String wherePatientComeCode,
        String patientComeCode,
        String hospitalDischargeStatusCode,
        String transportTypeCode,
        String reasonOfLeavingHomeCode,
        String reasonOfLateHospitalizationCode,
        String diagnosisConfirmedCode,
        String informationAboutVaccinationCode,
        String informationAboutLastVaccination,
        String briefEpidemiologicalComment,
        String initialDiagnosisCode,
        String patientIdentifiedCode,
        String placeOfApplication,
        String preventionAndAidCode,
        String nameOfMedicine,
        Long quantityOfMedicine,
        Long serialNumber,
        Long vaccinationCount,
        String clinicalForm,
        List<String> partOfInjury,
        String severityOfIllnessCode,
        String relevanceOfDiseaseToProfessionCode,
        String diseaseSpreaderTypeCode,
        String ownerOfDiseaseSpreaderCode,
        String observationResultOfAnimalsCode,
        String checkingDiagnosisCode,
        List<String> diseaseTransmissionConditionCode,
        List<String> pathogenMainFactor,
        List<String> takenMeasuresFromResidence
) implements CardDetailResponse {
}
