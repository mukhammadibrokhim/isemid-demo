package uz.uzinfocom.isemid.models.payload.requests.card.card161;

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
public class ReqCard161RiskFactor {
    private Long id;
    private String riskFactorCode;
    private String addressLocation;
    private String seasonTime;
}
