package uz.uzinfocom.isemid.features.card.models.card205;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import uz.uzinfocom.isemid.commons.models.BaseEntity;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity(name = "card205_animal_bite_victim")
public class AnimalBiteVictim extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card205_id", nullable = false)
    @JsonIgnore
    private Card205 card205;

    private String animalCategoryCode;      // Hayvon toifasi kodi
    private String animalType;              // Hayvon turi
    private String ownerFullName;           // Hayvon egasining F.I.Sh.
    private String ownerAddress;            // Egasi manzili
    private String stateCode;               // Viloyat kodi
    private String cityCode;                // Shahar/tuman kodi
    private String neighborhoodCode;        // Mahalla kodi
    private String street;                  // Ko‘cha
    private String houseNumber;             // Uy raqami
    private String apartmentNumber;         // Xonadon raqami
}
