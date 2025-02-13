package uz.uzinfocom.isemid.models.payload.requests.card;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReqCard161RiskFactor {
    private String riskFactorCode;
    private String addressLocation;
    private String seasonTime;
}
