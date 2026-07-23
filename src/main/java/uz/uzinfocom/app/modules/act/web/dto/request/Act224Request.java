package uz.uzinfocom.app.modules.act.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.modules.act.domain.enums.ActType;
import uz.uzinfocom.app.modules.act.web.dto.request.act224.Act224RecommendationRequest;
import uz.uzinfocom.app.modules.act.web.dto.request.embedded.InstitutionRequest;

import java.util.List;

@Schema(description = "Акт 224 — далолатнома по проверке соблюдения санитарных требований.")
public record Act224Request(
        InstitutionRequest institution,

        Integer tin,
        @Size(max = 255) String institutionName,
        @Size(max = 500) String institutionAddress,
        @Size(max = 255) String activityTypeCode,
        @Size(max = 255) String fullNameOfEpidStaff,
        @Size(max = 255) String positionOfEpidStaff,
        @Size(max = 255) String fullNameOfParticipantEpid,
        @Size(max = 255) String positionOfParticipantEpid,
        @Size(max = 255) String nameOfInstitution,
        @Size(max = 500) String addressOfInstitution,
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
