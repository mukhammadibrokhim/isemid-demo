package uz.uzinfocom.app.modules.card.web.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.modules.card.domain.enums.CardType;
import uz.uzinfocom.app.modules.card.web.dto.request.card205.InformationAboutAnimaBittenPeopleRequest;
import uz.uzinfocom.app.modules.card.web.dto.request.card205.InformationOtherBittenAnimalsRequest;
import uz.uzinfocom.app.modules.card.web.dto.request.card205.InformationOtherBittenPeopleRequest;

import java.time.LocalDate;
import java.util.List;

public record Card205Request(
        @Size(max = 64) String mkb10Code,
        @Size(max = 500) String mkb10Name,
        LocalDate epidemiologicalObservationDate,
        LocalDate veterinaryEmergencyInformationDate,
        @Size(max = 500) String addressOfBiteOccurrence,
        LocalDate dateOfBiteOccurrence,
        @Size(max = 255) String nameOfTreatmentPreventiveInstitution,
        LocalDate dateOfTreatmentPreventiveInstitution,
        @Valid List<InformationOtherBittenPeopleRequest> infoBittenPeople,
        @Size(max = 255) String animalType,
        @Size(max = 500) String whereAnimalComesFrom,
        @Size(max = 255) String whenAnimalAppeared,
        @Size(max = 64) String conditionOfAnimalCode,
        Integer ageOfAnimal,
        @Size(max = 255) String breedOfAnimal,
        @Size(max = 255) String colourOfAnimal,
        @Size(max = 500) String individualSignsOfAnimal,
        Integer certificateNumberOfFirstVetResults,
        LocalDate issueDateOfFirstCertificate,
        @Size(max = 64) String animalConservationCode,
        @Size(max = 64) String positionOfBittenVictimCode,
        Integer certificateNumberOfSecondVetResults,
        LocalDate issueDateOfSecondaryCertificate,
        @Size(max = 255) String dateTimeOfFeatherTaken,
        @Size(max = 255) String petRegisteredVetDepartment,
        @Size(max = 64) String dogOwnerComplianceCode,
        @Valid List<InformationOtherBittenAnimalsRequest> infoOtherBittenAnimal,
        @Valid List<InformationAboutAnimaBittenPeopleRequest> infoAbtAnimalBittenPeople,
        @Size(max = 1000) String additionalInformation,
        @Size(max = 255) String fullNameofAnimalOwner
) implements CardRequest {

    public Card205Request {
        infoBittenPeople = immutableCopy(infoBittenPeople);
        infoOtherBittenAnimal = immutableCopy(infoOtherBittenAnimal);
        infoAbtAnimalBittenPeople = immutableCopy(infoAbtAnimalBittenPeople);
    }

    @Override
    public CardType type() {
        return CardType.CARD205;
    }

    private static <T> List<T> immutableCopy(List<T> values) {
        return values == null ? List.of() : List.copyOf(values);
    }
}
