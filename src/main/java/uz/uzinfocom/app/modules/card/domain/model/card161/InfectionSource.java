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

/**
 * Card161's own possible-infection-source child — unrelated to (and
 * distinct from) {@code card_tube.InfectionSource}. Hibernate uses the bare
 * simple class name as the JPA entity name by default, which collides
 * across packages, so both give an explicit, distinct {@code name}.
 */
@Getter
@Setter
@Entity(name = "card161_infection_source")
@Table(
        name = "card161_infection_source",
        indexes = @Index(name = "idx_card161_infection_source_card161_id", columnList = "card161_id")
)
@NoArgsConstructor
@AllArgsConstructor
public class InfectionSource extends UuidAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "card161_id", nullable = false, foreignKey = @ForeignKey(name = "fk_card161_infection_source_card161"))
    private Card161 card161;

    @Column(name = "full_name", length = 500)
    private String fullName;

    @Column(name = "diagnosis_clinical_form_or_donor_status", length = 500)
    private String diagnosisClinicalFormOrDonorStatus;

    @Column(name = "contact_info_and_donor_residence", length = 500)
    private String contactInfoAndDonorResidence;

    @Column(name = "test_result", length = 500)
    private String testResult;
}
