package uz.uzinfocom.isemid.features.act.dtos.requests.act223;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Act223DetailRequest {
    private Long id;
    private String exactLocationPointSampling;
    private Double amount;
    private Double depthOfObtainedArea;
    private Integer deliveryConditionId;
    private String deliveryConditions;
    private Integer storageConditionId;
    private String storageConditions;
    private String supportingDocumentsForSampling;
}
