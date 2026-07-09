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
import uz.uzinfocom.app.modules.card.domain.annotation.CatalogCode;
import uz.uzinfocom.app.platform.persistence.entity.UuidAuditableEntity;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(
        name = "card_tube_tb_history",
        indexes = @Index(name = "idx_card_tube_tb_history_card_tube_id", columnList = "card_tube_id")
)
@NoArgsConstructor
@AllArgsConstructor
public class TBHistory extends UuidAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "card_tube_id", nullable = false, foreignKey = @ForeignKey(name = "fk_card_tube_tb_history_card_tube"))
    private CardTube cardTube;

    @Column(name = "infection_location", length = 255)
    private String infectionLocation;

    @Column(name = "infection_date")
    private LocalDate infectionDate;

    @CatalogCode("mkb10")
    @Column(name = "mkb10_code", length = 64)
    private String mkb10Code;

    @Column(name = "mkb10_name", length = 500)
    private String mkb10Name;

    @Column(name = "registration_group", length = 255)
    private String registrationGroup;
}
