package uz.uzinfocom.isemid.models.entities.card;

import jakarta.persistence.Entity;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class CardTube extends Card {
    private String specificName;
}
