package uz.uzinfocom.app.modules.card.application.query.dto.detail;

import uz.uzinfocom.app.modules.card.application.query.dto.detail.card205.InformationAboutAnimaBittenPeopleResponse;
import uz.uzinfocom.app.modules.card.application.query.dto.detail.card205.InformationOtherBittenAnimalsResponse;
import uz.uzinfocom.app.modules.card.application.query.dto.detail.card205.InformationOtherBittenPeopleResponse;
import uz.uzinfocom.app.modules.card.domain.enums.CardStatus;
import uz.uzinfocom.app.modules.card.domain.enums.CardType;

import java.time.LocalDate;
import java.util.List;

public record Card205DetailResponse(
        Long id,
        CardType type,
        CardStatus status,
        Long formId,
        Long assignedById,
        String supervisorComment,
        String attachedUserComment,
        LocalDate completedDate,

        String mkb10Code,
        String mkb10Name,
        LocalDate epidemiologicalObservationDate,
        LocalDate veterinaryEmergencyInformationDate,
        String addressOfBiteOccurrence,
        LocalDate dateOfBiteOccurrence,
        String nameOfTreatmentPreventiveInstitution,
        LocalDate dateOfTreatmentPreventiveInstitution,
        List<InformationOtherBittenPeopleResponse> infoBittenPeople,
        String animalType,
        String whereAnimalComesFrom,
        String whenAnimalAppeared,
        String conditionOfAnimalCode,
        Integer ageOfAnimal,
        String breedOfAnimal,
        String colourOfAnimal,
        String individualSignsOfAnimal,
        Integer certificateNumberOfFirstVetResults,
        LocalDate issueDateOfFirstCertificate,
        String animalConservationCode,
        String positionOfBittenVictimCode,
        Integer certificateNumberOfSecondVetResults,
        LocalDate issueDateOfSecondaryCertificate,
        String dateTimeOfFeatherTaken,
        String petRegisteredVetDepartment,
        String dogOwnerComplianceCode,
        List<InformationOtherBittenAnimalsResponse> infoOtherBittenAnimal,
        List<InformationAboutAnimaBittenPeopleResponse> infoAbtAnimalBittenPeople,
        String additionalInformation,
        String fullNameofAnimalOwner
) implements CardDetailResponse {
}
