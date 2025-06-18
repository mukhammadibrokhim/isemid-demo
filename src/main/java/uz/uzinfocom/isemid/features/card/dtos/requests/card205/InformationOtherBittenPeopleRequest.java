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
public class InformationOtherBittenPeopleRequest {
    private Long id;
    private String lastName; //Familiya.
    private String firstName; //Ismi.
    private String middleName; //Sharifi.
    private String gender; //Jinsi
    private String birthDate; //Tug‘ilgan yili (yoshi)
    private String livingAddress; //Yashash manzili
    private String region;//Viloyat/shahar
    private String district;//Tuman
    private String neighborhood;//Mahalla
    private String street;//Ko‘cha
    private String houseNumber;//Uy
    private String apartmentNumber;//Xonadon
    private LocalDateTime bittenDate; //tishlangan vaqti
}
