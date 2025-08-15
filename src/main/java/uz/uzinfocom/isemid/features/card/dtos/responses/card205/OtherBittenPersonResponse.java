package uz.uzinfocom.isemid.features.card.dtos.responses.card205;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OtherBittenPersonResponse {
    private Long id;
    private String lastName;             // Familiya
    private String firstName;            // Ismi
    private String middleName;           // Otasining ismi
    private String gender;               // Jinsi
    private String birthDate;            // Tug‘ilgan sana (string ko‘rinishda qoladi)
    private String livingAddress;        // Yashash manzili
    private String stateCode;            // Viloyat/shahar kodi
    private String cityCode;             // Tuman/shahar kodi (o‘zgartirilmaydi)
    private String neighborhoodCode;     // Mahalla kodi
    private String street;               // Ko‘cha
    private String houseNumber;          // Uy raqami
    private String apartmentNumber;      // Xonadon raqami
    private LocalDateTime bittenDateTime;    // Tishlangan sana va vaqt
}
