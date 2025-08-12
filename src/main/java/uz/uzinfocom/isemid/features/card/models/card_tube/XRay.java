package uz.uzinfocom.isemid.features.card.models.card_tube;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;
import lombok.experimental.SuperBuilder;
import uz.uzinfocom.isemid.commons.models.BaseEntity;

import java.time.LocalDate;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "card_tube_xray")
public class XRay extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_tube_id", nullable = false)
    @JsonIgnore
    private CardTube cardTube;

    private LocalDate xrayDate;
    private String xrayPlace;
    private String result;
}
