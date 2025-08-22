package uz.uzinfocom.isemid.features.card.dtos.responses.card205;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AnimalBiteVictimResponse {
    private Long id;
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
