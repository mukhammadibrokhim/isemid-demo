package uz.uzinfocom.isemid.services.impls.card.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.exceptioins.ApiException;
import uz.uzinfocom.isemid.models.entities.User;
import uz.uzinfocom.isemid.models.entities.card.CardStatus;
import uz.uzinfocom.isemid.models.entities.card.CardType;
import uz.uzinfocom.isemid.models.entities.card.card174.Card174;
import uz.uzinfocom.isemid.models.entities.card.card174.InfectionMonitoring;
import uz.uzinfocom.isemid.models.entities.card.card174.OutbreakControlMeasure;
import uz.uzinfocom.isemid.models.entities.enums.CatalogType;
import uz.uzinfocom.isemid.models.entities.value_set.Catalog;
import uz.uzinfocom.isemid.models.payload.requests.card.ReqCard;
import uz.uzinfocom.isemid.models.payload.requests.card.card174.ReqCard174;
import uz.uzinfocom.isemid.models.payload.requests.card.card174.ReqInfectionMonitoring;
import uz.uzinfocom.isemid.models.payload.requests.card.card174.ReqOutbreakControlMeasure;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.repositories.card.CardRepository;
import uz.uzinfocom.isemid.services.card.strategy.UpdateCardStrategy;
import uz.uzinfocom.isemid.services.value_set.CatalogService;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class UpdateCard174Strategy implements UpdateCardStrategy {

    final CardRepository cardRepository;
    final CatalogService catalogService;

    private Catalog getCatalogByCode(CatalogType type, String code) throws ApiException {
        return catalogService.findByTypeAndCode(type, code).orElse(null);
    }


    @Override
    public ApiResponse<?> updateCard(long id, ReqCard reqCard, User currentUser) {
        Card174 existingCard = (Card174) cardRepository.findById(id)
                .orElseThrow(() -> new ApiException(NOT_FOUND, "Card174 not found."));

        if (reqCard.getCardType() == null || !reqCard.getCardType().equals(CardType.CARD174)) {
            throw new ApiException(BAD_REQUEST, "Card type is not correct.");
        }
        existingCard.setStatus(CardStatus.IN_PROGRESS);
        ReqCard174 reqCard174 = (ReqCard174) reqCard;

        Optional.ofNullable(reqCard174.getSerialDocNumber()).ifPresent(existingCard::setSerialDocNumber);
        Optional.ofNullable(reqCard174.getMkb10Code()).ifPresent(existingCard::setMkb10Code);
        Optional.ofNullable(reqCard174.getMkb10Name()).ifPresent(existingCard::setMkb10Name);
        Optional.ofNullable(reqCard174.getPathogenType()).ifPresent(existingCard::setPathogenType);
        Optional.ofNullable(reqCard174.getDataObtainedDate()).ifPresent(existingCard::setDataObtainedDate);
        Optional.ofNullable(reqCard174.getReportToVeterinaryDepartmentDate()).ifPresent(existingCard::setReportToVeterinaryDepartmentDate);
        Optional.ofNullable(reqCard174.getAnimalPrimaryDiagnosis()).ifPresent(existingCard::setAnimalPrimaryDiagnosis);
        Optional.ofNullable(reqCard174.getHumanPrimaryDiagnosis()).ifPresent(existingCard::setHumanPrimaryDiagnosis);
        Optional.ofNullable(reqCard174.getInvestigationDate()).ifPresent(existingCard::setInvestigationDate);
        Optional.ofNullable(reqCard174.getLastDiseaseYear()).ifPresent(existingCard::setLastDiseaseYear);
        Optional.ofNullable(reqCard174.getCurrentAnimalInfectionDate()).ifPresent(existingCard::setCurrentAnimalInfectionDate);
        Optional.ofNullable(reqCard174.getOutbreakLocalization()).ifPresent(existingCard::setOutbreakLocalization);
        Optional.ofNullable(reqCard174.getAnimalOwner()).ifPresent(existingCard::setAnimalOwner);
        Optional.ofNullable(reqCard174.getOwnerAddress()).ifPresent(existingCard::setOwnerAddress);
        Optional.ofNullable(reqCard174.getTestDate()).ifPresent(existingCard::setTestDate);
        Optional.ofNullable(reqCard174.getTestSampleCount()).ifPresent(existingCard::setTestSampleCount);
        Optional.ofNullable(reqCard174.getTestingMethod()).ifPresent(existingCard::setTestingMethod);
        Optional.ofNullable(reqCard174.getTestResult()).ifPresent(existingCard::setTestResult);
        Optional.ofNullable(reqCard174.getAffectedHumans()).ifPresent(existingCard::setAffectedHumans);
        Optional.ofNullable(reqCard174.getTreatedHumans()).ifPresent(existingCard::setTreatedHumans);
        Optional.ofNullable(reqCard174.getQuarantineStartDate()).ifPresent(existingCard::setQuarantineStartDate);
        Optional.ofNullable(reqCard174.getQuarantineEndDate()).ifPresent(existingCard::setQuarantineEndDate);
        Optional.ofNullable(reqCard174.getAnimalDisposalDate()).ifPresent(existingCard::setAnimalDisposalDate);
        Optional.ofNullable(reqCard174.getPrecautionaryMeasures()).ifPresent(existingCard::setPrecautionaryMeasures);
        Optional.ofNullable(reqCard174.getIsolation()).ifPresent(existingCard::setIsolation);
        Optional.ofNullable(reqCard174.getMeasureTaken()).ifPresent(existingCard::setMeasureTaken);
        Optional.ofNullable(reqCard174.getLocationOfEvent()).ifPresent(existingCard::setLocationOfEvent);
        Optional.ofNullable(reqCard174.getExecutionControlResults()).ifPresent(existingCard::setExecutionControlResults);
        Optional.ofNullable(reqCard174.getAdditionalMeasuresInfo()).ifPresent(existingCard::setAdditionalMeasuresInfo);
        Optional.ofNullable(reqCard174.getIsAreaExotic()).ifPresent(existingCard::setAreaExotic);
        Optional.ofNullable(reqCard174.getAffectedAnimalTypeCode()).ifPresent(code -> existingCard.setAffectedAnimalType(getCatalogByCode(CatalogType.affectedAnimalType, code)));
        Optional.ofNullable(reqCard174.getAnimalOwnershipCode()).ifPresent(code -> existingCard.setAnimalOwnership(getCatalogByCode(CatalogType.ownerOfDiseaseSpreader, code)));
        Optional.ofNullable(reqCard174.getAnimalDisposalMethodCode()).ifPresent(code -> existingCard.setAnimalDisposalMethod(getCatalogByCode(CatalogType.animalDisposalMethod, code)));
        Optional.ofNullable(reqCard174.getDeratizationCode()).ifPresent(code -> existingCard.setDeratization(getCatalogByCode(CatalogType.deratization, code)));
        Optional.ofNullable(reqCard174.getQuarantineTypeCode()).ifPresent(code -> existingCard.setQuarantineType(getCatalogByCode(CatalogType.quarantineType, code)));
        Optional.ofNullable(reqCard174.getDisinfectionTransmissionFactorsCode())
                .ifPresent(codes -> {
                    List<Catalog> codeList = codes.stream()
                            .map(code -> catalogService.findByTypeAndCode(CatalogType.disinfectionTransmissionFactors, code))
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .collect(Collectors.toList());
                    existingCard.setDisinfectionTransmissionFactors(codeList);
                });
        Optional.ofNullable(reqCard174.getEliminationMethodCodes())
                .ifPresent(codes -> {
                    List<Catalog> codeList = codes.stream()
                            .map(code -> catalogService.findByTypeAndCode(CatalogType.eliminationMethod, code))
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .collect(Collectors.toList());
                    existingCard.setEliminationMethod(codeList);
                });
        Optional.ofNullable(reqCard174.getDiseaseFactorCodes())
                .ifPresent(codes -> {
                    List<Catalog> codeList = codes.stream()
                            .map(code -> catalogService.findByTypeAndCode(CatalogType.diseaseFactors, code))
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .collect(Collectors.toList());
                    existingCard.setDiseaseFactors(codeList);
                });
        Optional.ofNullable(reqCard174.getInfectionMonitoring()).ifPresent(reqInfectionMonitorings -> {
            if (reqInfectionMonitorings.isEmpty()) {
                existingCard.getInfectionMonitoring().clear();
            } else {

                Set<Long> requestIds = reqInfectionMonitorings.stream()
                        .map(ReqInfectionMonitoring::getId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());

                existingCard.getInfectionMonitoring().removeIf(exsistingReq ->
                        exsistingReq.getId() != null && !requestIds.contains(exsistingReq.getId()));


                reqInfectionMonitorings.forEach(reqContactPerson -> existingCard.getInfectionMonitoring().stream()
                        .filter(existingContactPerson -> existingContactPerson.getId() != null && existingContactPerson.getId().equals(reqContactPerson.getId()))
                        .findFirst()
                        .ifPresentOrElse(existingContactPerson -> {
                            existingContactPerson.setSequentialNumber(reqContactPerson.getSequentialNumber());
                            existingContactPerson.setLastName(reqContactPerson.getLastName());
                            existingContactPerson.setAddress(reqContactPerson.getAddress());
                            existingContactPerson.setFirstName(reqContactPerson.getFirstName());
                            existingContactPerson.setMiddleName(reqContactPerson.getMiddleName());
                            existingContactPerson.setGender(getCatalogByCode(CatalogType.gender, reqContactPerson.getGenderCode()));
                            existingContactPerson.setProfession(reqContactPerson.getProfession());
                            existingContactPerson.setBirthDate(reqContactPerson.getBirthDate());
                            existingContactPerson.setApplicationDate(reqContactPerson.getApplicationDate());
                            existingContactPerson.setConfirmationDate(reqContactPerson.getConfirmationDate());
                            existingContactPerson.setPossibleInfectionLocation(reqContactPerson.getPossibleInfectionLocation());
                            existingContactPerson.setPossibleInfectionFactor(reqContactPerson.getPossibleInfectionFactor());
                            existingContactPerson.setPossibleInfectionDate(reqContactPerson.getPossibleInfectionDate());

                        }, () -> {

                            InfectionMonitoring infectionMonitoring = InfectionMonitoring.builder()
                                    .card174(existingCard)
                                    .sequentialNumber(reqContactPerson.getSequentialNumber())
                                    .lastName(reqContactPerson.getLastName())
                                    .address(reqContactPerson.getAddress())
                                    .firstName(reqContactPerson.getFirstName())
                                    .middleName(reqContactPerson.getMiddleName())
                                    .birthDate(reqContactPerson.getBirthDate())
                                    .profession(reqContactPerson.getProfession())
                                    .applicationDate(reqContactPerson.getApplicationDate())
                                    .confirmationDate(reqContactPerson.getConfirmationDate())
                                    .possibleInfectionLocation(reqContactPerson.getPossibleInfectionLocation())
                                    .possibleInfectionFactor(reqContactPerson.getPossibleInfectionFactor())
                                    .possibleInfectionDate(reqContactPerson.getPossibleInfectionDate())
                                    .gender(getCatalogByCode(CatalogType.gender, reqContactPerson.getGenderCode()))
                                    .build();
                            existingCard.getInfectionMonitoring().add(infectionMonitoring);
                        }));
            }
        });

        Optional.ofNullable(reqCard174.getOutbreakControlMeasures()).ifPresent(reqOutbreakControlMeasures -> {
            if (reqOutbreakControlMeasures.isEmpty()) {
                existingCard.getOutbreakControlMeasures().clear();
            } else {

                Set<Long> requestIds = reqOutbreakControlMeasures.stream()
                        .map(ReqOutbreakControlMeasure::getId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());

                existingCard.getOutbreakControlMeasures().removeIf(exsistingReq ->
                        exsistingReq.getId() != null && !requestIds.contains(exsistingReq.getId()));


                reqOutbreakControlMeasures.forEach(reqContactPerson -> existingCard.getOutbreakControlMeasures().stream()
                        .filter(existingContactPerson -> existingContactPerson.getId() != null && existingContactPerson.getId().equals(reqContactPerson.getId()))
                        .findFirst()
                        .ifPresentOrElse(existingContactPerson -> {
                            existingContactPerson.setVaccinatedAnimals(reqContactPerson.getVaccinatedAnimals());
                            existingContactPerson.setLostAnimals(reqContactPerson.getLostAnimals());
                            existingContactPerson.setMeatDelivered(reqContactPerson.getMeatDelivered());
                            existingContactPerson.setProcessingMethod(getCatalogByCode(CatalogType.processingMethod, reqContactPerson.getProcessingMethodCode()));
                            existingContactPerson.setProcessedArea(reqContactPerson.getProcessedArea());
                            existingContactPerson.setEventConducted(reqContactPerson.getEventConducted());

                        }, () -> {

                            OutbreakControlMeasure outbreakControlMeasure = OutbreakControlMeasure.builder()
                                    .card174(existingCard)
                                    .vaccinatedAnimals(reqContactPerson.getVaccinatedAnimals())
                                    .lostAnimals(reqContactPerson.getLostAnimals())
                                    .meatDelivered(reqContactPerson.getMeatDelivered())
                                    .processingMethod(getCatalogByCode(CatalogType.processingMethod, reqContactPerson.getProcessingMethodCode()))
                                    .processedArea(reqContactPerson.getProcessedArea())
                                    .eventConducted(reqContactPerson.getEventConducted())
                                    .build();
                            existingCard.getOutbreakControlMeasures().add(outbreakControlMeasure);
                        }));
            }
        });

        return new ApiResponse<>(true, "Card174 updated successfully.", cardRepository.save(existingCard));
    }

}