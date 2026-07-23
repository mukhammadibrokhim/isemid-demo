package uz.uzinfocom.app.modules.act.domain.model.act224;

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
        name = "act224_detail",
        indexes = @Index(name = "idx_act224_detail_act224_id", columnList = "act224_id")
)
@NoArgsConstructor
@AllArgsConstructor
public class Act224Detail extends UuidAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "act224_id", nullable = false, foreignKey = @ForeignKey(name = "fk_act224_detail_act224"))
    private Act224 act224;

    @Column(name = "recommended_activities", columnDefinition = "TEXT")
    private String recommendedActivities;

    @Column(name = "execution_period")
    private LocalDateTime executionPeriod;
}
