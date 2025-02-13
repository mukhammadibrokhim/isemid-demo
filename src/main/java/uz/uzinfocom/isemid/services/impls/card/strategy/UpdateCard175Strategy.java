package uz.uzinfocom.isemid.services.impls.card.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.exceptioins.ApiException;
import uz.uzinfocom.isemid.models.entities.User;
import uz.uzinfocom.isemid.models.entities.card.Card175;
import uz.uzinfocom.isemid.models.entities.enums.CardType;
import uz.uzinfocom.isemid.models.entities.enums.CatalogType;
import uz.uzinfocom.isemid.models.entities.value_set.Catalog;
import uz.uzinfocom.isemid.models.payload.requests.card.ReqCard;
import uz.uzinfocom.isemid.models.payload.requests.card.ReqCard175;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.repositories.card.CardRepository;
import uz.uzinfocom.isemid.services.card.strategy.UpdateCardStrategy;
import uz.uzinfocom.isemid.services.value_set.CatalogService;

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
        Optional.ofNullable(reqCard175.getWherePatientCome())
                .ifPresent(code -> existingCard.setWherePatientCome(catalogService.findByTypeAndCode
                        (CatalogType.wherePatientCome, code).orElse(null)));
        Optional.ofNullable(reqCard175.getHospitalDischargeStatusCode())
                .ifPresent(code -> existingCard.setHospitalDischargeStatus(catalogService.findByTypeAndCode
                        (CatalogType.hospitalDischargeStatus, code).orElse(null)));
        Optional.ofNullable(reqCard175.getTransportTypeCode())
                .ifPresent(code -> existingCard.setTransportType(catalogService.findByTypeAndCode
                        (CatalogType.transportType, code).orElse(null)));
        Optional.ofNullable(reqCard175.getReasonOfLeavingHomeCode())
                .ifPresent(code -> existingCard.setReasonOfLeavingHome(catalogService.findByTypeAndCode
                        (CatalogType.reasonOfLeavingHome, code).orElse(null)));
        Optional.ofNullable(reqCard175.getReasonOfLateHospitalizationCode())
                .ifPresent(code -> existingCard.setReasonOfLateHospitalization(catalogService.findByTypeAndCode
                        (CatalogType.reasonOfLateHospitalization, code).orElse(null)));
        Optional.ofNullable(reqCard175.getDiagnosisConfirmedCode())
                .ifPresent(code -> existingCard.setReasonOfLateHospitalization(catalogService.findByTypeAndCode
                        (CatalogType.diagnosisConfirmed, code).orElse(null)));
        Optional.ofNullable(reqCard175.getInformationAboutVaccinationCode())
                .ifPresent(code -> existingCard.setInformationAboutVaccination(catalogService.findByTypeAndCode
                        (CatalogType.informationAboutVaccination, code).orElse(null)));
        Optional.ofNullable(reqCard175.getInitialDiagnosisCode())
                .ifPresent(code -> existingCard.setInitialDiagnosis(catalogService.findByTypeAndCode
                        (CatalogType.initialDiagnosis, code).orElse(null)));
        Optional.ofNullable(reqCard175.getPatientIdentifiedCode())
                .ifPresent(code -> existingCard.setPatientIdentified(catalogService.findByTypeAndCode
                        (CatalogType.patientIdentified, code).orElse(null)));
        Optional.ofNullable(reqCard175.getImmediatePreventionAndAntirabicAidCode())
                .ifPresent(code -> existingCard.setImmediatePreventionAndAntirabicAid(catalogService.findByTypeAndCode
                        (CatalogType.immediatePreventionAndAntirabicAid, code).orElse(null)));
        Optional.ofNullable(reqCard175.getInformationAboutImmediatePreventionAndAntirabicAidCode())
                .ifPresent(code -> existingCard.setInformationAboutImmediatePreventionAndAntirabicAid(catalogService.findByTypeAndCode
                        (CatalogType.informationAboutImmediatePreventionAndAntirabicAid, code).orElse(null)));
        Optional.ofNullable(reqCard175.getPartOfInjuryCode())
                .ifPresent(codes -> {
                    List<Catalog> codeList = codes.stream()
                            .map(code -> catalogService.findByTypeAndCode(CatalogType.partOfInjury, code))
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .collect(Collectors.toList());
                    existingCard.setPartOfInjury(codeList);
                });
        Optional.ofNullable(reqCard175.getDiseaseSpreaderTypeCode())
                .ifPresent(code -> existingCard.setDiseaseSpreaderType(catalogService.findByTypeAndCode
                        (CatalogType.diseaseSpreaderType, code).orElse(null)));
        Optional.ofNullable(reqCard175.getSeverityOfIllnessCode())
                .ifPresent(code -> existingCard.setSeverityOfIllness(catalogService.findByTypeAndCode
                        (CatalogType.severityOfIllness, code).orElse(null)));

        Optional.ofNullable(reqCard175.getRelevanceOfDiseaseToProfessionCode())
                .ifPresent(code -> existingCard.setRelevanceOfDiseaseToProfession(catalogService.findByTypeAndCode
                        (CatalogType.relevanceOfDiseaseToProfession, code).orElse(null)));

        Optional.ofNullable(reqCard175.getOwnerOfDiseaseSpreaderCode())
                .ifPresent(code -> existingCard.setOwnerOfDiseaseSpreader(catalogService.findByTypeAndCode
                        (CatalogType.ownerOfDiseaseSpreader, code).orElse(null)));

        Optional.ofNullable(reqCard175.getObservationResultOfAnimalsCode())
                .ifPresent(code -> existingCard.setObservationResultOfAnimals(catalogService.findByTypeAndCode
                        (CatalogType.observationResultOfAnimals, code).orElse(null)));

        Optional.ofNullable(reqCard175.getCheckingDiagnosisCode())
                .ifPresent(code -> existingCard.setCheckingDiagnosis(catalogService.findByTypeAndCode
                        (CatalogType.checkingDiagnosis, code).orElse(null)));

        Optional.ofNullable(reqCard175.getDiseaseTransmissionConditionCode())
                .ifPresent(codes -> {
                    List<Catalog> codeList = codes.stream()
                            .map(code -> catalogService.findByTypeAndCode(CatalogType.diseaseTransmissionCondition, code))
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .collect(Collectors.toList());
                    existingCard.setDiseaseTransmissionCondition(codeList);
                });
        Optional.ofNullable(reqCard175.getPathogenMainFactorCode())
                .ifPresent(codes -> {
                    List<Catalog> codeList = codes.stream()
                            .map(code -> catalogService.findByTypeAndCode(CatalogType.pathogenMainFactor, code))
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .collect(Collectors.toList());
                    existingCard.setPathogenMainFactor(codeList);
                });
        Optional.ofNullable(reqCard175.getTakenMeasuresFromResidenceCode())
                .ifPresent(codes -> {
                    List<Catalog> codeList = codes.stream()
                            .map(code -> catalogService.findByTypeAndCode(CatalogType.takenMeasuresFromResidence, code))
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .collect(Collectors.toList());
                    existingCard.setTakenMeasuresFromResidence(codeList);
                });
        Card175 updatedCard = cardRepository.save(existingCard);
        return new ApiResponse<>(true, "Card175 updated successfully.", updatedCard);
    }
}
