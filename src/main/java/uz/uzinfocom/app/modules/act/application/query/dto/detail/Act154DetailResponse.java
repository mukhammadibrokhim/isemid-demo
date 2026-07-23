package uz.uzinfocom.app.modules.act.application.query.dto.detail;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.modules.act.application.query.dto.detail.act154.Act154SampleResponse;
import uz.uzinfocom.app.modules.act.application.query.dto.detail.embedded.ActInstitutionResponse;
import uz.uzinfocom.app.modules.act.application.query.dto.detail.embedded.ConditionInfoResponse;
import uz.uzinfocom.app.modules.act.application.query.dto.detail.embedded.EmployeeInfoResponse;
import uz.uzinfocom.app.modules.act.application.query.dto.detail.embedded.PackageTypeInfoResponse;
import uz.uzinfocom.app.modules.act.application.query.dto.detail.embedded.PurposeResponse;
import uz.uzinfocom.app.modules.act.domain.enums.ActStatus;
import uz.uzinfocom.app.modules.act.domain.enums.ActType;
import uz.uzinfocom.app.modules.card.application.query.dto.CardMiniResponse;
import uz.uzinfocom.app.platform.iam.application.shared.dto.AuditResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Акт 154 — далолатнома по отбору проб (полные сведения).")
public record Act154DetailResponse(
        Long id,
        ActType type,
        ActStatus status,
        CardMiniResponse card,
        Long assignedById,
        String resultComment,
        ActInstitutionResponse institution,

        String title,
        Long actNumber,
        String activityTypeCode,
        LocalDateTime sampleTakenDateTime,
        LocalDateTime deliveredDateTime,
        String documentConfirmSampling,
        String goal,
        PurposeResponse purpose,
        EmployeeInfoResponse sampler,
        EmployeeInfoResponse participant,
        String manufacturingCompany,
        LocalDate manufactureDate,
        String docNumberOfTakenObject,
        ConditionInfoResponse specialCondition,
        ConditionInfoResponse storageAndDeliveryCondition,
        Long lisOrganizationId,
        String laboratoryAddress,
        PackageTypeInfoResponse packageTypeInfo,
        String additionalInfo,
        List<Act154SampleResponse> samples,

        AuditResponse audit
) implements ActDetailResponse {
}
