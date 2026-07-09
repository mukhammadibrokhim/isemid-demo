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
import uz.uzinfocom.app.platform.persistence.entity.UuidAuditableEntity;

@Getter
@Setter
@Entity
@Table(
        name = "card161_screened_group",
        indexes = @Index(name = "idx_card161_screened_group_card161_id", columnList = "card161_id")
)
@NoArgsConstructor
@AllArgsConstructor
public class ScreenedGroup extends UuidAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "card161_id", nullable = false, foreignKey = @ForeignKey(name = "fk_card161_screened_group_card161"))
    private Card161 card161;

    @Column(name = "team_name", length = 255)
    private String teamName;

    @Column(name = "prophylactic_address", length = 500)
    private String prophylacticAddress;

    @Column(name = "contact_count", length = 32)
    private String contactCount;

    @Column(name = "required_prophylactic_substance", length = 500)
    private String requiredProphylacticSubstance;

    @Column(name = "treated_with_prophylactic_substance", length = 500)
    private String treatedWithProphylacticSubstance;

    @Column(name = "laboratory_test_conducted", length = 500)
    private String laboratoryTestConducted;
}
