package uz.uzinfocom.isemid.models.payload.responses.act.act153;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.uzinfocom.isemid.models.payload.responses.RespCatalogDto;
import uz.uzinfocom.isemid.models.payload.responses.RespOrganizationDto;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RespAct153Detail {
    private Long id;
    private UUID uuid;
    private RespCatalogDto object;
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
    private RespOrganizationDto organization;
    private String laboratoryAddress;
    private String deliveryConditions;
    private String storageConditions;
    private String conservationMethods;
    private String additionalInfo;
}
