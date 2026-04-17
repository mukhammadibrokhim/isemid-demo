package uz.uzinfocom.isemid.features.act.dtos.responses.act153;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.uzinfocom.isemid.features.act.dtos.LengthUnit;
import uz.uzinfocom.isemid.features.act.dtos.SampleQtUnit;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Act153DetailResponse {
    private Long id;
    private Integer itemTypeId;
    private String objectCode;
    private Integer purposeId;
    private String samplingPurpose;
    private String samplingPurposeLoinc;
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
    private Integer sampleTypeId;
    private String sampleType;
    private LocalDateTime samplingDate;
    private Integer specialConditionId;
    private String specialSamplingConditions;
    private Long lisOrganizationId;
    private String laboratoryAddress;
    private String deliveryConditions;
    private Integer storageConditionId;
    private String storageConditions;
    private Integer conservationMethodId;
    private String conservationMethods;
    private String additionalInfo;
}
