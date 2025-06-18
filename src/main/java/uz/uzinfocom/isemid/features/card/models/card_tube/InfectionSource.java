package uz.uzinfocom.isemid.features.card.models.card_tube;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;
import uz.uzinfocom.isemid.commons.models.BaseEntity;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "card_tube_infection_source")
public class InfectionSource extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_tube_id", nullable = false)
    @JsonIgnore
    private CardTube cardTube;

    private String tbContactCode;
    private String fullName;
    private String relationDegreeCode;
    private String contactDuration;
}
