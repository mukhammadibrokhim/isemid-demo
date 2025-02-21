package uz.uzinfocom.isemid.services.impls.card.strategy;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.exceptioins.ApiException;
import uz.uzinfocom.isemid.models.entities.User;
import uz.uzinfocom.isemid.models.entities.card.CardType;
import uz.uzinfocom.isemid.models.entities.card.card161.*;
import uz.uzinfocom.isemid.models.entities.enums.CatalogType;
import uz.uzinfocom.isemid.models.entities.value_set.Catalog;
import uz.uzinfocom.isemid.models.payload.requests.card.ReqCard;
import uz.uzinfocom.isemid.models.payload.requests.card.card161.ReqCard161;
import uz.uzinfocom.isemid.models.payload.requests.card.card161.ReqHomePreventiveMeasure;
import uz.uzinfocom.isemid.models.payload.requests.card.card161.ReqOutbreakDisinfectionMeasure;
import uz.uzinfocom.isemid.models.payload.requests.card.card161.ReqVaccination;
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
@Transactional
public class UpdateCard161Strategy implements UpdateCardStrategy {
    final CardRepository cardRepository;
    final CatalogService catalogService;

    private Catalog getCatalogByCode(CatalogType type, String code) throws ApiException {
        return catalogService.findByTypeAndCode(type, code).orElse(null);
    }


