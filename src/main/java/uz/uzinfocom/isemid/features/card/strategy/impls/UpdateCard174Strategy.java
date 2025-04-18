package uz.uzinfocom.isemid.features.card.strategy.impls;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.card.dtos.requests.CardRequest;
import uz.uzinfocom.isemid.features.card.dtos.requests.card174.Card174Request;
import uz.uzinfocom.isemid.features.card.dtos.requests.card174.InfectionMonitoringRequest;
import uz.uzinfocom.isemid.features.card.dtos.requests.card174.OutbreakControlMeasureRequest;
import uz.uzinfocom.isemid.features.card.enums.CardStatus;
import uz.uzinfocom.isemid.features.card.models.Card;
import uz.uzinfocom.isemid.features.card.models.card174.Card174;
import uz.uzinfocom.isemid.features.card.models.card174.InfectionMonitoring;
import uz.uzinfocom.isemid.features.card.models.card174.OutbreakControlMeasure;
import uz.uzinfocom.isemid.features.card.reposiotries.CardRepository;
import uz.uzinfocom.isemid.features.card.strategy.UpdateCardStrategy;
import uz.uzinfocom.isemid.features.classifier.eums.CatalogType;
import uz.uzinfocom.isemid.features.classifier.services.CatalogService;
import uz.uzinfocom.isemid.features.user.models.User;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static uz.uzinfocom.isemid.features.card.utils.CardUtil.validateCard;

@Service
@RequiredArgsConstructor
public class UpdateCard174Strategy implements UpdateCardStrategy {

    private final CardRepository cardRepository;
    private final CatalogService catalogService;

    @Override
    public ApiResponse<?> updateCard(long id, CardRequest reqCard, User currentUser) {
        Card card = validateCard(id, reqCard, cardRepository);
        Card174 existingCard = (Card174) card;

        existingCard.setStatus(CardStatus.IN_PROGRESS);
        Card174Request reqCard174 = (Card174Request) reqCard;

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
        Optional.ofNullable(reqCard174.getAffectedAnimalTypeCode()).ifPresent(existingCard::setAffectedAnimalTypeCode);
        Optional.ofNullable(reqCard174.getAnimalOwnershipCode()).ifPresent(existingCard::setAnimalOwnershipCode);
        Optional.ofNullable(reqCard174.getAnimalDisposalMethodCode()).ifPresent(existingCard::setAnimalDisposalMethodCode);
        Optional.ofNullable(reqCard174.getDeratizationCode()).ifPresent(existingCard::setDeratizationCode);
        Optional.ofNullable(reqCard174.getQuarantineTypeCode()).ifPresent(existingCard::setQuarantineTypeCode);
        Optional.ofNullable(reqCard174.getDisinfectionTransmissionFactorsCode())
                .ifPresent(codes -> {
                    List<String> codeList = codes.stream()
                            .map(code -> catalogService.findByTypeAndCode(CatalogType.disinfectionTransmissionFactors, code))
                            .filter(Optional::isPresent)  // Faqat mavjud kataloglarni olish
                            .map(catalog -> catalog.get().getCode())  // Katalogdan kodni olish
                            .collect(Collectors.toList());
                    existingCard.setDisinfectionTransmissionFactorCodes(codeList);  // Kodlar ro'yxatini o'rnatish
                });
        Optional.ofNullable(reqCard174.getEliminationMethodCodes())
                .ifPresent(codes -> {
                    List<String> codeList = codes.stream()
                            .map(code -> catalogService.findByTypeAndCode(CatalogType.eliminationMethod, code))
                            .filter(Optional::isPresent)
                            .map(catalog -> catalog.get().getCode())
                            .collect(Collectors.toList());
                    existingCard.setEliminationMethodCodes(codeList);
                });
        Optional.ofNullable(reqCard174.getDiseaseFactorCodes())
                .ifPresent(codes -> {
                    List<String> codeList = codes.stream()
                            .map(code -> catalogService.findByTypeAndCode(CatalogType.diseaseFactors, code))
                            .filter(Optional::isPresent)
                            .map(catalog -> catalog.get().getCode())
                            .collect(Collectors.toList());
                    existingCard.setDiseaseFactorCodes(codeList);
                });
        Optional.ofNullable(reqCard174.getInfectionMonitoring()).ifPresent(reqInfectionMonitorings -> {
            if (reqInfectionMonitorings.isEmpty()) {
                existingCard.getInfectionMonitoring().clear();
            } else {

                Set<Long> requestIds = reqInfectionMonitorings.stream()
                        .map(InfectionMonitoringRequest::getId)
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
                            existingContactPerson.setGenderCode(reqContactPerson.getGenderCode());
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
                                    .genderCode(reqContactPerson.getGenderCode())
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
                        .map(OutbreakControlMeasureRequest::getId)
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
                            existingContactPerson.setProcessingMethodCode(reqContactPerson.getProcessingMethodCode());
                            existingContactPerson.setProcessedArea(reqContactPerson.getProcessedArea());
                            existingContactPerson.setEventConducted(reqContactPerson.getEventConducted());

                        }, () -> {

                            OutbreakControlMeasure outbreakControlMeasure = OutbreakControlMeasure.builder()
                                    .card174(existingCard)
                                    .vaccinatedAnimals(reqContactPerson.getVaccinatedAnimals())
                                    .lostAnimals(reqContactPerson.getLostAnimals())
                                    .meatDelivered(reqContactPerson.getMeatDelivered())
                                    .processingMethodCode(reqContactPerson.getProcessingMethodCode())
                                    .processedArea(reqContactPerson.getProcessedArea())
                                    .eventConducted(reqContactPerson.getEventConducted())
                                    .build();
                            existingCard.getOutbreakControlMeasures().add(outbreakControlMeasure);
                        }));
            }
        });

        return new ApiResponse<>(true, MessageUtil.UPDATED_SUCCESS, cardRepository.save(existingCard));
    }

}