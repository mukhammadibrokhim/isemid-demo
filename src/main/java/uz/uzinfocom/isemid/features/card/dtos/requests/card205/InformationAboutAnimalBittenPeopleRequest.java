package uz.uzinfocom.isemid.features.card.dtos.requests.card205;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InformationAboutAnimalBittenPeopleRequest {
    private Long id;
    private String animalCategoryCode;
    private String animalType;
    private String fullNameOfAnimalBittenOwner;
    private String addressOfAnimalBittenOwner;
    private String region;
    private String district;
    private String neighborhood;
    private String street;
    private String houseNumber;
    private String apartmentNumber;
}
