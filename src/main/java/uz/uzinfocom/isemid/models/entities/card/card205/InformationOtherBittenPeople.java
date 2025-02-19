package uz.uzinfocom.isemid.models.entities.card.card205;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;
import uz.uzinfocom.isemid.models.entities.template.BaseEntity;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "card205_info_bitten_people")
public class InformationOtherBittenPeople extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "card205_id", nullable = false)
    @JsonIgnore
    private Card205 card205;

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
