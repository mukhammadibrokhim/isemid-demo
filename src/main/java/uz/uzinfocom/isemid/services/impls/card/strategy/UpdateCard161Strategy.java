package uz.uzinfocom.isemid.services.impls.card.strategy;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.exceptioins.ApiException;
import uz.uzinfocom.isemid.models.entities.User;
import uz.uzinfocom.isemid.models.entities.card.Card161;
import uz.uzinfocom.isemid.models.entities.enums.CardType;
import uz.uzinfocom.isemid.models.entities.enums.CatalogType;
import uz.uzinfocom.isemid.models.payload.requests.card.ReqCard;
import uz.uzinfocom.isemid.models.payload.requests.card.ReqCard161;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.repositories.card.CardRepository;
import uz.uzinfocom.isemid.services.card.strategy.UpdateCardStrategy;
import uz.uzinfocom.isemid.services.value_set.CatalogService;

import java.util.Optional;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional
public class UpdateCard161Strategy implements UpdateCardStrategy {
    final CardRepository cardRepository;
    final CatalogService catalogService;

    @Override
    public ApiResponse<?> updateCard(long id, ReqCard reqCard, User currentUser) {
        Card161 existingCard = (Card161) cardRepository.findById(id)
                .orElseThrow(() -> new ApiException(NOT_FOUND, "Card161 not found."));

        if (reqCard.getCardType() == null || !reqCard.getCardType().equals(CardType.CARD161)) {
            throw new ApiException(BAD_REQUEST, "Card type is not correct.");
        }
        ReqCard161 reqCard161 = (ReqCard161) reqCard;
        Optional.ofNullable(reqCard161.getResidentialTreatmentFacility()).ifPresent(existingCard::setResidentialTreatmentFacility);
        Optional.ofNullable(reqCard161.getRegion()).ifPresent(existingCard::setRegion);
        Optional.ofNullable(reqCard161.getDistrict()).ifPresent(existingCard::setDistrict);
        Optional.ofNullable(reqCard161.getPolyclinic()).ifPresent(existingCard::setPolyclinic);
        Optional.ofNullable(reqCard161.getInitialSymptoms()).ifPresent(existingCard::setInitialSymptoms);
        Optional.ofNullable(reqCard161.getDetectedCode()).ifPresent(code -> catalogService.findByTypeAndCode(CatalogType.detected, code));
        Optional.ofNullable(reqCard161.getEpidemiologicalExamDate()).ifPresent(existingCard::setEpidemiologicalExamDate);
        Optional.ofNullable(reqCard161.getObservationEndDate()).ifPresent(existingCard::setObservationEndDate);
        Optional.ofNullable(reqCard161.getFinalDiagnosisDate()).ifPresent(existingCard::setFinalDiagnosisDate);
//        Optional.ofNullable(reqCard161.getDeliveryMethodCode()).ifPresent(existingCard::setDeliveryMethodCode);
//        Optional.ofNullable(reqCard161.getHomeStayExclusionReasonCode()).ifPresent(existingCard::setHomeStayExclusionReasonCode);
//        Optional.ofNullable(reqCard161.getLateAdmissionReasonsCode()).ifPresent(existingCard::setLateAdmissionReasonsCode);
        Optional.ofNullable(reqCard161.getDiagnosisVerified()).ifPresent(existingCard::setDiagnosisVerified);
        Optional.ofNullable(reqCard161.getEstimatedInfectionDate()).ifPresent(existingCard::setEstimatedInfectionDate);
//        Optional.ofNullable(reqCard161.getRiskFactorCodes()).ifPresent(existingCard::setRiskFactorCodes);
        Optional.ofNullable(reqCard161.getAddressLocation()).ifPresent(existingCard::setAddressLocation);
        Optional.ofNullable(reqCard161.getSeasonTime()).ifPresent(existingCard::setSeasonTime);
        Optional.ofNullable(reqCard161.getFullName()).ifPresent(existingCard::setFullName);
        Optional.ofNullable(reqCard161.getDiagnosisClinicalFormOrDonorStatus()).ifPresent(existingCard::setDiagnosisClinicalFormOrDonorStatus);
        Optional.ofNullable(reqCard161.getContactInfoAndDonorResidence()).ifPresent(existingCard::setContactInfoAndDonorResidence);
        Optional.ofNullable(reqCard161.getTestResult()).ifPresent(existingCard::setTestResult);
        Optional.ofNullable(reqCard161.getFoodAndWaterSourceTypes()).ifPresent(existingCard::setFoodAndWaterSourceTypes);
        Optional.ofNullable(reqCard161.getCollectionLocationAndTime()).ifPresent(existingCard::setCollectionLocationAndTime);
        Optional.ofNullable(reqCard161.getUsageLocationAndTime()).ifPresent(existingCard::setUsageLocationAndTime);
        Optional.ofNullable(reqCard161.getStorageConditions()).ifPresent(existingCard::setStorageConditions);
        Optional.ofNullable(reqCard161.getQualityFeedbackFromPatientAndOthers()).ifPresent(existingCard::setQualityFeedbackFromPatientAndOthers);
//        Optional.ofNullable(reqCard161.getLivingConditionCode()).ifPresent(existingCard::setLivingConditionCode);
        Optional.of(reqCard161.getNumberOfPeople()).ifPresent(existingCard::setNumberOfPeople);
        Optional.of(reqCard161.getNumberOfRooms()).ifPresent(existingCard::setNumberOfRooms);
        Optional.ofNullable(reqCard161.getArea()).ifPresent(existingCard::setArea);
//        Optional.ofNullable(reqCard161.getWaterSupplyCode()).ifPresent(existingCard::setWaterSupplyCode);
//        Optional.ofNullable(reqCard161.getLiquidWasteDisposalTypeCode()).ifPresent(existingCard::setLiquidWasteDisposalTypeCode);
//        Optional.ofNullable(reqCard161.getSolidWasteDisposalTypeCode()).ifPresent(existingCard::setSolidWasteDisposalTypeCode);
//        Optional.ofNullable(reqCard161.getRoomConditionCode()).ifPresent(existingCard::setRoomConditionCode);
//        Optional.ofNullable(reqCard161.getYardConditionCode()).ifPresent(existingCard::setYardConditionCode);
//        Optional.ofNullable(reqCard161.getAreaConditionCode()).ifPresent(existingCard::setAreaConditionCode);
        Optional.ofNullable(reqCard161.getHasLice()).ifPresent(existingCard::setHasLice);
//        Optional.ofNullable(reqCard161.isHasOtherInsects()).ifPresent(existingCard::setHasOtherInsects);
//        Optional.ofNullable(reqCard161.isHasRodents()).ifPresent(existingCard::setHasRodents);
        Optional.ofNullable(reqCard161.getImportantCausesOfDisease()).ifPresent(existingCard::setImportantCausesOfDisease);
//        Optional.ofNullable(reqCard161.getVisitedObjectsCode()).ifPresent(existingCard::setVisitedObjectsCode);
//        Optional.ofNullable(reqCard161.isDenselyPopulated()).ifPresent(existingCard::setDenselyPopulated);
        Optional.ofNullable(reqCard161.getIsolationStatus()).ifPresent(existingCard::setIsolationStatus);
        Optional.ofNullable(reqCard161.getWaterSupplyStatus()).ifPresent(existingCard::setWaterSupplyStatus);
        Optional.ofNullable(reqCard161.getSanitaryMaintenance()).ifPresent(existingCard::setSanitaryMaintenance);
        Optional.ofNullable(reqCard161.getFoodStorage()).ifPresent(existingCard::setFoodStorage);
        Optional.ofNullable(reqCard161.getFoodPreparation()).ifPresent(existingCard::setFoodPreparation);
        Optional.ofNullable(reqCard161.getDiseaseCausingFactors()).ifPresent(existingCard::setDiseaseCausingFactors);
        Optional.ofNullable(reqCard161.getExaminationDate()).ifPresent(existingCard::setExaminationDate);
        Optional.ofNullable(reqCard161.getObjectArthropodsAnimals()).ifPresent(existingCard::setObjectArthropodsAnimals);
        Optional.ofNullable(reqCard161.getMaterial()).ifPresent(existingCard::setMaterial);
        Optional.ofNullable(reqCard161.getSampleQuantity()).ifPresent(existingCard::setSampleQuantity);
        Optional.ofNullable(reqCard161.getTestTypeAndResult()).ifPresent(existingCard::setTestTypeAndResult);
        Optional.ofNullable(reqCard161.getContactFullName()).ifPresent(existingCard::setContactFullName);
        Optional.ofNullable(reqCard161.getContactAge()).ifPresent(existingCard::setContactAge);
        Optional.ofNullable(reqCard161.getContactAddress()).ifPresent(existingCard::setContactAddress);
        Optional.ofNullable(reqCard161.getContactJobTypeAndLocation()).ifPresent(existingCard::setContactJobTypeAndLocation);
        Optional.ofNullable(reqCard161.getContactImmunizationStatus()).ifPresent(existingCard::setContactImmunizationStatus);
        Optional.ofNullable(reqCard161.getContactRestrictionMeasures()).ifPresent(existingCard::setContactRestrictionMeasures);
        Optional.ofNullable(reqCard161.getTeamName()).ifPresent(existingCard::setTeamName);
        Optional.ofNullable(reqCard161.getProphylacticAddress()).ifPresent(existingCard::setProphylacticAddress);
        Optional.ofNullable(reqCard161.getContactCount()).ifPresent(existingCard::setContactCount);
        Optional.ofNullable(reqCard161.getRequiredProphylacticSubstance()).ifPresent(existingCard::setRequiredProphylacticSubstance);
        Optional.ofNullable(reqCard161.getTreatedWithProphylacticSubstance()).ifPresent(existingCard::setTreatedWithProphylacticSubstance);
        Optional.ofNullable(reqCard161.getLaboratoryTestConducted()).ifPresent(existingCard::setLaboratoryTestConducted);
        Optional.ofNullable(reqCard161.getNotifiedPerson()).ifPresent(existingCard::setNotifiedPerson);
        Optional.ofNullable(reqCard161.getNextDiseasePrevention()).ifPresent(existingCard::setNextDiseasePrevention);
        Optional.ofNullable(reqCard161.getLaboratoryExamination()).ifPresent(existingCard::setLaboratoryExamination);
        Optional.ofNullable(reqCard161.getObservationResult()).ifPresent(existingCard::setObservationResult);

        Card161 updatedCard = cardRepository.save(existingCard);
        return new ApiResponse<>(true, "Card161 updated successfully.", updatedCard);
    }
}