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

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(
        name = "card161_environmental_source",
        indexes = @Index(name = "idx_card161_env_source_card161_id", columnList = "card161_id")
)
@NoArgsConstructor
@AllArgsConstructor
public class EnvironmentalSource extends UuidAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "card161_id", nullable = false, foreignKey = @ForeignKey(name = "fk_card161_env_source_card161"))
    private Card161 card161;

    @Column(name = "food_and_water_source_types", length = 500)
    private String foodAndWaterSourceTypes;

    @Column(name = "collection_location", length = 500)
    private String collectionLocation;

    @Column(name = "collection_time")
    private LocalDateTime collectionTime;

    @Column(name = "usage_location", length = 500)
    private String usageLocation;

    @Column(name = "usage_time")
    private LocalDateTime usageTime;

    @Column(name = "storage_conditions", length = 500)
    private String storageConditions;

    @Column(name = "quality_feedback", length = 1000)
    private String qualityFeedbackFromPatientAndOthers;
}
