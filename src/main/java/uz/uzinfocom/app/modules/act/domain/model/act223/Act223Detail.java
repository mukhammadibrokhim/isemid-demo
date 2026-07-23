package uz.uzinfocom.app.modules.act.domain.model.act223;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import uz.uzinfocom.app.modules.act.domain.enums.LengthUnit;
import uz.uzinfocom.app.modules.act.domain.model.embedded.ResearchItemTypeInfo;
import uz.uzinfocom.app.platform.persistence.entity.UuidAuditableEntity;

@Getter
@Setter
@Entity
@Table(
        name = "act223_detail",
        indexes = @Index(name = "idx_act223_detail_act223_id", columnList = "act223_id")
)
@NoArgsConstructor
@AllArgsConstructor
public class Act223Detail extends UuidAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "act223_id", nullable = false, foreignKey = @ForeignKey(name = "fk_act223_detail_act223"))
    private Act223 act223;

    @Embedded
    private ResearchItemTypeInfo researchItemTypeInfo;

    /**
     * WKT point, e.g. {@code POINT(69.2797 41.3111)}.
     */
    @Column(name = "exact_location_point_sampling")
    private String exactLocationPointSampling;

    @Column(name = "amount")
    private Double amount;

    @Column(name = "depth_of_obtained_area")
    private Double depthOfObtainedArea;

    @Enumerated(EnumType.STRING)
    @Column(name = "depth_unit", length = 20)
    private LengthUnit depthUnit;
}
