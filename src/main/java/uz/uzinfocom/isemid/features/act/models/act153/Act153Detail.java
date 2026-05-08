package uz.uzinfocom.isemid.features.act.models.act153;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import uz.uzinfocom.isemid.commons.models.BaseEntity;
import uz.uzinfocom.isemid.features.act.dtos.LengthUnit;
import uz.uzinfocom.isemid.features.act.dtos.SampleQtUnit;
import uz.uzinfocom.isemid.features.act.dtos.SampleVolumeUnit;
import uz.uzinfocom.isemid.features.act.models.common.ResearchItemTypeInfo;
import uz.uzinfocom.isemid.features.act.models.common.SampleTypeInfo;


@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "act153_detail")
public class Act153Detail extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "act153_id")
    @JsonBackReference
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
    private Double samplingDepth; // number

    @Enumerated(EnumType.STRING)
    private LengthUnit depthUnit;

    @Column(name = "distance_from_shore")
    private Double distanceFromShore; // number

    @Enumerated(EnumType.STRING)
    private LengthUnit distanceFromShoreUnit;

    @Column(name = "sample_volume")
    private Double sampleVolume; // number

    @Enumerated(EnumType.STRING)
    private SampleVolumeUnit sampleVolumeUnit;

    @Enumerated(EnumType.STRING)
    private SampleQtUnit sampleQtUnit;

    @Column(name = "sample_location", columnDefinition = "TEXT")
    private String sampleLocation;

    @Column(name = "weather_at_sampling")
    private Double weatherAtSampling; // number (Celsius)

    @Column(name = "water_temperature")
    private Double waterTemperature; // number (Celsius)

    @Embedded
    private SampleTypeInfo sampleTypeInfo;



}
