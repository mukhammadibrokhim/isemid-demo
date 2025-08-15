package uz.uzinfocom.isemid.features.card.models.card205;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;
import lombok.experimental.SuperBuilder;
import uz.uzinfocom.isemid.commons.models.BaseEntity;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity(name = "card205_other_bitten_people")
public class OtherBittenPerson extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card205_id", nullable = false)
    @JsonIgnore
    private Card205 card205;

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

