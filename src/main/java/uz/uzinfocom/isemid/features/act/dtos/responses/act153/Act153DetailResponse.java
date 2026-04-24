package uz.uzinfocom.isemid.features.act.dtos.responses.act153;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.uzinfocom.isemid.features.act.dtos.LengthUnit;
import uz.uzinfocom.isemid.features.act.dtos.SampleQtUnit;
import uz.uzinfocom.isemid.features.act.models.common.ResearchItemTypeInfo;
import uz.uzinfocom.isemid.features.act.models.common.SampleTypeInfo;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Act153DetailResponse {
    private Long id;
    private ResearchItemTypeInfo researchItemTypeInfo;
    private Integer objectTypeId;
    private String objectCode;
    private String address;
    private Double samplingDepth;
    private LengthUnit depthUnit;
    private Double distanceFromShore;
    private LengthUnit distanceFromShoreUnit;
    private Double sampleVolume;
    private SampleQtUnit sampleQtUnit;
    private String sampleLocation;
    private Double weatherAtSampling;
    private Double waterTemperature;
    private SampleTypeInfo sampleTypeInfo;
}
