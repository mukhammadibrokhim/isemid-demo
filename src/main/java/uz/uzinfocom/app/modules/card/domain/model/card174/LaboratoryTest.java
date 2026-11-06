package uz.uzinfocom.app.modules.card.domain.model.card174;

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

/**
 * One row of the card's "laboratory tests" table. Replaces the single
 * {@code animal_type}/{@code test_*} column set Card174 used to carry —
 * the paper form (and the legacy {@code card174_external_sample_test}
 * table) has always allowed several rows.
 */
@Getter
@Setter
@Entity
@Table(
        name = "card174_laboratory_test",
        indexes = @Index(name = "idx_card174_laboratory_test_card174_id", columnList = "card174_id")
)
@NoArgsConstructor
@AllArgsConstructor
public class LaboratoryTest extends UuidAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "card174_id", nullable = false, foreignKey = @ForeignKey(name = "fk_card174_laboratory_test_card174"))
    private Card174 card174;

    /** Animal type and name of the tested sample — one free-text column on the paper form. */
    @Column(name = "sample_name", length = 255)
    private String sampleName;

    @Column(name = "test_date")
    private LocalDate testDate;

    @Column(name = "sample_count")
    private Integer sampleCount;

    @Column(name = "testing_method", length = 255)
    private String testingMethod;

    @Column(name = "test_result", length = 500)
    private String testResult;
}
