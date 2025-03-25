package uz.uzinfocom.isemid.models.payload.responses.act.act223;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RespAct223Detail {
    private String exactLocationPointSampling;
    private Double amount;
    private Double depthOfObtainedArea;
    private String supportingDocumentsForSampling;
}
