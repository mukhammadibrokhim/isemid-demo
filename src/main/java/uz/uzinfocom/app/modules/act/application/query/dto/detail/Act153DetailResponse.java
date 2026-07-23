package uz.uzinfocom.app.modules.act.application.query.dto.detail;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.modules.act.application.query.dto.detail.act153.Act153SampleResponse;
import uz.uzinfocom.app.modules.act.application.query.dto.detail.embedded.ActInstitutionResponse;
import uz.uzinfocom.app.modules.act.application.query.dto.detail.embedded.ConditionInfoResponse;
import uz.uzinfocom.app.modules.act.application.query.dto.detail.embedded.ConservationTypeInfoResponse;
import uz.uzinfocom.app.modules.act.application.query.dto.detail.embedded.EmployeeInfoResponse;
import uz.uzinfocom.app.modules.act.application.query.dto.detail.embedded.PackageTypeInfoResponse;
import uz.uzinfocom.app.modules.act.application.query.dto.detail.embedded.PurposeResponse;
import uz.uzinfocom.app.modules.act.domain.enums.ActStatus;
import uz.uzinfocom.app.modules.act.domain.enums.ActType;
import uz.uzinfocom.app.modules.card.application.query.dto.CardMiniResponse;
import uz.uzinfocom.app.platform.iam.application.shared.dto.AuditResponse;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Акт 153 — далолатнома по отбору проб (полные сведения).")
public record Act153DetailResponse(
        Long id,
        ActType type,
        ActStatus status,
        CardMiniResponse card,
        Long assignedById,
        String resultComment,
        ActInstitutionResponse institution,

        Long actNumber,
        String activityTypeCode,
        String samplingDocuments,
        String goal,
        LocalDateTime sampleTakenDateTime,
        LocalDateTime deliveredDateTime,
        PurposeResponse purpose,
        EmployeeInfoResponse sampler,
        EmployeeInfoResponse participant,
        ConditionInfoResponse specialCondition,
        ConditionInfoResponse storageAndDeliveryCondition,
        Long lisOrganizationId,
        String laboratoryAddress,
        PackageTypeInfoResponse packageTypeInfo,
        ConservationTypeInfoResponse conservationTypeInfo,
        String additionalInfo,
        List<Act153SampleResponse> samples,

        AuditResponse audit
) implements ActDetailResponse {
}
