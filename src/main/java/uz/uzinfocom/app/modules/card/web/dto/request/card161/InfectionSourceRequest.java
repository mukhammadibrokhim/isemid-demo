package uz.uzinfocom.app.modules.card.web.dto.request.card161;

import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.modules.card.web.dto.request.ChildRequest;

public record InfectionSourceRequest(
        Long id,
        @Size(max = 500) String fullName,
        @Size(max = 500) String diagnosisClinicalFormOrDonorStatus,
        @Size(max = 500) String contactInfoAndDonorResidence,
        @Size(max = 500) String testResult
) implements ChildRequest {
}
