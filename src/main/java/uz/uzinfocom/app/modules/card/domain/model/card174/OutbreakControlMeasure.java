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
import uz.uzinfocom.app.modules.card.domain.annotation.CatalogCode;
import uz.uzinfocom.app.platform.persistence.entity.UuidAuditableEntity;

@Getter
@Setter
@Entity
@Table(
        name = "card174_outbreak_control_measure",
        indexes = @Index(name = "idx_card174_outbreak_control_measure_card174_id", columnList = "card174_id")
)
@NoArgsConstructor
@AllArgsConstructor
public class OutbreakControlMeasure extends UuidAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "card174_id", nullable = false, foreignKey = @ForeignKey(name = "fk_card174_outbreak_control_measure_card174"))
    private Card174 card174;

    @Column(name = "vaccinated_animals")
    private Integer vaccinatedAnimals;

    @Column(name = "lost_animals")
    private Integer lostAnimals;

    @Column(name = "meat_delivered")
    private Integer meatDelivered;

    @CatalogCode("card174-processing-method")
    @Column(name = "processing_method_code", length = 64)
    private String processingMethodCode;

    @Column(name = "processed_area")
    private Integer processedArea;

    @Column(name = "event_conducted")
    private Boolean eventConducted;
}
