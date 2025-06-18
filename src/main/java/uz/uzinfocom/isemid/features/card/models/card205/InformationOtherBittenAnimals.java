package uz.uzinfocom.isemid.features.card.models.card205;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;
import uz.uzinfocom.isemid.commons.models.BaseEntity;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "card205_info_bitten_animals")
public class InformationOtherBittenAnimals extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card205_id", nullable = false)
    @JsonIgnore
    private Card205 card205;

    private String bittenAnimalCategoryCode;
    private LocalDateTime bittenDateTime;
    private String whereAnimalBitten;
}
