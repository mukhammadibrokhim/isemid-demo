package uz.uzinfocom.isemid.models.entities.card.card161;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import uz.uzinfocom.isemid.models.entities.template.BaseEntity;
import uz.uzinfocom.isemid.models.entities.value_set.Catalog;

import java.util.ArrayList;
import java.util.List;

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
//
//    @ManyToOne(cascade = CascadeType.ALL)
//    @JoinColumn(referencedColumnName = "code")
//    @JsonIgnoreProperties({"type", "id", "createdAt", "updatedAt", "createdBy", "updatedBy"})
//    private Catalog riskFactor;

    @ManyToMany()
    @JoinTable(
            name = "card161_risk_factor_code",
            joinColumns = @JoinColumn(name = "card161_id"),
            inverseJoinColumns = @JoinColumn(name = "catalog_code", referencedColumnName = "code")
    )
    @JsonIgnoreProperties({"type", "createdAt", "updatedAt", "createdBy", "updatedBy"})
    @Builder.Default
    private List<Catalog> riskFactors = new ArrayList<>();



    private String addressLocation;
    private String seasonTime;
}
