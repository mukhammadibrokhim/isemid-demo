package uz.uzinfocom.isemid.features.act.dtos.requests.act153;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.time.LocalDateTime;
import uz.uzinfocom.isemid.features.act.dtos.LengthUnit;
import uz.uzinfocom.isemid.features.act.dtos.SampleQtUnit;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Act153DetailRequest {
    private Long id;
    private Integer itemTypeId;
    private Integer objectTypeId;
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
    private Integer deliveryConditionId;
    private String deliveryConditions;
    private Integer storageConditionId;
    private String storageConditions;
    private Integer conservationMethodId;
    private String conservationMethods;
    private String additionalInfo;
}
