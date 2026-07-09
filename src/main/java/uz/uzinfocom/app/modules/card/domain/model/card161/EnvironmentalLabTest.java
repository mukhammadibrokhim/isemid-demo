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

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(
        name = "card161_environmental_lab_test",
        indexes = @Index(name = "idx_card161_env_lab_test_card161_id", columnList = "card161_id")
)
@NoArgsConstructor
@AllArgsConstructor
public class EnvironmentalLabTest extends UuidAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "card161_id", nullable = false, foreignKey = @ForeignKey(name = "fk_card161_env_lab_test_card161"))
    private Card161 card161;

    @Column(name = "examination_date")
    private LocalDate examinationDate;

    @Column(name = "object_arthropods_animals", length = 500)
    private String objectArthropodsAnimals;

    @Column(name = "material", length = 255)
    private String material;

    @Column(name = "sample_quantity", length = 100)
    private String sampleQuantity;

    @Column(name = "test_type_and_result", length = 500)
    private String testTypeAndResult;
}
