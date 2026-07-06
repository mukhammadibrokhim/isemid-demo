package uz.uzinfocom.app.modules.form058.application.query.dto.detail;

import uz.uzinfocom.app.modules.form058.domain.enums.FormStatus;
import uz.uzinfocom.app.modules.patient.application.query.dto.detail.PatientDetailResponse;
import uz.uzinfocom.app.platform.iam.application.shared.dto.AuditResponse;

import java.util.UUID;

public record Form058DetailResponse(
        Long id,
        UUID uuid,

        FormStatus status,
        String source,

        Long senderOrganizationId,
        Long receiverOrganizationId,

        Form058DiagnosisDetailResponse diagnosisInfo,
        Form058ClinicalDetailResponse clinicalInfo,
        Form058DateDetailResponse dateInfo,
        Form058LocationDetailResponse location,
        Form058EpidemicDetailResponse epidemicInfo,
        Form058ReportDetailResponse reportInfo,

        Boolean hasLinkedCards,
        Long assignedCardId,

        Form058CancellationDetailResponse cancellationInfo,
        Form058ApprovalDetailResponse approvalInfo,
        Form058DeleteDetailResponse deleteInfo,

        PatientDetailResponse patient,

        AuditResponse audit
) {
}