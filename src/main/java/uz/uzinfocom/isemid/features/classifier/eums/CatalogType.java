package uz.uzinfocom.isemid.features.classifier.eums;

public enum CatalogType {
    //General
    gender, state, city, neighborhood, country, nationality, identifierType, catalogType, maritalStatus, addressType,
    populationType, residentialStatus, position, formType, diseasePlace, category, profession, affiliationType,

    //Card 161
    detected, definedDisease, homeStayExclusionReason,diagnosisVerified,
    livingCondition, waterSupply,
    liquidWaste, solidWaste, roomCondition, yardCondition,
    areaContidion, visitetObj, riskFactor, preventMeasure, counductedLoca, infectedLocation,probableInfectionLoc,
    notFoundInfection, infectionPeriod, animalType, mainFactor, outbreakInfection, caseStatus,
    infeCondition, level,

    //Card175
    wherePatientCome,
    hospitalDischargeStatus,transportType,reasonOfLeavingHome,
    reasonOfLateHospitalization,
    diagnosisConfirmed,
    informationAboutVaccination,initialDiagnosis,patientIdentified,
    //immediatePreventionAndAntirabicAid,
    informationAboutImmediatePreventionAndAntirabicAid,
    partOfInjury,relevanceOfDiseaseToProfession,severityOfIllness,diseaseSpreaderType,
    ownerOfDiseaseSpreader,observationResultOfAnimals,checkingDiagnosis,diseaseTransmissionCondition,
    pathogenMainFactor, vaccinationVerified, takenMeasuresFromResidence,

    //card205
    diagnosisOfInjury,conditionOfAnimal,animalConservation,positionOfBittenVictim,
    animalCategory,dogOwnerCompliance,

    //Card Tube
    reasonStayHome, nutritionType, workCondition, incomingLevel,
    harmfulHabit, contactType, kinship, housingCondition, hygieneAssessment, heating, sewerage, renovationNeeded,
    habitability, sputumDisposal, getsDisinfectant, visitInterval, epidemicPlanType, recoveryPlan,

    //card174
    affectedAnimalType,diseaseFactors,animalDisposalMethod,deratization,quarantineType,eliminationMethod,
    disinfectionTransmissionFactors,processingMethod,

    //act154
    shift,

    diseaseCause, //act153
    organizationType, act153DetailObj
}
