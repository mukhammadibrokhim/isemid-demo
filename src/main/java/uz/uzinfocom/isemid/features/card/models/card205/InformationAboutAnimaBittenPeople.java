package uz.uzinfocom.isemid.features.card.models.card205;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import uz.uzinfocom.isemid.commons.models.BaseEntity;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "card205_info_about_animal_bitten_people")
public class InformationAboutAnimaBittenPeople extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card205_id", nullable = false)
    @JsonIgnore
    private Card205 card205;

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
