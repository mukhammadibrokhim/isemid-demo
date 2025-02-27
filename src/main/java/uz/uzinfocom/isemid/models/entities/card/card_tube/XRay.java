package uz.uzinfocom.isemid.models.entities.card.card_tube;

import jakarta.persistence.Entity;
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
    private LocalDate xrayDate;
    private String xrayPlace;
    private String result;
}
