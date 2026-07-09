package uz.uzinfocom.app.modules.card.web.dto.request.card161;

import jakarta.validation.constraints.Size;

public record ScreenedGroupRequest(
        @Size(max = 255) String teamName,
        @Size(max = 500) String prophylacticAddress,
        @Size(max = 32) String contactCount,
        @Size(max = 500) String requiredProphylacticSubstance,
        @Size(max = 500) String treatedWithProphylacticSubstance,
        @Size(max = 500) String laboratoryTestConducted
) {
}
