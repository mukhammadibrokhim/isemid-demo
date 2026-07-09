package uz.uzinfocom.app.modules.card.web.dto.request.card_tube;

import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.modules.card.web.dto.request.ChildRequest;

import java.time.LocalDate;

public record XRayRequest(
        Long id,
        LocalDate xrayDate,
        @Size(max = 255) String xrayPlace,
        @Size(max = 500) String result
) implements ChildRequest {
}
