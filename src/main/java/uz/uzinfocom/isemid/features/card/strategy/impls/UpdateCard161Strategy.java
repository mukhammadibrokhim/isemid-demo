package uz.uzinfocom.isemid.features.card.strategy.impls;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.card.dtos.requests.CardRequest;
import uz.uzinfocom.isemid.features.card.dtos.requests.card161.*;
import uz.uzinfocom.isemid.features.card.mappers.CardMapper;
import uz.uzinfocom.isemid.features.card.models.Card;
import uz.uzinfocom.isemid.features.card.models.card161.*;
import uz.uzinfocom.isemid.features.card.reposiotries.CardRepository;
import uz.uzinfocom.isemid.features.card.strategy.UpdateCardStrategy;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.organization.services.OrganizationService;
import uz.uzinfocom.isemid.features.user.models.User;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static uz.uzinfocom.isemid.features.card.utils.CardUtil.validateCard;

@Service
@RequiredArgsConstructor
@Transactional
public class UpdateCard161Strategy implements UpdateCardStrategy {

    private final CardRepository cardRepository;
    private final OrganizationService organizationService;
    private final CardMapper cardMapper;

    @Transactional
    @Override
    public ApiResponse<?> updateCard(long id, CardRequest reqCard, User currentUser) {
        Card card = validateCard(id, reqCard, cardRepository);

        Card161 existingCard = (Card161) card;
        Card161Request reqCard161 = (Card161Request) reqCard;

        Optional.ofNullable(reqCard161.getCallerType()).ifPresent(existingCard::setCallerType);
        Optional.ofNullable(reqCard161.getIsResident()).ifPresent(existingCard::setIsResident);
        Optional.ofNullable(reqCard161.getResidentialTreatmentFacility()).ifPresent(existingCard::setResidentialTreatmentFacility);
        Optional.ofNullable(reqCard161.getDiseaseDetectedDate()).ifPresent(existingCard::setDiseaseDetectedDate);

        Optional.ofNullable(reqCard161.getRegionCode()).ifPresent(existingCard::setRegionCode);
        Optional.ofNullable(reqCard161.getDistrictCode()).ifPresent(existingCard::setDistrictCode);
        Optional.ofNullable(reqCard161.getPolyclinicId()).ifPresent(pId -> {
            Organization organization = organizationService.findById(pId).orElse(null);
            if (organization != null) {
                existingCard.setPolyclinicId(pId);
                existingCard.setPolyclinic(organization);
            } else {
                existingCard.setPolyclinicId(null);
                existingCard.setPolyclinic(null);
            }
        });

        Optional.ofNullable(reqCard161.getInitialSymptoms()).ifPresent(existingCard::setInitialSymptoms);
        Optional.ofNullable(reqCard161.getDetectedCode()).ifPresent(existingCard::setDetectedCode);
        Optional.ofNullable(reqCard161.getEpidemiologicalExamDate()).ifPresent(existingCard::setEpidemiologicalExamDate);
        Optional.ofNullable(reqCard161.getObservationEndDate()).ifPresent(existingCard::setObservationEndDate);
        Optional.ofNullable(reqCard161.getFinalDiagnosisDate()).ifPresent(existingCard::setFinalDiagnosisDate);
        Optional.ofNullable(reqCard161.getDeliveryMethodCode()).ifPresent(existingCard::setDeliveryMethodCode);
        Optional.ofNullable(reqCard161.getHomeStayExclusionReasonCode()).ifPresent(existingCard::setHomeStayExclusionReasonCode);
        Optional.ofNullable(reqCard161.getLateAdmissionReasonsCode()).ifPresent(existingCard::setLateAdmissionReasonCode);
        Optional.ofNullable(reqCard161.getDiagnosisVerifiedCode()).ifPresent(existingCard::setDiagnosisVerifiedCode);
        Optional.ofNullable(reqCard161.getVaccinations()).ifPresent(reqVaccinations -> {
            if (reqVaccinations.isEmpty()) {
                existingCard.getVaccinations().clear();
            } else {
                Set<Long> requestIds = reqVaccinations.stream()
                        .map(VaccinationRequest::getId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());

                existingCard.getVaccinations().removeIf(existingVaccination ->
                        existingVaccination.getId() != null && !requestIds.contains(existingVaccination.getId()));

                reqVaccinations.forEach(reqVaccination -> existingCard.getVaccinations().stream()
                        .filter(existingVaccination -> existingVaccination.getId() != null && existingVaccination.getId().equals(reqVaccination.getId()))
                        .findFirst()
                        .ifPresentOrElse(existingVaccination -> {
                            existingVaccination.setVaccinationName(reqVaccination.getVaccinationName());
                            existingVaccination.setVaccinationVerifiedCode(reqVaccination.getVaccinationVerifiedCode());
                            existingVaccination.setVaccinationDate(reqVaccination.getVaccinationDate());
                            existingVaccination.setSerialNumber(reqVaccination.getSerialNumber());
                            existingVaccination.setDoseVolume(reqVaccination.getDoseVolume());
                            existingVaccination.setScheduled(reqVaccination.getScheduled());
                        }, () -> {
                            Vaccination newVaccination = new Vaccination();
                            newVaccination.setCard161(existingCard);
                            newVaccination.setVaccinationName(reqVaccination.getVaccinationName());
                            newVaccination.setVaccinationVerifiedCode(reqVaccination.getVaccinationVerifiedCode());
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

                Set<Long> requestIds = reqRiskFactor.stream()
                        .map(Card161RiskFactorRequest::getId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());

                existingCard.getRiskFactors().removeIf(exsistingReq ->
                        exsistingReq.getId() != null && !requestIds.contains(exsistingReq.getId()));

                reqRiskFactor.forEach(reqRisk -> existingCard.getRiskFactors().stream()
                        .filter(existingRisk -> existingRisk.getId() != null && existingRisk.getId().equals(reqRisk.getId()))
                        .findFirst()
                        .ifPresentOrElse(existingRisk -> {
                            existingRisk.setRiskFactorCode(reqRisk.getRiskFactorCode());
                            existingRisk.setAddressLocation(reqRisk.getAddressLocation());
                            existingRisk.setSeasonTime(reqRisk.getSeasonTime());
                        }, () -> {
                            Card161RiskFactor riskFactor = Card161RiskFactor.builder()
                                    .riskFactorCode(reqRisk.getRiskFactorCode())
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

            Set<Long> requestIds = reqInfectionSources.stream()
                    .map(InfectionSourceRequest::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            existingCard.getPossibleInfectionSources().removeIf(exsistingReq ->
                    exsistingReq.getId() != null && !requestIds.contains(exsistingReq.getId()));


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

                Set<Long> requestIds = reqSource.stream()
                        .map(EnvironmentalSourceRequest::getId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());

                existingCard.getEnvironmentalSources().removeIf(exsistingReq ->
                        exsistingReq.getId() != null && !requestIds.contains(exsistingReq.getId()));


                reqSource.forEach(reqEnvSource -> existingCard.getEnvironmentalSources().stream()
                        .filter(environmentalSource -> environmentalSource.getId() != null && environmentalSource.getId().equals(reqEnvSource.getId()))
                        .findFirst()
                        .ifPresentOrElse(envSource -> {
                            envSource.setFoodAndWaterSourceTypes(reqEnvSource.getFoodAndWaterSourceTypes());
                            envSource.setCollectionLocation(reqEnvSource.getCollectionLocation());
                            envSource.setCollectionTime(reqEnvSource.getCollectionTime());


                            envSource.setUsageLocation(reqEnvSource.getUsageLocation());
                            envSource.setUsageTime(reqEnvSource.getUsageTime());
                            envSource.setStorageConditions(reqEnvSource.getStorageConditions());
                            envSource.setQualityFeedbackFromPatientAndOthers(reqEnvSource.getQualityFeedbackFromPatientAndOthers());
                        }, () -> {
                            EnvironmentalSource source = EnvironmentalSource.builder()
                                    .card161(existingCard)
                                    .foodAndWaterSourceTypes(reqEnvSource.getFoodAndWaterSourceTypes())
                                    .collectionLocation(reqEnvSource.getCollectionLocation())
                                    .collectionTime(reqEnvSource.getCollectionTime())
                                    .usageLocation(reqEnvSource.getUsageLocation())
                                    .usageTime(reqEnvSource.getUsageTime())
                                    .storageConditions(reqEnvSource.getStorageConditions())
                                    .qualityFeedbackFromPatientAndOthers(reqEnvSource.getQualityFeedbackFromPatientAndOthers())
                                    .build();
                            existingCard.getEnvironmentalSources().add(source);
                        }));
            }
        });

        Optional.ofNullable(reqCard161.getLivingConditionCode()).ifPresent(existingCard::setLivingConditionCode);
        Optional.ofNullable(reqCard161.getNumberOfPeople()).ifPresent(existingCard::setNumberOfPeople);
        Optional.ofNullable(reqCard161.getNumberOfRooms()).ifPresent(existingCard::setNumberOfRooms);
        Optional.ofNullable(reqCard161.getArea()).ifPresent(existingCard::setArea);
        Optional.ofNullable(reqCard161.getWaterSupplyCode()).ifPresent(existingCard::setWaterSupplyCode);
        Optional.ofNullable(reqCard161.getLiquidWasteDisposalTypeCode()).ifPresent(existingCard::setLiquidWasteDisposalTypeCode);
        Optional.ofNullable(reqCard161.getSolidWasteDisposalTypeCode()).ifPresent(existingCard::setSolidWasteDisposalTypeCode);
        Optional.ofNullable(reqCard161.getRoomConditionCode()).ifPresent(existingCard::setRoomConditionCode);
        Optional.ofNullable(reqCard161.getYardConditionCode()).ifPresent(existingCard::setYardConditionCode);
        Optional.ofNullable(reqCard161.getAreaConditionCode()).ifPresent(existingCard::setAreaConditionCode);
        Optional.ofNullable(reqCard161.getHasLice()).ifPresent(existingCard::setHasLice);
        Optional.ofNullable(reqCard161.getHasOtherInsects()).ifPresent(existingCard::setHasOtherInsects);
        Optional.ofNullable(reqCard161.getHasRodents()).ifPresent(existingCard::setHasRodents);
        Optional.ofNullable(reqCard161.getImportantCausesOfDiseaseCode()).ifPresent(existingCard::setImportantCausesOfDiseaseCode);
        Optional.ofNullable(reqCard161.getVisitedObjectsCode()).ifPresent(existingCard::setVisitedObjectsCode);
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

                Set<Long> requestIds = reqLabTest.stream()
                        .map(EnvironmentalLabTestRequest::getId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());

                existingCard.getEnvironmentalLabTests().removeIf(exsistingReq ->
                        exsistingReq.getId() != null && !requestIds.contains(exsistingReq.getId()));


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

                Set<Long> requestIds = reqContactPeople.stream()
                        .map(ContactPersonRequest::getId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());

                existingCard.getContactPersonDetails().removeIf(exsistingReq ->
                        exsistingReq.getId() != null && !requestIds.contains(exsistingReq.getId()));


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

                Set<Long> requestIds = reqGroup.stream()
                        .map(ScreenedGroupRequest::getId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());

                existingCard.getScreenedGroups().removeIf(exsistingReq ->
                        exsistingReq.getId() != null && !requestIds.contains(exsistingReq.getId()));


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
                        .map(HomePreventiveMeasureRequest::getId)
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
                        .map(OutbreakDisinfectionMeasureRequest::getId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());

                existingCard.getOutbreakDisinfectionMeasures().removeIf(existingPrMeasure ->
                        existingPrMeasure.getId() != null && !requestIds.contains(existingPrMeasure.getId()));

                rqMeasure.forEach(reqMeasure -> existingCard.getOutbreakDisinfectionMeasures().stream()
                        .filter(existingMeasure -> existingMeasure.getId() != null && existingMeasure.getId().equals(reqMeasure.getId()))
                        .findFirst()
                        .ifPresentOrElse(existingMeasure -> {
                            existingMeasure.setPreventiveMeasuresCode(reqMeasure.getPreventiveMeasuresCode());
                            existingMeasure.setDrugType(reqMeasure.getDrugType());
                            existingMeasure.setConductedAt(reqMeasure.getConductedAt());
                            existingMeasure.setConductedLocationCode(reqMeasure.getConductedLocationCode());
                            existingMeasure.setExecutors(reqMeasure.getExecutors());
                            existingMeasure.setExecutionMonitoringResult(reqMeasure.getExecutionMonitoringResult());
                        }, () -> {

                            OutbreakDisinfectionMeasure outbreakDisinfectionMeasure = OutbreakDisinfectionMeasure.builder()
                                    .card161(existingCard)
                                    .preventiveMeasuresCode(reqMeasure.getPreventiveMeasuresCode())
                                    .drugType(reqMeasure.getDrugType())
                                    .conductedAt(reqMeasure.getConductedAt())
                                    .conductedLocationCode(reqMeasure.getConductedLocationCode())
                                    .executors(reqMeasure.getExecutors())
                                    .executionMonitoringResult(reqMeasure.getExecutionMonitoringResult())
                                    .build();
                            existingCard.getOutbreakDisinfectionMeasures().add(outbreakDisinfectionMeasure);
                        }));
            }
        });

        Optional.ofNullable(reqCard161.getHospitalName()).ifPresent(existingCard::setHospitalName);
        Optional.ofNullable(reqCard161.getInfectionLocationCode()).ifPresent(existingCard::setInfectionLocationCode);
        Optional.ofNullable(reqCard161.getProbableInfectionLocationCode()).ifPresent(existingCard::setProbableInfectionLocationCode);
        Optional.ofNullable(reqCard161.getIsInfectionSourceMissing()).ifPresent(existingCard::setIsInfectionSourceMissing);
        Optional.ofNullable(reqCard161.getInfectionSourceDetail()).ifPresent(reqInfectionD -> {
            if (existingCard.getInfectionSourceDetail() != null) {
                InfectionSourceDetail existingDetail = existingCard.getInfectionSourceDetail();
                existingDetail.setInfectionSourceNotFoundCode(reqInfectionD.getInfectionSourceNotFoundCode());
                existingDetail.setPersonFullName(reqInfectionD.getPersonFullName());
                existingDetail.setAnimalTypeCode(reqInfectionD.getAnimalTypeCode());
                existingDetail.setInfectionSourceDiseasePeriodCode(reqInfectionD.getInfectionSourceDiseasePeriodCode());
            } else {
                InfectionSourceDetail newDetail = InfectionSourceDetail.builder()
                        .card161(existingCard)
                        .infectionSourceNotFoundCode(reqInfectionD.getInfectionSourceNotFoundCode())
                        .personFullName(reqInfectionD.getPersonFullName())
                        .infectionSourceDiseasePeriodCode(reqInfectionD.getInfectionSourceDiseasePeriodCode())
                        .animalTypeCode(reqInfectionD.getAnimalTypeCode())
                        .build();
                existingCard.setInfectionSourceDetail(newDetail);
            }
        });

        Optional.ofNullable(reqCard161.getMainProbableInfectionFactorCode()).ifPresent(existingCard::setMainProbableInfectionFactorCode);

//        Optional.ofNullable(reqCard161.getInfectionCausingConditionsCode()).ifPresent(codes -> {
//            List<Catalog> conditions = codes.stream()
//                    .map(code -> getCatalogByCode(CatalogType.infeCondition, code))
//                    .filter(Objects::nonNull)
//                    .toList();
//            existingCard.getInfectionCausingConditions().clear();
//            existingCard.getInfectionCausingConditions().addAll(conditions);
//        });

        Optional.ofNullable(reqCard161.getOutbreakInfectionCode()).ifPresent(existingCard::setOutbreakInfectionCode);
        Optional.ofNullable(reqCard161.getCaseStatusCode()).ifPresent(existingCard::setCaseStatusCode);
        Optional.ofNullable(reqCard161.getEpidemiologist()).ifPresent(existingCard::setEpidemiologist);
        Optional.ofNullable(reqCard161.getEpidemiologistAssistant()).ifPresent(existingCard::setEpidemiologistAssistant);
        Optional.ofNullable(reqCard161.getComment()).ifPresent(existingCard::setComment);
        return new ApiResponse<>(true, MessageUtil.UPDATED_SUCCESS, cardMapper.toCardResponse(cardRepository.save(existingCard)));
    }
}