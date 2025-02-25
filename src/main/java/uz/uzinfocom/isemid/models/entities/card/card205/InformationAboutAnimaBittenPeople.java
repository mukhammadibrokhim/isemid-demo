package uz.uzinfocom.isemid.models.entities.card.card205;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;
import uz.uzinfocom.isemid.models.entities.template.AbsEntity;
import uz.uzinfocom.isemid.models.entities.value_set.Catalog;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "card205_info_about_animal_bitten_people")
public class InformationAboutAnimaBittenPeople extends AbsEntity {

    @ManyToOne
    @JoinColumn(name = "card205_id", nullable = false)
    @JsonIgnore
    private Card205 card205;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(referencedColumnName = "code")
    @JsonIgnoreProperties({"type", "id", "createdAt", "updatedAt", "createdBy", "updatedBy"})
    private Catalog animalCategory;
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
