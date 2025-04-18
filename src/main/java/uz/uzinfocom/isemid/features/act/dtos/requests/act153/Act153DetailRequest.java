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
    private String objectCode;
    private String samplingPurpose;
    private String samplingPurposeLoinc;
    private String address;
    private Double samplingDepth;
    private Double distanceFromShore;
    private Double sampleVolume;
    private String sampleLocation;
    private Double weatherAtSampling;
    private Double waterTemperature;
    private String sampleType;
    private LocalDateTime samplingDate;
    private String specialSamplingConditions;
    private Long organizationId;
    private String laboratoryAddress;
    private String deliveryConditions;
    private String storageConditions;
    private String conservationMethods;
    private String additionalInfo;
}
