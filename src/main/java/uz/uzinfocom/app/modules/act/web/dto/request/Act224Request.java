package uz.uzinfocom.app.modules.act.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.modules.act.domain.enums.ActType;
import uz.uzinfocom.app.modules.act.web.dto.request.act224.Act224RecommendationRequest;
import uz.uzinfocom.app.modules.act.web.dto.request.embedded.ActSubjectRequest;

import java.util.List;

@Schema(description = "Акт 224 — далолатнома по проверке соблюдения санитарных требований.")
public record Act224Request(
        @Schema(description = "Субъект акта (блок «Tashkilot turi»).")
        @Valid ActSubjectRequest subject,

        @Schema(description = "Вид деятельности («Faoliyat turi») — код из ref_catalog (type=ACTIVITY_TYPE).")
        @Size(max = 255) String activityTypeCode,
        @Size(max = 255) String fullNameOfEpidStaff,
        @Size(max = 255) String positionOfEpidStaff,
        @Size(max = 255) String fullNameOfParticipantEpid,
        @Size(max = 255) String positionOfParticipantEpid,
        @Size(max = 500) String nameOfRegulatoryActs,
        String checkingFulfillmentOfRequirements,
        @Size(max = 255) String fullNameOfParticipant,
        String additionalInfo,

        @Valid List<Act224RecommendationRequest> recommendations
) implements ActRequest {

    public Act224Request {
        recommendations = recommendations == null ? List.of() : List.copyOf(recommendations);
    }

    @Override
    public ActType type() {
        return ActType.ACT224;
    }
}
