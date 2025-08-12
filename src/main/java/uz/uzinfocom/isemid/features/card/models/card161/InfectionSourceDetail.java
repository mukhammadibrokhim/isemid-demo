package uz.uzinfocom.isemid.features.card.models.card161;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.*;
import lombok.experimental.SuperBuilder;
import uz.uzinfocom.isemid.commons.models.BaseEntity;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity(name = "card161_infection_source_detail")
public class InfectionSourceDetail extends BaseEntity {
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card161_id")
    private Card161 card161;

    private String infectionSourceNotFoundCode;
    private String personFullName;
    private String infectionSourceDiseasePeriodCode;
    private String animalTypeCode;
}
