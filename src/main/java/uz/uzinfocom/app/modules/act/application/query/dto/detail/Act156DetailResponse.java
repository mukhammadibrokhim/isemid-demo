package uz.uzinfocom.app.modules.act.application.query.dto.detail;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.modules.act.application.query.dto.detail.act156.Act156GroupDetailResponse;
import uz.uzinfocom.app.modules.act.application.query.dto.detail.act156.Act156KitchenUtensilResponse;
import uz.uzinfocom.app.modules.act.application.query.dto.detail.embedded.ActInstitutionResponse;
import uz.uzinfocom.app.modules.act.domain.enums.ActStatus;
import uz.uzinfocom.app.modules.act.domain.enums.ActType;
import uz.uzinfocom.app.modules.card.application.query.dto.CardMiniResponse;
import uz.uzinfocom.app.platform.iam.application.shared.dto.AuditResponse;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Акт 156 — далолатнома по проверке пищеблока (полные сведения).")
public record Act156DetailResponse(
        Long id,
        ActType type,
        ActStatus status,
        CardMiniResponse card,
        Long assignedById,
        String resultComment,
        ActInstitutionResponse institution,

        String title,
        Integer tin,
        String institutionName,
        String institutionAddress,
        String activityTypeCode,
        LocalDateTime sampleTakenTime,
        Long lisOrganizationId,
        String laboratoryAddress,
        LocalDateTime sampleDeliveryTime,
        String fullNameOfSampler,
        String positionOfSampler,
        String fullNameOfObjectRepresentative,
        String positionOfObjectRepresentative,
        List<Act156KitchenUtensilResponse> kitchenUtensils,
        List<Act156GroupDetailResponse> groupDetails,

        AuditResponse audit
) implements ActDetailResponse {
}
