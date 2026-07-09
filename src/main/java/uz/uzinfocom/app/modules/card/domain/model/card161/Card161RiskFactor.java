package uz.uzinfocom.app.modules.card.domain.model.card161;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.uzinfocom.app.modules.card.domain.annotation.CatalogCode;
import uz.uzinfocom.app.platform.persistence.entity.UuidAuditableEntity;

@Getter
@Setter
@Entity
@Table(
        name = "card161_risk_factors",
        indexes = @Index(name = "idx_card161_risk_factor_card161_id", columnList = "card161_id")
)
@NoArgsConstructor
@AllArgsConstructor
public class Card161RiskFactor extends UuidAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "card161_id", nullable = false, foreignKey = @ForeignKey(name = "fk_card161_risk_factor_card161"))
    private Card161 card161;

    @CatalogCode("card161-risk-factor")
    @Column(name = "risk_factor_code", length = 64)
    private String riskFactorCode;

    @Column(name = "address_location", length = 500)
    private String addressLocation;

    @Column(name = "season_time", length = 100)
    private String seasonTime;
}
