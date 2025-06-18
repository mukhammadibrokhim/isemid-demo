package uz.uzinfocom.isemid.features.act.dtos.responses.act223;

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
public class Act223DetailResponse {
    private Long id;
    private String exactLocationPointSampling;
    private Double amount;
    private Double depthOfObtainedArea;
    private String supportingDocumentsForSampling;
}
