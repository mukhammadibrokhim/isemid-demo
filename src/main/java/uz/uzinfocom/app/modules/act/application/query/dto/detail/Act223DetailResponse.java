package uz.uzinfocom.app.modules.act.application.query.dto.detail;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.modules.act.application.query.dto.detail.act223.Act223SampleResponse;
import uz.uzinfocom.app.modules.act.application.query.dto.detail.embedded.ActInstitutionResponse;
import uz.uzinfocom.app.modules.act.application.query.dto.detail.embedded.ConditionInfoResponse;
import uz.uzinfocom.app.modules.act.application.query.dto.detail.embedded.EmployeeInfoResponse;
import uz.uzinfocom.app.modules.act.application.query.dto.detail.embedded.PackageTypeInfoResponse;
import uz.uzinfocom.app.modules.act.application.query.dto.detail.embedded.PurposeResponse;
import uz.uzinfocom.app.modules.act.domain.enums.ActStatus;
import uz.uzinfocom.app.modules.act.domain.enums.ActType;
import uz.uzinfocom.app.modules.card.application.query.dto.CardMiniResponse;
import uz.uzinfocom.app.platform.iam.application.shared.dto.AuditResponse;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Акт 223 — далолатнома по отбору проб (полные сведения).")
public record Act223DetailResponse(
        Long id,
        ActType type,
        ActStatus status,
        CardMiniResponse card,
        Long assignedById,
        String resultComment,
        ActInstitutionResponse institution,

        Long actNumber,
        String supportingDocumentsForSampling,
        String goal,
        String activityTypeCode,
        EmployeeInfoResponse sampler,
        EmployeeInfoResponse participant,
        PurposeResponse purpose,
        LocalDateTime sampleTakenDateTime,
        LocalDateTime deliveredDateTime,
        ConditionInfoResponse specialCondition,
        ConditionInfoResponse storageAndDeliveryCondition,
        Long lisOrganizationId,
        String laboratoryAddress,
        PackageTypeInfoResponse packageTypeInfo,
        String additionalInfo,
        List<Act223SampleResponse> samples,

        AuditResponse audit
) implements ActDetailResponse {
}
