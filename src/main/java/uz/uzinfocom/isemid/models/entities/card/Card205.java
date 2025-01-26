package uz.uzinfocom.isemid.models.entities.card;

import jakarta.persistence.Entity;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Card205 extends Card {
    private String specificFieldForCard205; // Faqat Card205 ga xos bo'lgan maydon
}
