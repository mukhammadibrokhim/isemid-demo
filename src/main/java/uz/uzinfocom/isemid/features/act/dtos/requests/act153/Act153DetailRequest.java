package uz.uzinfocom.isemid.features.act.dtos.requests.act153;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.uzinfocom.isemid.features.act.dtos.LengthUnit;
import uz.uzinfocom.isemid.features.act.models.common.ResearchItemTypeInfo;
import uz.uzinfocom.isemid.features.act.models.common.SampleTypeInfo;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Act153DetailRequest {
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
    private String sampleLocation;
    private Double weatherAtSampling;
    private Double waterTemperature;
    private SampleTypeInfo sampleTypeInfo;
    private LocalDateTime samplingDate;
}
