package uz.uzinfocom.isemid.services.impls;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.models.entities.User;
import uz.uzinfocom.isemid.models.entities.card.Card175;
import uz.uzinfocom.isemid.models.payload.requests.ReqCard175;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.repositories.card.Card175Repository;
import uz.uzinfocom.isemid.services.Card175Service;

@Service
@RequiredArgsConstructor
public class Card175ServiceImpl implements Card175Service {

    private final Card175Repository card175Repository;

    @Override
    public ApiResponse<Card175> createCard175(ReqCard175 reqCard175, User user){
        Card175 card175 = Card175.builder()
                  .timeOfEpidemiologicalInvestigation(reqCard175.getTimeOfEpidemiologicalInvestigation())
                  .dateOfIllness(reqCard175.getDateOfIllness())
                  .dateOfDiagnosisOfEmergencyDay(reqCard175.getDateOfDiagnosisOfEmergencyDay())
                  .dateOfFinalDiagnosis(reqCard175.getDateOfFinalDiagnosis())
                  .dateOfDischargeFromHospital(reqCard175.getDateOfDischargeFromHospital())
                  .dateOfVaccination(reqCard175.getDateOfVaccination())
                  .pathogenType(reqCard175.getPathogenType())
                  .wherePatientCome(reqCard175.getWherePatientCome())
                  .hospitalDischargeStatus(reqCard175.getHospitalDischargeStatus())
                  .transportType(reqCard175.getTransportType())
                  .reasonOfLeavingHome(reqCard175.getReasonOfLeavingHome())
                  .reasonOfLateHospitalization(reqCard175.getReasonOfLateHospitalization())
                  .isDiagnosisConfirmed(reqCard175.getIsDiagnosisConfirmed())
                  .informationAboutVaccination(reqCard175.getInformationAboutVaccination())
                  .informationAboutLastVaccination(reqCard175.getInformationAboutLastVaccination())
                  .briefEpidemiologicalComment(reqCard175.getBriefEpidemiologicalComment())
                  .initialDiagnosis(reqCard175.getInitialDiagnosis())
                  .patientIdentified(reqCard175.getPatientIdentified())
                  .placeOfApplication(reqCard175.getPlaceOfApplication())
                  .immediatePreventionAndAntirabicAid(reqCard175.getImmediatePreventionAndAntirabicAid())
                  .informationAboutImmediatePreventionAndAntirabicAid(reqCard175.getInformationAboutImmediatePreventionAndAntirabicAid())
                  .nameOfMedicine(reqCard175.getNameOfMedicine())
                  .quantityOfMedicine(reqCard175.getQuantityOfMedicine())
                  .serialNumber(reqCard175.getSerialNumber())
                  .vaccinationCount(reqCard175.getVaccinationCount())
                  .clinicalForm(reqCard175.getClinicalForm())
                  .partOfInjury(reqCard175.getPartOfInjury())
                  .severityOfIllness(reqCard175.getSeverityOfIllness())
                  .relevanceOfDiseaseToProfession(reqCard175.getRelevanceOfDiseaseToProfession())
                  .diseaseTransmissionCondition(reqCard175.getDiseaseTransmissionCondition())
                  .ownerOfDiseaseSpreader(reqCard175.getOwnerOfDiseaseSpreader())
                  .observationResultOfAnimals(reqCard175.getObservationResultOfAnimals())
                  .isCheckingDiagnosis(reqCard175.getIsCheckingDiagnosis())
                  .diseaseTransmissionCondition(reqCard175.getDiseaseTransmissionCondition())
                  .pathogenMainFactor(reqCard175.getPathogenMainFactor())
                  .takenMeasuresFromResidence(reqCard175.getTakenMeasuresFromResidence())
                  .build();
          return new ApiResponse<>(true, "Карта 175 успешно создана.", card175Repository.save(card175));

    }
}