    @Transactional
    @Override
    public ApiResponse<?> updateCard(long id, ReqCard reqCard, User currentUser) {
        Card161 existingCard = (Card161) cardRepository.findById(id)
                .orElseThrow(() -> new ApiException(NOT_FOUND, "Card161 not found."));

        if (reqCard.getCardType() == null || !reqCard.getCardType().equals(CardType.CARD161)) {
            throw new ApiException(BAD_REQUEST, "Card type is not correct.");
        }
        ReqCard161 reqCard161 = (ReqCard161) reqCard;

        Optional.ofNullable(reqCard161.getCallerType()).ifPresent(existingCard::setCallerType);
        Optional.ofNullable(reqCard161.getIsResident()).ifPresent(existingCard::setIsResident);
        Optional.ofNullable(reqCard161.getResidentialTreatmentFacility()).ifPresent(existingCard::setResidentialTreatmentFacility);
        Optional.ofNullable(reqCard161.getDiseaseDetectedDate()).ifPresent(existingCard::setDiseaseDetectedDate);
        Optional.ofNullable(reqCard161.getRegion()).ifPresent(existingCard::setRegion);
        Optional.ofNullable(reqCard161.getDistrict()).ifPresent(existingCard::setDistrict);
        Optional.ofNullable(reqCard161.getPolyclinic()).ifPresent(existingCard::setPolyclinic);
        Optional.ofNullable(reqCard161.getInitialSymptoms()).ifPresent(existingCard::setInitialSymptoms);
        Optional.ofNullable(reqCard161.getDetectedCode()).ifPresent(code -> existingCard.setDetected(getCatalogByCode(CatalogType.definedDisease, code)));
        Optional.ofNullable(reqCard161.getEpidemiologicalExamDate()).ifPresent(existingCard::setEpidemiologicalExamDate);
        Optional.ofNullable(reqCard161.getObservationEndDate()).ifPresent(existingCard::setObservationEndDate);
        Optional.ofNullable(reqCard161.getFinalDiagnosisDate()).ifPresent(existingCard::setFinalDiagnosisDate);
        Optional.ofNullable(reqCard161.getDeliveryMethodCode()).ifPresent(code -> existingCard.setDeliveryMethod(getCatalogByCode(CatalogType.transportType, code)));
        Optional.ofNullable(reqCard161.getHomeStayExclusionReasonCode()).ifPresent(code -> existingCard.setHomeStayExclusionReason(getCatalogByCode(CatalogType.reasonOfLeavingHome, code)));
        Optional.ofNullable(reqCard161.getLateAdmissionReasonsCode()).ifPresent(code -> existingCard.setLateAdmissionReason(getCatalogByCode(CatalogType.reasonOfLateHospitalization, code)));
        Optional.ofNullable(reqCard161.getDiagnosisVerifiedCode()).ifPresent(code -> existingCard.setDiagnosisVerified(getCatalogByCode(CatalogType.diagnosisVerified, code)));
        Optional.ofNullable(reqCard161.getVaccinations()).ifPresent(reqVaccinations -> {
            if (reqVaccinations.isEmpty()) {
                existingCard.getVaccinations().clear();
            } else {
                Set<Long> requestIds = reqVaccinations.stream()
                        .map(ReqVaccination::getId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());

                existingCard.getVaccinations().removeIf(existingVaccination ->
                        existingVaccination.getId() != null && !requestIds.contains(existingVaccination.getId()));

                reqVaccinations.forEach(reqVaccination -> existingCard.getVaccinations().stream()
                        .filter(existingVaccination -> existingVaccination.getId() != null && existingVaccination.getId().equals(reqVaccination.getId()))
                        .findFirst()
                        .ifPresentOrElse(existingVaccination -> {
                            existingVaccination.setVaccinationName(reqVaccination.getVaccinationName());
                            existingVaccination.setVaccinationVerified(getCatalogByCode(CatalogType.informationAboutVaccination, reqVaccination.getVaccinationVerifiedCode()));
                            existingVaccination.setVaccinationDate(reqVaccination.getVaccinationDate());
                            existingVaccination.setSerialNumber(reqVaccination.getSerialNumber());
                            existingVaccination.setDoseVolume(reqVaccination.getDoseVolume());
                            existingVaccination.setScheduled(reqVaccination.getScheduled());
                        }, () -> {
                            Vaccination newVaccination = new Vaccination();
                            newVaccination.setCard161(existingCard);
                            newVaccination.setVaccinationName(reqVaccination.getVaccinationName());
                            newVaccination.setVaccinationVerified(getCatalogByCode(CatalogType.informationAboutVaccination, reqVaccination.getVaccinationVerifiedCode()));
                            newVaccination.setVaccinationDate(reqVaccination.getVaccinationDate());
                            newVaccination.setSerialNumber(reqVaccination.getSerialNumber());
                            newVaccination.setDoseVolume(reqVaccination.getDoseVolume());
                            existingCard.getVaccinations().add(newVaccination);
                        }));
            }
        });

        Optional.ofNullable(reqCard161.getEstimatedInfectionDateFrom()).ifPresent(existingCard::setEstimatedInfectionDateFrom);
        Optional.ofNullable(reqCard161.getEstimatedInfectionDateTo()).ifPresent(existingCard::setEstimatedInfectionDateTo);
        Optional.ofNullable(reqCard161.getRiskFactors()).ifPresent(reqRiskFactor -> {
            if (reqRiskFactor.isEmpty()) {
                existingCard.getRiskFactors().clear();
            } else {
                reqRiskFactor.forEach(reqRisk -> existingCard.getRiskFactors().stream()
                        .filter(existingRisk -> existingRisk.getId() != null && existingRisk.getId().equals(reqRisk.getId()))
                        .findFirst()
                        .ifPresentOrElse(existingRisk -> {
                            existingRisk.setRiskFactor(getCatalogByCode(CatalogType.riskFactor, reqRisk.getRiskFactorCode()));
                            existingRisk.setAddressLocation(reqRisk.getAddressLocation());
                            existingRisk.setSeasonTime(reqRisk.getSeasonTime());
                        }, () -> {
                            Card161RiskFactor riskFactor = Card161RiskFactor.builder()
                                    .riskFactor(getCatalogByCode(CatalogType.riskFactor, reqRisk.getRiskFactorCode()))
                                    .addressLocation(reqRisk.getAddressLocation())
                                    .card161(existingCard)
                                    .seasonTime(reqRisk.getSeasonTime())
                                    .build();
                            existingCard.getRiskFactors().add(riskFactor);
                        }));
            }

        });

        Optional.ofNullable(reqCard161.getPossibleInfectionSources()).ifPresent(reqInfectionSources -> {
            if (reqInfectionSources.isEmpty()) {
                existingCard.getPossibleInfectionSources().clear();
            }
            reqInfectionSources.forEach(reqInfectionSource -> existingCard.getPossibleInfectionSources().stream()
                    .filter(existingSource -> existingSource.getId() != null && existingSource.getId().equals(reqInfectionSource.getId()))
                    .findFirst()
                    .ifPresentOrElse(existingSource -> {
                        existingSource.setFullName(reqInfectionSource.getFullName());
                        existingSource.setContactInfoAndDonorResidence(reqInfectionSource.getContactInfoAndDonorResidence());
                        existingSource.setDiagnosisClinicalFormOrDonorStatus(reqInfectionSource.getDiagnosisClinicalFormOrDonorStatus());
                        existingSource.setTestResult(reqInfectionSource.getTestResult());
                    }, () -> {
                        InfectionSource infectionSource = InfectionSource.builder()
                                .card161(existingCard)
                                .fullName(reqInfectionSource.getFullName())
                                .diagnosisClinicalFormOrDonorStatus(reqInfectionSource.getDiagnosisClinicalFormOrDonorStatus())
                                .contactInfoAndDonorResidence(reqInfectionSource.getContactInfoAndDonorResidence())
                                .testResult(reqInfectionSource.getTestResult())
                                .build();
                        existingCard.getPossibleInfectionSources().add(infectionSource);
                    }));
        });

        Optional.ofNullable(reqCard161.getEnvironmentalSources()).ifPresent(reqSource -> {
            if (reqSource.isEmpty()) {
                existingCard.getEnvironmentalSources().clear();
            } else {
                reqSource.forEach(reqEnvSource -> existingCard.getEnvironmentalSources().stream()
                        .filter(environmentalSource -> environmentalSource.getId() != null && environmentalSource.getId().equals(reqEnvSource.getId()))
                        .findFirst()
                        .ifPresentOrElse(envSource -> {
                            envSource.setFoodAndWaterSourceTypes(reqEnvSource.getFoodAndWaterSourceTypes());
                            envSource.setCollectionLocationAndTime(reqEnvSource.getCollectionLocationAndTime());
                            envSource.setUsageLocationAndTime(reqEnvSource.getUsageLocationAndTime());
                            envSource.setStorageConditions(reqEnvSource.getStorageConditions());
                            envSource.setQualityFeedbackFromPatientAndOthers(reqEnvSource.getQualityFeedbackFromPatientAndOthers());
                        }, () -> {
                            EnvironmentalSource source = EnvironmentalSource.builder()
                                    .card161(existingCard)
                                    .foodAndWaterSourceTypes(reqEnvSource.getFoodAndWaterSourceTypes())
                                    .collectionLocationAndTime(reqEnvSource.getCollectionLocationAndTime())
                                    .usageLocationAndTime(reqEnvSource.getUsageLocationAndTime())
                                    .storageConditions(reqEnvSource.getStorageConditions())
                                    .qualityFeedbackFromPatientAndOthers(reqEnvSource.getQualityFeedbackFromPatientAndOthers())
                                    .build();
                            existingCard.getEnvironmentalSources().add(source);
                        }));
            }
        });

        Optional.ofNullable(reqCard161.getLivingConditionCode()).ifPresent(code -> existingCard.setLivingCondition(getCatalogByCode(CatalogType.livingCondition, code)));
        Optional.of(reqCard161.getNumberOfPeople()).ifPresent(existingCard::setNumberOfPeople);
        Optional.of(reqCard161.getNumberOfRooms()).ifPresent(existingCard::setNumberOfRooms);
        Optional.ofNullable(reqCard161.getArea()).ifPresent(existingCard::setArea);
        Optional.ofNullable(reqCard161.getWaterSupplyCode()).ifPresent(code -> existingCard.setWaterSupply(getCatalogByCode(CatalogType.waterSupply, code)));
        Optional.ofNullable(reqCard161.getLiquidWasteDisposalTypeCode()).ifPresent(code -> existingCard.setLiquidWasteDisposalType(getCatalogByCode(CatalogType.liquidWaste, code)));
        Optional.ofNullable(reqCard161.getSolidWasteDisposalTypeCode()).ifPresent(code -> existingCard.setSolidWasteDisposalType(getCatalogByCode(CatalogType.solidWaste, code)));
        Optional.ofNullable(reqCard161.getRoomConditionCode()).ifPresent(code -> existingCard.setRoomCondition(getCatalogByCode(CatalogType.roomCondition, code)));
        Optional.ofNullable(reqCard161.getYardConditionCode()).ifPresent(code -> existingCard.setYardCondition(getCatalogByCode(CatalogType.yardCondition, code)));
        Optional.ofNullable(reqCard161.getAreaConditionCode()).ifPresent(code -> existingCard.setAreaCondition(getCatalogByCode(CatalogType.areaContidion, code)));
        Optional.ofNullable(reqCard161.getHasLice()).ifPresent(existingCard::setHasLice);
        Optional.ofNullable(reqCard161.getHasOtherInsects()).ifPresent(existingCard::setHasOtherInsects);
        Optional.ofNullable(reqCard161.getHasRodents()).ifPresent(existingCard::setHasRodents);
        Optional.ofNullable(reqCard161.getImportantCausesOfDisease()).ifPresent(existingCard::setImportantCausesOfDisease);
        Optional.ofNullable(reqCard161.getVisitedObjectsCode()).ifPresent(code -> existingCard.setVisitedObjects(getCatalogByCode(CatalogType.visitetObj, code)));
        Optional.ofNullable(reqCard161.getDenselyPopulated()).ifPresent(existingCard::setDenselyPopulated);
        Optional.ofNullable(reqCard161.getIsolationStatus()).ifPresent(existingCard::setIsolationStatus);
        Optional.ofNullable(reqCard161.getWaterSupplyStatus()).ifPresent(existingCard::setWaterSupplyStatus);
        Optional.ofNullable(reqCard161.getSanitaryMaintenance()).ifPresent(existingCard::setSanitaryMaintenance);
        Optional.ofNullable(reqCard161.getFoodStorage()).ifPresent(existingCard::setFoodStorage);
        Optional.ofNullable(reqCard161.getFoodPreparation()).ifPresent(existingCard::setFoodPreparation);
        Optional.ofNullable(reqCard161.getDiseaseCausingFactors()).ifPresent(existingCard::setDiseaseCausingFactors);

        Optional.ofNullable(reqCard161.getEnvironmentalLabTests()).ifPresent(reqLabTest -> {
            if (reqLabTest.isEmpty()) {
                existingCard.getEnvironmentalLabTests().clear();
            } else {
                reqLabTest.forEach(reqLab -> existingCard.getEnvironmentalLabTests().stream()
                        .filter(existingLabTest -> existingLabTest.getId() != null && existingLabTest.getId().equals(reqLab.getId()))
                        .findFirst()
                        .ifPresentOrElse(existingLabTest -> {
                            existingLabTest.setExaminationDate(reqLab.getExaminationDate());
                            existingLabTest.setObjectArthropodsAnimals(reqLab.getObjectArthropodsAnimals());
                            existingLabTest.setMaterial(reqLab.getMaterial());
                            existingLabTest.setSampleQuantity(reqLab.getSampleQuantity());
                            existingLabTest.setTestTypeAndResult(reqLab.getTestTypeAndResult());
                        }, () -> {

                            EnvironmentalLabTest labTest = EnvironmentalLabTest.builder()
                                    .examinationDate(reqLab.getExaminationDate())
                                    .objectArthropodsAnimals(reqLab.getObjectArthropodsAnimals())
                                    .material(reqLab.getMaterial())
                                    .sampleQuantity(reqLab.getSampleQuantity())
                                    .testTypeAndResult(reqLab.getTestTypeAndResult())
                                    .card161(existingCard)
                                    .build();
                            existingCard.getEnvironmentalLabTests().add(labTest);
                        }));
            }
        });

        Optional.ofNullable(reqCard161.getContactPersonDetails()).ifPresent(reqContactPeople -> {
            if (reqContactPeople.isEmpty()) {
                existingCard.getContactPersonDetails().clear();
            } else {
                reqContactPeople.forEach(reqContactPerson -> existingCard.getContactPersonDetails().stream()
                        .filter(existingContactPerson -> existingContactPerson.getId() != null && existingContactPerson.getId().equals(reqContactPerson.getId()))
                        .findFirst()
                        .ifPresentOrElse(existingContactPerson -> {
                            existingContactPerson.setFullName(reqContactPerson.getFullName());
                            existingContactPerson.setAge(reqContactPerson.getAge());
                            existingContactPerson.setAddress(reqContactPerson.getAddress());
                            existingContactPerson.setJobTypeAndLocation(reqContactPerson.getJobTypeAndLocation());
                            existingContactPerson.setImmunizationStatus(reqContactPerson.getImmunizationStatus());
                            existingContactPerson.setRestrictionMeasures(reqContactPerson.getRestrictionMeasures());
                        }, () -> {

                            ContactPerson contactPerson = ContactPerson.builder()
                                    .card161(existingCard)
                                    .fullName(reqContactPerson.getFullName())
                                    .age(reqContactPerson.getAge())
                                    .address(reqContactPerson.getAddress())
                                    .jobTypeAndLocation(reqContactPerson.getJobTypeAndLocation())
                                    .immunizationStatus(reqContactPerson.getImmunizationStatus())
                                    .restrictionMeasures(reqContactPerson.getRestrictionMeasures())
                                    .build();
                            existingCard.getContactPersonDetails().add(contactPerson);
                        }));
            }
        });

        Optional.ofNullable(reqCard161.getScreenedGroups()).ifPresent(reqGroup -> {
            if (reqGroup.isEmpty()) {
                existingCard.getScreenedGroups().clear();
            } else {
                reqGroup.forEach(reqScreenedGroup -> existingCard.getScreenedGroups().stream()
                        .filter(existingGroup -> existingGroup.getId() != null && existingGroup.getId().equals(reqScreenedGroup.getId()))
                        .findFirst()
                        .ifPresentOrElse(existingGroup -> {
                            existingGroup.setContactCount(reqScreenedGroup.getContactCount());
                            existingGroup.setTeamName(reqScreenedGroup.getTeamName());
                            existingGroup.setProphylacticAddress(reqScreenedGroup.getProphylacticAddress());
                            existingGroup.setRequiredProphylacticSubstance(reqScreenedGroup.getRequiredProphylacticSubstance());
                            existingGroup.setTreatedWithProphylacticSubstance(reqScreenedGroup.getTreatedWithProphylacticSubstance());
                            existingGroup.setLaboratoryTestConducted(reqScreenedGroup.getLaboratoryTestConducted());
                        }, () -> {

                            ScreenedGroup screenedGroup = ScreenedGroup.builder()
                                    .card161(existingCard)
                                    .teamName(reqScreenedGroup.getTeamName())
                                    .prophylacticAddress(reqScreenedGroup.getProphylacticAddress())
                                    .contactCount(reqScreenedGroup.getContactCount())
                                    .requiredProphylacticSubstance(reqScreenedGroup.getRequiredProphylacticSubstance())
                                    .treatedWithProphylacticSubstance(reqScreenedGroup.getTreatedWithProphylacticSubstance())
                                    .laboratoryTestConducted(reqScreenedGroup.getLaboratoryTestConducted())
                                    .build();
                            existingCard.getScreenedGroups().add(screenedGroup);
                        }));
            }
        });

        Optional.ofNullable(reqCard161.getHomePreventiveMeasures()).ifPresent(rqMeasure -> {
            if (rqMeasure.isEmpty()) {
                existingCard.getHomePreventiveMeasures().clear();
            } else {
                Set<Long> requestIds = rqMeasure.stream()
                        .map(ReqHomePreventiveMeasure::getId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());

                existingCard.getHomePreventiveMeasures().removeIf(existingPrMeasure ->
                        existingPrMeasure.getId() != null && !requestIds.contains(existingPrMeasure.getId()));

                rqMeasure.forEach(reqHomePreventiveMeasure -> existingCard.getHomePreventiveMeasures().stream()
                        .filter(existingMeasure -> existingMeasure.getId() != null && existingMeasure.getId().equals(reqHomePreventiveMeasure.getId()))
                        .findFirst()
                        .ifPresentOrElse(existingMeasure -> {
                            existingMeasure.setNotifiedPerson(reqHomePreventiveMeasure.getNotifiedPerson());
                            existingMeasure.setNextDiseaseTime(reqHomePreventiveMeasure.getNextDiseaseTime());
                            existingMeasure.setDrugName(reqHomePreventiveMeasure.getDrugName());
                            existingMeasure.setDose(reqHomePreventiveMeasure.getDose());
                            existingMeasure.setSeries(reqHomePreventiveMeasure.getSeries());
                            existingMeasure.setResultTime(reqHomePreventiveMeasure.getResultTime());
                            existingMeasure.setReceivedDate(reqHomePreventiveMeasure.getReceivedDate());
                            existingMeasure.setLisResult(reqHomePreventiveMeasure.getLisResult());
                            existingMeasure.setObservationResult(reqHomePreventiveMeasure.getObservationResult());
                        }, () -> {

                            HomePreventiveMeasure homePreventiveMeasure = HomePreventiveMeasure.builder()
                                    .card161(existingCard)
                                    .notifiedPerson(reqHomePreventiveMeasure.getNotifiedPerson())
                                    .nextDiseaseTime(reqHomePreventiveMeasure.getNextDiseaseTime())
                                    .drugName(reqHomePreventiveMeasure.getDrugName())
                                    .dose(reqHomePreventiveMeasure.getDose())
                                    .series(reqHomePreventiveMeasure.getSeries())
                                    .resultTime(reqHomePreventiveMeasure.getResultTime())
                                    .receivedDate(reqHomePreventiveMeasure.getReceivedDate())
                                    .lisResult(reqHomePreventiveMeasure.getLisResult())
                                    .observationResult(reqHomePreventiveMeasure.getObservationResult())
                                    .build();
                            existingCard.getHomePreventiveMeasures().add(homePreventiveMeasure);
                        }));
            }
        });

        Optional.ofNullable(reqCard161.getOutbreakDisinfectionMeasures()).ifPresent(rqMeasure -> {
            if (rqMeasure.isEmpty()) {
                existingCard.getOutbreakDisinfectionMeasures().clear();
            } else {

                Set<Long> requestIds = rqMeasure.stream()
                        .map(ReqOutbreakDisinfectionMeasure::getId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());

                existingCard.getOutbreakDisinfectionMeasures().removeIf(existingPrMeasure ->
                        existingPrMeasure.getId() != null && !requestIds.contains(existingPrMeasure.getId()));

                rqMeasure.forEach(reqMeasure -> existingCard.getOutbreakDisinfectionMeasures().stream()
                        .filter(existingMeasure -> existingMeasure.getId() != null && existingMeasure.getId().equals(reqMeasure.getId()))
                        .findFirst()
                        .ifPresentOrElse(existingMeasure -> {
                            existingMeasure.setPreventiveMeasures(getCatalogByCode(CatalogType.preventMeasure, reqMeasure.getPreventiveMeasuresCode()));
                            existingMeasure.setDrugType(reqMeasure.getDrugType());
                            existingMeasure.setConductedAt(reqMeasure.getConductedAt());
                            existingMeasure.setConductedLocation(getCatalogByCode(CatalogType.counductedLoca, reqMeasure.getConductedLocationCode()));
                            existingMeasure.setExecutors(reqMeasure.getExecutors());
                            existingMeasure.setExecutionMonitoringResult(reqMeasure.getExecutionMonitoringResult());
                        }, () -> {

                            OutbreakDisinfectionMeasure outbreakDisinfectionMeasure = OutbreakDisinfectionMeasure.builder()
                                    .card161(existingCard)
                                    .preventiveMeasures(getCatalogByCode(CatalogType.preventMeasure, reqMeasure.getPreventiveMeasuresCode()))
                                    .drugType(reqMeasure.getDrugType())
                                    .conductedAt(reqMeasure.getConductedAt())
                                    .conductedLocation(getCatalogByCode(CatalogType.counductedLoca, reqMeasure.getConductedLocationCode()))
                                    .executors(reqMeasure.getExecutors())
                                    .executionMonitoringResult(reqMeasure.getExecutionMonitoringResult())
                                    .build();
                            existingCard.getOutbreakDisinfectionMeasures().add(outbreakDisinfectionMeasure);
                        }));
            }
        });

        Optional.ofNullable(reqCard161.getHospitalName()).ifPresent(existingCard::setHospitalName);
        Optional.ofNullable(reqCard161.getInfectionLocationCode()).ifPresent(code -> existingCard.setInfectionLocation(getCatalogByCode(CatalogType.infectedLocation, code)));
        Optional.ofNullable(reqCard161.getProbableInfectionLocationCode()).ifPresent(code-> existingCard.setProbableInfectionLocation(getCatalogByCode(CatalogType.probableInfectionLoc,code)));
        Optional.ofNullable(reqCard161.getIsInfectionSourceMissing()).ifPresent(existingCard::setIsInfectionSourceMissing);
        Optional.ofNullable(reqCard161.getInfectionSourceDetail()).ifPresent(reqInfectionD -> {
            if (existingCard.getInfectionSourceDetail() != null) {
                InfectionSourceDetail existingDetail = existingCard.getInfectionSourceDetail();
                existingDetail.setInfectionSourceNotFound(getCatalogByCode(CatalogType.notFoundInfection, reqInfectionD.getInfectionSourceNotFoundCode()));
                existingDetail.setPersonFullName(reqInfectionD.getPersonFullName());
                existingDetail.setAnimalType(getCatalogByCode(CatalogType.animalType, reqInfectionD.getAnimalTypeCode()));
                existingDetail.setInfectionSourceDiseasePeriod(getCatalogByCode(CatalogType.infectionPeriod, reqInfectionD.getInfectionSourceDiseasePeriodCode()));
            } else {
                InfectionSourceDetail newDetail = InfectionSourceDetail.builder()
                        .infectionSourceNotFound(getCatalogByCode(CatalogType.notFoundInfection, reqInfectionD.getInfectionSourceNotFoundCode()))
                        .personFullName(reqInfectionD.getPersonFullName())
                        .infectionSourceDiseasePeriod(getCatalogByCode(CatalogType.infectionPeriod, reqInfectionD.getInfectionSourceDiseasePeriodCode()))
                        .animalType(getCatalogByCode(CatalogType.animalType, reqInfectionD.getAnimalTypeCode()))
                        .build();
                existingCard.setInfectionSourceDetail(newDetail);
            }
        });

        Optional.ofNullable(reqCard161.getMainProbableInfectionFactorCode()).ifPresent(code -> existingCard.setMainProbableInfectionFactor(getCatalogByCode(CatalogType.mainFactor, code)));
        Optional.ofNullable(reqCard161.getInfectionCausingConditionsCode()).ifPresent(codes -> {
            List<Catalog> conditions = codes.stream()
                    .map(code -> getCatalogByCode(CatalogType.infeCondition, code))
                    .filter(Objects::nonNull)
                    .toList();
            existingCard.getInfectionCausingConditions().clear();
            existingCard.getInfectionCausingConditions().addAll(conditions);
        });

        Optional.ofNullable(reqCard161.getOutbreakInfectionCode()).ifPresent(code -> existingCard.setOutbreakInfection(getCatalogByCode(CatalogType.outbreakInfection, code)));
        Optional.ofNullable(reqCard161.getCaseStatusCode()).ifPresent(code -> existingCard.setCaseStatus(getCatalogByCode(CatalogType.caseStatus, code)));
        Optional.ofNullable(reqCard161.getEpidemiologist()).ifPresent(existingCard::setEpidemiologist);
        Optional.ofNullable(reqCard161.getEpidemiologistAssistant()).ifPresent(existingCard::setEpidemiologistAssistant);
        Card161 updatedCard = cardRepository.save(existingCard);
        return new ApiResponse<>(true, "Card161 updated successfully.", updatedCard);
    }
}