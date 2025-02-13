package uz.uzinfocom.isemid.models.entities.card;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;
import uz.uzinfocom.isemid.models.entities.template.BaseEntity;
import uz.uzinfocom.isemid.models.entities.value_set.Catalog;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "card161_risk_factors")
public class Card161RiskFactor extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "card161_id", nullable = false)
    @JsonIgnore
    private Card161 card161;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(referencedColumnName = "code")
    @JsonIgnoreProperties({"type", "id", "createdAt", "updatedAt", "createdBy", "updatedBy"})
    private Catalog riskFactor;
    private String addressLocation;
    private String seasonTime;
}
