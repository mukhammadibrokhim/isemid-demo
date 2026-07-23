package uz.uzinfocom.app.modules.act.domain.model.act153;

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
import uz.uzinfocom.app.modules.act.domain.enums.SampleQtUnit;
import uz.uzinfocom.app.modules.act.domain.enums.SampleVolumeUnit;
import uz.uzinfocom.app.modules.act.domain.model.embedded.ResearchItemTypeInfo;
import uz.uzinfocom.app.modules.act.domain.model.embedded.SampleTypeInfo;
import uz.uzinfocom.app.platform.persistence.entity.UuidAuditableEntity;

@Getter
@Setter
@Entity
@Table(
        name = "act153_detail",
        indexes = @Index(name = "idx_act153_detail_act153_id", columnList = "act153_id")
)
@NoArgsConstructor
@AllArgsConstructor
public class Act153Detail extends UuidAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "act153_id", nullable = false, foreignKey = @ForeignKey(name = "fk_act153_detail_act153"))
    private Act153 act153;

    @Embedded
    private ResearchItemTypeInfo researchItemTypeInfo;

    @Column(name = "object_type_id")
    private Integer objectTypeId;

    @Column(name = "object_code")
    private String objectCode;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "sampling_depth")
    private Double samplingDepth;

    @Enumerated(EnumType.STRING)
    @Column(name = "depth_unit", length = 20)
    private LengthUnit depthUnit;

    @Column(name = "distance_from_shore")
    private Double distanceFromShore;

    @Enumerated(EnumType.STRING)
    @Column(name = "distance_from_shore_unit", length = 20)
    private LengthUnit distanceFromShoreUnit;

    @Column(name = "sample_volume")
    private Double sampleVolume;

    @Enumerated(EnumType.STRING)
    @Column(name = "sample_volume_unit", length = 20)
    private SampleVolumeUnit sampleVolumeUnit;

    @Enumerated(EnumType.STRING)
    @Column(name = "sample_qt_unit", length = 20)
    private SampleQtUnit sampleQtUnit;

    @Column(name = "sample_location", columnDefinition = "TEXT")
    private String sampleLocation;

    @Column(name = "weather_at_sampling")
    private Double weatherAtSampling;

    @Column(name = "water_temperature")
    private Double waterTemperature;

    @Embedded
    private SampleTypeInfo sampleTypeInfo;
}
