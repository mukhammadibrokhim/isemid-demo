package uz.uzinfocom.isemid.models.payload.responses.act;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RespAct153Detail {
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
