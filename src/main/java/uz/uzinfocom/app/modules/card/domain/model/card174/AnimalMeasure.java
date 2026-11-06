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
 * One row of the card's "measures applied to the animals in the outbreak"
 * table — name, date, count, place. The legacy
 * {@code card174_preventive_measure} table; separate from
 * {@link OutbreakControlMeasure}.
 */
@Getter
@Setter
@Entity
@Table(
        name = "card174_animal_measure",
        indexes = @Index(name = "idx_card174_animal_measure_card174_id", columnList = "card174_id")
)
@NoArgsConstructor
@AllArgsConstructor
public class AnimalMeasure extends UuidAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "card174_id", nullable = false, foreignKey = @ForeignKey(name = "fk_card174_animal_measure_card174"))
    private Card174 card174;

    @Column(name = "measure_name", length = 500)
    private String measureName;

    @Column(name = "measure_date")
    private LocalDate measureDate;

    @Column(name = "measure_count")
    private Integer measureCount;

    @Column(name = "measure_location", length = 500)
    private String measureLocation;
}
