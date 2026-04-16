package uz.uzinfocom.isemid.features.act.dtos.requests.act153;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.time.LocalDateTime;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Act153DetailRequest {
    private Long id;
    private Integer objectTypeId;
    private String objectCode;
    private Integer purposeId;
    private String samplingPurpose;
    private String samplingPurposeLoinc;
    private String address;
    private Double samplingDepth;
    private Double distanceFromShore;
    private Double sampleVolume;
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
