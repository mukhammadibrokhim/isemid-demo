package uz.uzinfocom.isemid.services.impls.card.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.exceptioins.ApiException;
import uz.uzinfocom.isemid.models.entities.User;
import uz.uzinfocom.isemid.models.entities.card.Card161;
import uz.uzinfocom.isemid.models.entities.card.Card174;
import uz.uzinfocom.isemid.models.entities.card.Card175;
import uz.uzinfocom.isemid.models.entities.enums.CardType;
import uz.uzinfocom.isemid.models.payload.requests.card.ReqCard;
import uz.uzinfocom.isemid.models.payload.requests.card.ReqCard175;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.repositories.card.CardRepository;
import uz.uzinfocom.isemid.services.card.strategy.UpdateCardStrategy;

import java.util.Optional;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class UpdateCard175Strategy implements UpdateCardStrategy {
    final CardRepository cardRepository;

    @Override
    public ApiResponse<?> updateCard(long id, ReqCard reqCard, User currentUser) {

        Card175 existingCard = (Card175) cardRepository.findById(id)
                .orElseThrow(() -> new ApiException(NOT_FOUND, "Card175 not found."));

        if (reqCard.getCardType() == null || !reqCard.getCardType().equals(CardType.CARD175)) {
            throw new ApiException(BAD_REQUEST, "Card type is not correct.");
        }
        ReqCard175 reqCard175 = (ReqCard175) reqCard;
        Optional.ofNullable(reqCard175.getTimeOfEpidemiologicalInvestigation())
                .ifPresent(existingCard::setTimeOfEpidemiologicalInvestigation);
        Optional.ofNullable(reqCard175.getDateOfIllness())
                .ifPresent(existingCard::setDateOfIllness);
        Optional.ofNullable(reqCard175.getDateOfDiagnosisOfEmergencyDay())
                .ifPresent(existingCard::setDateOfDiagnosisOfEmergencyDay);
        Optional.ofNullable(reqCard175.getDateOfFinalDiagnosis())
                .ifPresent(existingCard::setDateOfFinalDiagnosis);
        Optional.ofNullable(reqCard175.getDateOfDischargeFromHospital())
                .ifPresent(existingCard::setDateOfDischargeFromHospital);
        Optional.ofNullable(reqCard175.getDateOfVaccination())
                .ifPresent(existingCard::setDateOfVaccination);
        Optional.ofNullable(reqCard175.getPathogenType())
                .ifPresent(existingCard::setPathogenType);
        Optional.ofNullable(reqCard175.getWherePatientCome())
                .ifPresent(existingCard::setWherePatientCome);
        Optional.ofNullable(reqCard175.getHospitalDischargeStatus())
                .ifPresent(existingCard::setHospitalDischargeStatus);
        Optional.ofNullable(reqCard175.getTransportType())
                .ifPresent(existingCard::setTransportType);
        Optional.ofNullable(reqCard175.getReasonOfLeavingHome())
                .ifPresent(existingCard::setReasonOfLeavingHome);
        Optional.ofNullable(reqCard175.getReasonOfLateHospitalization())
                .ifPresent(existingCard::setReasonOfLateHospitalization);
        Optional.ofNullable(reqCard175.getIsDiagnosisConfirmed())
                .ifPresent(existingCard::setIsDiagnosisConfirmed);
        Optional.ofNullable(reqCard175.getInformationAboutVaccination())
                .ifPresent(existingCard::setInformationAboutVaccination);
        Optional.ofNullable(reqCard175.getInformationAboutLastVaccination())
                .ifPresent(existingCard::setInformationAboutLastVaccination);
        Optional.ofNullable(reqCard175.getBriefEpidemiologicalComment())
                .ifPresent(existingCard::setBriefEpidemiologicalComment);
        Optional.ofNullable(reqCard175.getInitialDiagnosis())
                .ifPresent(existingCard::setInitialDiagnosis);
        Optional.ofNullable(reqCard175.getPatientIdentified())
                .ifPresent(existingCard::setPatientIdentified);
        Optional.ofNullable(reqCard175.getPlaceOfApplication())
                .ifPresent(existingCard::setPlaceOfApplication);
        Optional.ofNullable(reqCard175.getImmediatePreventionAndAntirabicAid())
                .ifPresent(existingCard::setImmediatePreventionAndAntirabicAid);
        Optional.ofNullable(reqCard175.getInformationAboutImmediatePreventionAndAntirabicAid())
                .ifPresent(existingCard::setInformationAboutImmediatePreventionAndAntirabicAid);
        Optional.ofNullable(reqCard175.getNameOfMedicine())
                .ifPresent(existingCard::setNameOfMedicine);
        Optional.ofNullable(reqCard175.getQuantityOfMedicine())
                .ifPresent(existingCard::setQuantityOfMedicine);
        Optional.ofNullable(reqCard175.getSerialNumber())
                .ifPresent(existingCard::setSerialNumber);
        Optional.ofNullable(reqCard175.getVaccinationCount())
                .ifPresent(existingCard::setVaccinationCount);
        Optional.ofNullable(reqCard175.getClinicalForm())
                .ifPresent(existingCard::setClinicalForm);
        Optional.ofNullable(reqCard175.getPartOfInjury())
                .ifPresent(existingCard::setPartOfInjury);
        Optional.ofNullable(reqCard175.getSeverityOfIllness())
                .ifPresent(existingCard::setSeverityOfIllness);
        Optional.ofNullable(reqCard175.getRelevanceOfDiseaseToProfession())
                .ifPresent(existingCard::setRelevanceOfDiseaseToProfession);
        Optional.ofNullable(reqCard175.getDiseaseSpreaderType())
                .ifPresent(existingCard::setDiseaseSpreaderType);
        Optional.ofNullable(reqCard175.getOwnerOfDiseaseSpreader())
                .ifPresent(existingCard::setOwnerOfDiseaseSpreader);
        Optional.ofNullable(reqCard175.getObservationResultOfAnimals())
                .ifPresent(existingCard::setObservationResultOfAnimals);
        Optional.ofNullable(reqCard175.getIsCheckingDiagnosis())
                .ifPresent(existingCard::setIsCheckingDiagnosis);
        Optional.ofNullable(reqCard175.getDiseaseTransmissionCondition())
                .ifPresent(existingCard::setDiseaseTransmissionCondition);
        Optional.ofNullable(reqCard175.getPathogenMainFactor())
                .ifPresent(existingCard::setPathogenMainFactor);
        Optional.ofNullable(reqCard175.getTakenMeasuresFromResidence())
                .ifPresent(existingCard::setTakenMeasuresFromResidence);

        Card175 updatedCard = cardRepository.save(existingCard);
        return new ApiResponse<>(true, "Card175 updated successfully.", updatedCard);
    }
}
