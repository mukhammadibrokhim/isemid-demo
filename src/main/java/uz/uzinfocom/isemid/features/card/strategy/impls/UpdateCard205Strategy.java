package uz.uzinfocom.isemid.features.card.strategy.impls;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.features.card.dtos.requests.CardRequest;
import uz.uzinfocom.isemid.features.card.dtos.requests.card205.Card205Request;
import uz.uzinfocom.isemid.features.card.enums.CardStatus;
import uz.uzinfocom.isemid.features.card.models.Card;
import uz.uzinfocom.isemid.features.card.models.card205.Card205;
import uz.uzinfocom.isemid.features.card.models.card205.InformationAboutAnimaBittenPeople;
import uz.uzinfocom.isemid.features.card.models.card205.InformationOtherBittenAnimals;
import uz.uzinfocom.isemid.features.card.models.card205.InformationOtherBittenPeople;
import uz.uzinfocom.isemid.features.card.reposiotries.CardRepository;
import uz.uzinfocom.isemid.features.card.strategy.UpdateCardStrategy;
import uz.uzinfocom.isemid.features.classifier.services.CatalogService;
import uz.uzinfocom.isemid.features.user.models.User;

import java.util.List;
import java.util.Optional;

import static uz.uzinfocom.isemid.features.card.utils.CardUtil.validateCard;

@Service
@RequiredArgsConstructor
public class UpdateCard205Strategy implements UpdateCardStrategy {

    private final CardRepository cardRepository;
    private final CatalogService catalogService;


