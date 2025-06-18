package uz.uzinfocom.isemid.features.card.models.card161;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;
import uz.uzinfocom.isemid.commons.models.BaseEntity;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "card161_risk_factors")
public class Card161RiskFactor extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card161_id", nullable = false)
    @JsonIgnore
    private Card161 card161;

    private String riskFactorCode;

    private String addressLocation;
    private String seasonTime;
}
