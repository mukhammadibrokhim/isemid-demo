package uz.uzinfocom.app.modules.act.domain.model.act154;

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
import uz.uzinfocom.app.modules.act.domain.enums.SampleQtUnit;
import uz.uzinfocom.app.modules.act.domain.enums.SampleVolumeUnit;
import uz.uzinfocom.app.modules.act.domain.model.embedded.ResearchItemTypeInfo;
import uz.uzinfocom.app.platform.persistence.entity.UuidAuditableEntity;

@Getter
@Setter
@Entity
@Table(
        name = "act154_detail",
        indexes = @Index(name = "idx_act154_detail_act154_id", columnList = "act154_id")
)
@NoArgsConstructor
@AllArgsConstructor
public class Act154Detail extends UuidAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "act154_id", nullable = false, foreignKey = @ForeignKey(name = "fk_act154_detail_act154"))
    private Act154 act154;

    @Embedded
    private ResearchItemTypeInfo researchItemTypeInfo;

    @Column(name = "shift_code")
    private String shiftCode;

    @Column(name = "sample_name")
    private String sampleName;

    @Column(name = "group_size")
    private Long groupSize;

    @Column(name = "serial_number_of_group")
    private Long serialNumberOfGroup;

    @Column(name = "sample_weight")
    private Double sampleWeight;

    @Enumerated(EnumType.STRING)
    @Column(name = "sample_qt_unit", length = 20)
    private SampleQtUnit sampleQtUnit;

    @Column(name = "sample_volume")
    private Double sampleVolume;

    @Enumerated(EnumType.STRING)
    @Column(name = "sample_volume_unit", length = 20)
    private SampleVolumeUnit sampleVolumeUnit;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;
}
