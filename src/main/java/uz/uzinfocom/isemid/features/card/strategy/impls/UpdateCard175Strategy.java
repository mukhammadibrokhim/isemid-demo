package uz.uzinfocom.isemid.features.card.strategy.impls;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.dtos.ApiException;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.card.dtos.requests.CardRequest;
import uz.uzinfocom.isemid.features.card.dtos.requests.card175.Card175Request;
import uz.uzinfocom.isemid.features.card.enums.CardStatus;
import uz.uzinfocom.isemid.features.card.enums.CardType;
import uz.uzinfocom.isemid.features.card.models.card175.Card175;
import uz.uzinfocom.isemid.features.card.reposiotries.CardRepository;
import uz.uzinfocom.isemid.features.card.strategy.UpdateCardStrategy;
import uz.uzinfocom.isemid.features.classifier.eums.CatalogType;
import uz.uzinfocom.isemid.features.classifier.services.CatalogService;
import uz.uzinfocom.isemid.features.user.models.User;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class UpdateCard175Strategy implements UpdateCardStrategy {
    final CardRepository cardRepository;
    final CatalogService catalogService;

    @Override
    public ApiResponse<?> updateCard(long id, CardRequest cardRequest, User currentUser) {

        Card175 existingCard = (Card175) cardRepository.findById(id)
                .orElseThrow(() -> new ApiException(NOT_FOUND, MessageUtil.NOT_FOUND));

        if (cardRequest.getCardType() == null || !cardRequest.getCardType().equals(CardType.CARD175)) {
            throw new ApiException(BAD_REQUEST, "Тип карты неправильный.");
        }
        existingCard.setStatus(CardStatus.IN_PROGRESS);

        Card175Request reqCard175 = (Card175Request) cardRequest;

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
        Optional.ofNullable(reqCard175.getInformationAboutLastVaccination())
                .ifPresent(existingCard::setInformationAboutLastVaccination);
        Optional.ofNullable(reqCard175.getBriefEpidemiologicalComment())
                .ifPresent(existingCard::setBriefEpidemiologicalComment);
        Optional.ofNullable(reqCard175.getPlaceOfApplication())
                .ifPresent(existingCard::setPlaceOfApplication);
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
        Optional.ofNullable(reqCard175.getWherePatientCome()).ifPresent(existingCard::setPatientComeCode);
        Optional.ofNullable(reqCard175.getHospitalDischargeStatusCode()).ifPresent(existingCard::setHospitalDischargeStatusCode);
        Optional.ofNullable(reqCard175.getTransportTypeCode()).ifPresent(existingCard::setTransportTypeCode);
        Optional.ofNullable(reqCard175.getReasonOfLeavingHomeCode()).ifPresent(existingCard::setReasonOfLeavingHomeCode);
        Optional.ofNullable(reqCard175.getReasonOfLateHospitalizationCode()).ifPresent(existingCard::setReasonOfLateHospitalizationCode);
        Optional.ofNullable(reqCard175.getDiagnosisConfirmedCode()).ifPresent(existingCard::setDiagnosisConfirmedCode);
        Optional.ofNullable(reqCard175.getInformationAboutVaccinationCode()).ifPresent(existingCard::setInformationAboutVaccinationCode);
        Optional.ofNullable(reqCard175.getInitialDiagnosisCode())
                .ifPresent(existingCard::setInitialDiagnosisCode);
        Optional.ofNullable(reqCard175.getPatientIdentifiedCode())
                .ifPresent(existingCard::setPatientIdentifiedCode);
        Optional.ofNullable(reqCard175.getPreventionAndAidCode())
                .ifPresent(existingCard::setPreventionAndAidCode);
        Optional.ofNullable(reqCard175.getPartOfInjuryCode())
                .ifPresent(codes -> {
                    List<String> codeList = codes.stream()
                            .map(code -> catalogService.findByTypeAndCode(CatalogType.partOfInjury, code))
                            .filter(Optional::isPresent)
                            .map(catalog -> catalog.get().getCode())
                            .toList();
                    existingCard.setPartOfInjury(codeList);
                });
        Optional.ofNullable(reqCard175.getDiseaseSpreaderTypeCode()).ifPresent(existingCard::setDiseaseSpreaderTypeCode);
        Optional.ofNullable(reqCard175.getSeverityOfIllnessCode()).ifPresent(existingCard::setSeverityOfIllnessCode);
        Optional.ofNullable(reqCard175.getRelevanceOfDiseaseToProfessionCode()).ifPresent(existingCard::setRelevanceOfDiseaseToProfessionCode);
        Optional.ofNullable(reqCard175.getOwnerOfDiseaseSpreaderCode()).ifPresent(existingCard::setOwnerOfDiseaseSpreaderCode);

        Optional.ofNullable(reqCard175.getObservationResultOfAnimalsCode())
                .ifPresent(existingCard::setObservationResultOfAnimalsCode);

        Optional.ofNullable(reqCard175.getCheckingDiagnosisCode())
                .ifPresent(existingCard::setCheckingDiagnosisCode);

        Optional.ofNullable(reqCard175.getDiseaseTransmissionConditionCode())
                .ifPresent(codes -> {
                    List<String> codeList = codes.stream()
                            .map(code -> catalogService.findByTypeAndCode(CatalogType.diseaseTransmissionCondition, code))
                            .filter(Optional::isPresent)
                            .map(catalog -> catalog.get().getCode())
                            .toList();
                    existingCard.setDiseaseTransmissionConditionCode(codeList);
                });
        Optional.ofNullable(reqCard175.getPathogenMainFactorCode())
                .ifPresent(codes -> {
                    List<String> codeList = codes.stream()
                            .map(code -> catalogService.findByTypeAndCode(CatalogType.pathogenMainFactor, code))
                            .filter(Optional::isPresent)
                            .map(catalog -> catalog.get().getCode())
                            .collect(Collectors.toList());
                    existingCard.setPathogenMainFactor(codeList);
                });
        Optional.ofNullable(reqCard175.getTakenMeasuresFromResidenceCode())
                .ifPresent(codes -> {
                    List<String> codeList = codes.stream()
                            .map(code -> catalogService.findByTypeAndCode(CatalogType.takenMeasuresFromResidence, code))
                            .filter(Optional::isPresent)
                            .map(catalog -> catalog.get().getCode())
                            .collect(Collectors.toList());
                    existingCard.setTakenMeasuresFromResidence(codeList);
                });
        Card175 updatedCard = cardRepository.save(existingCard);
        return new ApiResponse<>(true, MessageUtil.UPDATED_SUCCESS, updatedCard);
    }
}
