package uz.uzinfocom.isemid.features.card.models.card_tube;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;
import lombok.experimental.SuperBuilder;
import uz.uzinfocom.isemid.commons.models.BaseEntity;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "card_tube_infection_source")
public class InfectionSource extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_tube_id", nullable = false)
    @JsonIgnore
    private CardTube cardTube;

    private String contactTypeCode; // Sil kasalligi bilan bog‘liq aloqa turi (kod)
    private String fullName; // Aloqa qilingan shaxsning F.I.Sh.
    private String relationCode; // Qarindoshlik darajasi (masalan: ota, ona, qarindosh)
    private String duration; // Aloqa davomiyligi (masalan: 3 oy, 1 yil)
}
