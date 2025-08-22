package uz.uzinfocom.isemid.features.card.dtos.requests.card205;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AnimalBiteVictimRequest {
    private Long id;
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
