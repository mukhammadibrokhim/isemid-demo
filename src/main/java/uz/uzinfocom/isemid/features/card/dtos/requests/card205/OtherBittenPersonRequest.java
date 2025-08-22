package uz.uzinfocom.isemid.features.card.dtos.requests.card205;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OtherBittenPersonRequest {
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
    private LocalDateTime bittenDate;    // Tishlangan sana va vaqt
}
