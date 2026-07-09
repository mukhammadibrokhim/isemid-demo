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

/**
 * CardTube's own possible-infection-source child — unrelated to (and
 * distinct from) {@code card161.InfectionSource}, which is a separate,
 * Card161-owned entity with its own fields.
 */
@Getter
@Setter
@Entity(name = "card_tube_infection_source")
@Table(
        name = "card_tube_infection_source",
        indexes = @Index(name = "idx_card_tube_infection_source_card_tube_id", columnList = "card_tube_id")
)
@NoArgsConstructor
@AllArgsConstructor
public class InfectionSource extends UuidAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "card_tube_id", nullable = false, foreignKey = @ForeignKey(name = "fk_card_tube_infection_source_card_tube"))
    private CardTube cardTube;

    @CatalogCode("card-tube-tb-contact")
    @Column(name = "tb_contact_code", length = 64)
    private String tbContactCode;

    @Column(name = "full_name", length = 255)
    private String fullName;

    @CatalogCode("card-tube-relation-degree")
    @Column(name = "relation_degree_code", length = 64)
    private String relationDegreeCode;

    @Column(name = "contact_duration", length = 255)
    private String contactDuration;
}
