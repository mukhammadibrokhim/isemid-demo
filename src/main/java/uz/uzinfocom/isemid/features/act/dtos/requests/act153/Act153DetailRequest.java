package uz.uzinfocom.isemid.features.act.dtos.requests.act153;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.uzinfocom.isemid.features.act.dtos.LengthUnit;
import uz.uzinfocom.isemid.features.act.dtos.SampleQtUnit;
import uz.uzinfocom.isemid.features.act.models.common.ConservationTypeInfo;
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
//    private Integer researchTypeId;
//    private Integer categoryId;
//    private Integer itemTypeId;
//    private Integer itemTypeNameUz;
//    private Integer itemTypeNameRu;
    private ResearchItemTypeInfo researchItemTypeInfo;
    private Integer objectTypeId;
    private String objectCode;
//    private Integer purposeId;
//    private String samplingPurposeUz;
//    private String samplingPurposeRu;
//    private String samplingPurposeLoinc;
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
    //    private Integer sampleTypeId;
//    private String sampleTypeUz;
//    private String sampleTypeRu;
    private SampleTypeInfo sampleTypeInfo;
    private LocalDateTime samplingDate;
//    private Integer specialConditionId;
//    private String specialSamplingConditionsUz;
//    private String specialSamplingConditionsRu;
    private Long lisOrganizationId;
    private String laboratoryAddress;
    //    private Integer deliveryConditionId;
//    private String deliveryConditionsUz;
//    private String deliveryConditionsRu;
//    private Integer storageConditionId;
//    private String storageConditionsUz;
//    private String storageConditionsRu;
//    private Integer conservationMethodId;
//    private String conservationMethodsUz;
//    private String conservationMethodsRu;
    private ConservationTypeInfo conservationTypeInfo;
    private String additionalInfo;
}
