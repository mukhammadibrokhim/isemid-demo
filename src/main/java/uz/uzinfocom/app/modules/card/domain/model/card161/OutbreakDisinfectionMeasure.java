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
        name = "card161_outbreak_measure",
        indexes = @Index(name = "idx_card161_outbreak_measure_card161_id", columnList = "card161_id")
)
@NoArgsConstructor
@AllArgsConstructor
public class OutbreakDisinfectionMeasure extends UuidAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "card161_id", nullable = false, foreignKey = @ForeignKey(name = "fk_card161_outbreak_measure_card161"))
    private Card161 card161;

    @CatalogCode("card161-preventive-measure")
    @Column(name = "preventive_measures_code", length = 64)
    private String preventiveMeasuresCode;

    @Column(name = "drug_type", length = 255)
    private String drugType;

    @Column(name = "conducted_at", length = 255)
    private String conductedAt;

    @CatalogCode("card161-conducted-location")
    @Column(name = "conducted_location_code", length = 64)
    private String conductedLocationCode;

    @Column(name = "executors", length = 500)
    private String executors;

    @Column(name = "execution_monitoring_result", length = 500)
    private String executionMonitoringResult;
}
