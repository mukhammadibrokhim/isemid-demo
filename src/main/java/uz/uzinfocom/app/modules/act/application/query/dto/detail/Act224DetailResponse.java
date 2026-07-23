package uz.uzinfocom.app.modules.act.application.query.dto.detail;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.modules.act.application.query.dto.detail.act224.Act224RecommendationResponse;
import uz.uzinfocom.app.modules.act.application.query.dto.detail.embedded.ActInstitutionResponse;
import uz.uzinfocom.app.modules.act.domain.enums.ActStatus;
import uz.uzinfocom.app.modules.act.domain.enums.ActType;
import uz.uzinfocom.app.modules.card.application.query.dto.CardMiniResponse;
import uz.uzinfocom.app.platform.iam.application.shared.dto.AuditResponse;

import java.util.List;

@Schema(description = "Акт 224 — далолатнома по проверке соблюдения санитарных требований (полные сведения).")
public record Act224DetailResponse(
        Long id,
        ActType type,
        ActStatus status,
        CardMiniResponse card,
        Long assignedById,
        String resultComment,
        ActInstitutionResponse institution,

        Integer tin,
        String institutionName,
        String institutionAddress,
        String activityTypeCode,
        String fullNameOfEpidStaff,
        String positionOfEpidStaff,
        String fullNameOfParticipantEpid,
        String positionOfParticipantEpid,
        String nameOfInstitution,
        String addressOfInstitution,
        String nameOfRegulatoryActs,
        String checkingFulfillmentOfRequirements,
        String fullNameOfParticipant,
        String additionalInfo,
        List<Act224RecommendationResponse> recommendations,

        AuditResponse audit
) implements ActDetailResponse {
}
