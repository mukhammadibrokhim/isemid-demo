package uz.uzinfocom.app.modules.card.web.dto.request.card161;

import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.modules.card.web.dto.request.ChildRequest;

public record Card161RiskFactorRequest(
        Long id,
        @Size(max = 64) String riskFactorCode,
        @Size(max = 500) String addressLocation,
        @Size(max = 100) String seasonTime
) implements ChildRequest {
}
