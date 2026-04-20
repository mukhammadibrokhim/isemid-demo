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
    private Integer researchTypeId;
    private Integer categoryId;
    private Integer itemTypeId;
    private Integer itemTypeNameUz;
    private Integer itemTypeNameRu;
    private String objectCode;
    private Integer purposeId;
    private String samplingPurposeUz;
    private String samplingPurposeRu;
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
    private String sampleTypeUz;
    private String sampleTypeRu;
    private LocalDateTime samplingDate;
    private Integer specialConditionId;
    private String specialSamplingConditionsUz;
    private String specialSamplingConditionsRu;
    private Long lisOrganizationId;
    private String laboratoryAddress;
    private Integer deliveryConditionId;
    private String deliveryConditionsUz;
    private String deliveryConditionsRu;
    private Integer storageConditionId;
    private String storageConditionsUz;
    private String storageConditionsRu;
    private Integer conservationMethodId;
    private String conservationMethodsUz;
    private String conservationMethodsRu;
    private String additionalInfo;
}
