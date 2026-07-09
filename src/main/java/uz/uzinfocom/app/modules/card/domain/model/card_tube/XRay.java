package uz.uzinfocom.app.modules.card.domain.model.card_tube;

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
import uz.uzinfocom.app.platform.persistence.entity.UuidAuditableEntity;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(
        name = "card_tube_xray",
        indexes = @Index(name = "idx_card_tube_xray_card_tube_id", columnList = "card_tube_id")
)
@NoArgsConstructor
@AllArgsConstructor
public class XRay extends UuidAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "card_tube_id", nullable = false, foreignKey = @ForeignKey(name = "fk_card_tube_xray_card_tube"))
    private CardTube cardTube;

    @Column(name = "xray_date")
    private LocalDate xrayDate;

    @Column(name = "xray_place", length = 255)
    private String xrayPlace;

    @Column(name = "result", length = 500)
    private String result;
}
