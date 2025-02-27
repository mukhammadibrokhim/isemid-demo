package uz.uzinfocom.isemid.models.entities.card.card_tube;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;
import uz.uzinfocom.isemid.models.entities.template.BaseEntity;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "card_tube_xray")
public class XRay extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "card_tube_id", nullable = false)
    @JsonIgnore
    private CardTube cardTube;

    private LocalDate xrayDate;
    private String xrayPlace;
    private String result;
}
