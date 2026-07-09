package uz.uzinfocom.app.modules.card.web.dto.request.card_tube;

import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.modules.card.web.dto.request.ChildRequest;

public record InfectionSourceRequest(
        Long id,
        @Size(max = 64) String tbContactCode,
        @Size(max = 255) String fullName,
        @Size(max = 64) String relationDegreeCode,
        @Size(max = 255) String contactDuration
) implements ChildRequest {
}
