package uz.uzinfocom.isemid.features.act.dtos.requests.act223;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.uzinfocom.isemid.features.act.dtos.LengthUnit;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Act223DetailRequest {
    private Long id;
    private Integer itemTypeId;
    private String exactLocationPointSampling;
    private Double amount;
    private Double depthOfObtainedArea;
    private LengthUnit depthUnit;
    private Integer deliveryConditionId;
    private String deliveryConditions;
    private Integer storageConditionId;
    private String storageConditions;
    private Integer sampleTypeId;
    private String sampleType;
    private String supportingDocumentsForSampling;
}
