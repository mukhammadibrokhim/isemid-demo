package uz.uzinfocom.app.modules.card.web.dto.request.card161;

import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.modules.card.web.dto.request.ChildRequest;

public record ContactPersonRequest(
        Long id,
        @Size(max = 500) String fullName,
        @Size(max = 32) String age,
        @Size(max = 500) String address,
        @Size(max = 500) String jobTypeAndLocation,
        @Size(max = 255) String immunizationStatus,
        @Size(max = 500) String restrictionMeasures
) implements ChildRequest {
}
