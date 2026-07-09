package uz.uzinfocom.app.modules.card.web.dto.request.card161;

import jakarta.validation.constraints.Size;

public record InfectionSourceRequest(
        @Size(max = 500) String fullName,
        @Size(max = 500) String diagnosisClinicalFormOrDonorStatus,
        @Size(max = 500) String contactInfoAndDonorResidence,
        @Size(max = 500) String testResult
) {
}