    @Override
    public ApiResponse<?> updateCard(long id, CardRequest cardRequest, User currentUser) {
        Card card = validateCard(id, cardRequest, cardRepository);
        Card205 existingCard = (Card205) card;
        existingCard.setStatus(CardStatus.IN_PROGRESS);

        Card205Request reqCard205 = (Card205Request) cardRequest;

        Optional.ofNullable(reqCard205.getMkb10Code()).ifPresent(existingCard::setMkb10Code);
        Optional.ofNullable(reqCard205.getMkb10Name()).ifPresent(existingCard::setMkb10Name);
        Optional.ofNullable(reqCard205.getEpidemiologicalObservationDate()).ifPresent(existingCard::setEpidemiologicalObservationDate);
        Optional.ofNullable(reqCard205.getVeterinaryEmergencyInformationDate()).ifPresent(existingCard::setVeterinaryEmergencyInformationDate);
        Optional.ofNullable(reqCard205.getAddressOfBiteOccurrence()).ifPresent(existingCard::setAddressOfBiteOccurrence);
        Optional.ofNullable(reqCard205.getDateOfBiteOccurrence()).ifPresent(existingCard::setDateOfBiteOccurrence);
        Optional.ofNullable(reqCard205.getNameOfTreatmentPreventiveInstitution()).ifPresent(existingCard::setNameOfTreatmentPreventiveInstitution);
        Optional.ofNullable(reqCard205.getDateOfTreatmentPreventiveInstitution()).ifPresent(existingCard::setDateOfTreatmentPreventiveInstitution);
        Optional.ofNullable(reqCard205.getAnimalType()).ifPresent(existingCard::setAnimalType);
        Optional.ofNullable(reqCard205.getWhereAnimalComesFrom()).ifPresent(existingCard::setWhereAnimalComesFrom);
        Optional.ofNullable(reqCard205.getWhenAnimalAppeared()).ifPresent(existingCard::setWhenAnimalAppeared);
        Optional.ofNullable(reqCard205.getAgeOfAnimal()).ifPresent(existingCard::setAgeOfAnimal);
        Optional.ofNullable(reqCard205.getBreedOfAnimal()).ifPresent(existingCard::setBreedOfAnimal);
        Optional.ofNullable(reqCard205.getColourOfAnimal()).ifPresent(existingCard::setColourOfAnimal);
        Optional.ofNullable(reqCard205.getIndividualSignsOfAnimal()).ifPresent(existingCard::setIndividualSignsOfAnimal);
        Optional.ofNullable(reqCard205.getCertificateNumberOfFirstVetResults()).ifPresent(existingCard::setCertificateNumberOfFirstVetResults);
        Optional.ofNullable(reqCard205.getIssueDateOfFirstCertificate()).ifPresent(existingCard::setIssueDateOfFirstCertificate);
        Optional.ofNullable(reqCard205.getCertificateNumberOfSecondVetResults()).ifPresent(existingCard::setCertificateNumberOfSecondVetResults);
        Optional.ofNullable(reqCard205.getIssueDateOfSecondaryCertificate()).ifPresent(existingCard::setIssueDateOfSecondaryCertificate);
        Optional.ofNullable(reqCard205.getDateTimeOfFeatherTaken()).ifPresent(existingCard::setDateTimeOfFeatherTaken);
        Optional.ofNullable(reqCard205.getPetRegisteredVetDepartment()).ifPresent(existingCard::setPetRegisteredVetDepartment);
        Optional.ofNullable(reqCard205.getAdditionalInformation()).ifPresent(existingCard::setAdditionalInformation);
        Optional.ofNullable(reqCard205.getFullNameofAnimalOwner()).ifPresent(existingCard::setFullNameofAnimalOwner);
        Optional.ofNullable(reqCard205.getConditionOfAnimalCode()).ifPresent(existingCard::setConditionOfAnimalCode);
        Optional.ofNullable(reqCard205.getAnimalConservationCode()).ifPresent(existingCard::setAnimalConservationCode);
        Optional.ofNullable(reqCard205.getPositionOfBittenVictimCode()).ifPresent(existingCard::setPositionOfBittenVictimCode);
        Optional.ofNullable(reqCard205.getDogOwnerComplianceCode()).ifPresent(existingCard::setDogOwnerComplianceCode);

        Optional.ofNullable(reqCard205.getInfoAbtAnimalBittenPeople()).ifPresent(reqInformationAboutAnimalBittenPeople -> {
            List<InformationAboutAnimaBittenPeople> animaBittenPeopleList = reqInformationAboutAnimalBittenPeople.stream()
                    .map(list1 -> InformationAboutAnimaBittenPeople.builder()
                            .card205(existingCard)
                            .animalCategoryCode(list1.getAnimalCategoryCode())
                            .animalType(list1.getAnimalType())
                            .fullNameOfAnimalBittenOwner(list1.getFullNameOfAnimalBittenOwner())
                            .addressOfAnimalBittenOwner(list1.getAddressOfAnimalBittenOwner())
                            .region(list1.getRegion())
                            .district(list1.getDistrict())
                            .neighborhood(list1.getNeighborhood())
                            .street(list1.getStreet())
                            .houseNumber(list1.getHouseNumber())
                            .apartmentNumber(list1.getApartmentNumber())
                            .build()).toList();
            existingCard.getInfoAbtAnimalBittenPeople().clear();
            existingCard.getInfoAbtAnimalBittenPeople().addAll(animaBittenPeopleList);
        });
        Optional.ofNullable(reqCard205.getInfoBittenPeople()).ifPresent(reqInformationOtherBittenPeople -> {
            List<InformationOtherBittenPeople> animaBittenPeopleList2 = reqInformationOtherBittenPeople.stream()
                    .map(list1 -> InformationOtherBittenPeople.builder()
                            .card205(existingCard)
                            .lastName(list1.getLastName())
                            .firstName(list1.getFirstName())
                            .middleName(list1.getMiddleName())
                            .gender(list1.getGender())
                            .birthDate(list1.getBirthDate())
                            .livingAddress(list1.getLivingAddress())
                            .region(list1.getRegion())
                            .district(list1.getDistrict())
                            .neighborhood(list1.getNeighborhood())
                            .street(list1.getStreet())
                            .houseNumber(list1.getHouseNumber())
                            .apartmentNumber(list1.getApartmentNumber())
                            .bittenDate(list1.getBittenDate())
                            .build()).toList();
            existingCard.getInfoBittenPeople().clear();
            existingCard.getInfoBittenPeople().addAll(animaBittenPeopleList2);
        });
        Optional.ofNullable(reqCard205.getInfoOtherBittenAnimal()).ifPresent(reqInformationOtherBittenAnimals -> {
            List<InformationOtherBittenAnimals> otherBittenAnimalsList = reqInformationOtherBittenAnimals.stream()
                    .map(info -> InformationOtherBittenAnimals.builder()
                            .card205(existingCard)
                            .bittenAnimalCategoryCode(info.getBittenAnimalCategoryCode())
                            .bittenDateTime(info.getBittenDateTime())
                            .whereAnimalBitten(info.getWhereAnimalBitten())
                            .build()).toList();
            existingCard.getInfoOtherBittenAnimal().clear();
            existingCard.getInfoOtherBittenAnimal().addAll(otherBittenAnimalsList);
        });
        Card205 updatedCard = cardRepository.save(existingCard);
        return new ApiResponse<>(true, "Card205 updated successfully.", updatedCard);
    }
}

