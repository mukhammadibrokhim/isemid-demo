package uz.uzinfocom.app.modules.act.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.uzinfocom.app.modules.act.domain.enums.ActStatus;
import uz.uzinfocom.app.modules.card.domain.model.Card;
import uz.uzinfocom.app.platform.persistence.entity.AbsEntity;

/**
 * Minimal placeholder for the Act feature. The legacy module has 6 act
 * subtypes (ACT153/154/155/156/223/224) with their own detail entities —
 * rebuilding that is out of scope for the Card module; this entity exists
 * only so {@code Card.acts} and the assign-act flow have somewhere to
 * attach to. Do not add subtype-specific fields here without designing the
 * Act module properly.
 */
@Getter
@Setter
@Entity
@Table(
        name = "act",
        indexes = {
                @Index(name = "idx_act_card_id", columnList = "card_id")
        }
)
@NoArgsConstructor
@AllArgsConstructor
public class Act extends AbsEntity {

    @Column(name = "act_type", nullable = false, length = 50)
    private String actType;

    @Enumerated(EnumType.STRING)
    @Column(name = "act_status", nullable = false, length = 32)
    private ActStatus actStatus = ActStatus.NEW;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "card_id", nullable = false)
    private Card card;

    @Column(name = "card_id", insertable = false, updatable = false)
    private Long cardId;
}
