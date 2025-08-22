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
@Entity(name = "card205_other_bitten_animal")
public class OtherBittenAnimal extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card205_id", nullable = false)
    @JsonIgnore
    private Card205 card205;

    private String animalCategoryCode;   // Tishlangan hayvon toifasi kodi
    private LocalDateTime biteDateTime; // Tishlangan vaqt
    private String biteLocation;        // Tishlanish joyi (qayerda tishlangan)
}

