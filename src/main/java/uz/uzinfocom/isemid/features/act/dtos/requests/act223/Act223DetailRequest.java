package uz.uzinfocom.isemid.features.act.dtos.requests.act223;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.uzinfocom.isemid.features.act.dtos.LengthUnit;
import uz.uzinfocom.isemid.features.act.models.common.ResearchItemTypeInfo;
import uz.uzinfocom.isemid.features.act.models.common.SampleTypeInfo;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Act223DetailRequest {
    private Long id;
//    private Integer researchTypeId;
//    private Integer categoryId;
//    private Integer itemTypeId;
//    private Integer itemTypeNameUz;
//    private Integer itemTypeNameRu;
    private ResearchItemTypeInfo researchItemTypeInfo;
    private String exactLocationPointSampling;
    private Double amount;
    private Double depthOfObtainedArea;
    private LengthUnit depthUnit;

//    private Integer deliveryConditionId;
//    private String deliveryConditionsUz;
//    private String deliveryConditionsRu;
//
//    private Integer storageConditionId;
//    private String storageConditionsUz;
//    private String storageConditionsRu;

//    private Integer sampleTypeId;
//    private String sampleTypeUz;
//    private String sampleTypeRu;
    private SampleTypeInfo sampleTypeInfo;
    private String supportingDocumentsForSampling;
}
