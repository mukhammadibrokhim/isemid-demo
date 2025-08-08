package uz.uzinfocom.isemid.features.card.models.card205;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import uz.uzinfocom.isemid.commons.models.BaseEntity;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "card205_animal_bite_victim")
public class AnimalBiteVictim extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card205_id", nullable = false)
    @JsonIgnore
    private Card205 card205;

    private String animalCategoryCode;      // Hayvon toifasi kodi
    private String animalType;              // Hayvon turi

    private String ownerFullName;           // Jabrlanuvchi egasining F.I.Sh.
    private String ownerAddress;            // Egasi manzili

    private String stateCode;               // Viloyat kodi
    private String cityCode;                // Shahar/tuman kodi
    private String neighborhoodCode;        // Mahalla kodi

    private String street;                  // Ko‘cha
    private String houseNumber;             // Uy raqami
    private String apartmentNumber;         // Xonadon raqami
}
