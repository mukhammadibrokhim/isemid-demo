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
@Entity(name = "card_tube_tb_history")
public class TBHistory extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "card_tube_id", nullable = false)
    @JsonIgnore
    private CardTube cardTube;

    private String infectionLocation;
    private LocalDate infectionDate;
    private String mkb10Code;
    private String mkb10Name;
    private String registrationGroup;
}
