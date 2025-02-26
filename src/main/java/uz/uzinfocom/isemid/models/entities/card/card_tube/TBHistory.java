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
@Entity(name = "card_tube_tb_history")
public class TBHistory extends BaseEntity {
    private String infectionLocation;
    private LocalDate infectionDate;
    private String mkb10Code;
    private String mkb10Name;
    private String registrationGroup;
}
