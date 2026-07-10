package uz.uzinfocom.app.modules.form058.application.query.dto.detail;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.modules.form058.domain.enums.FormStatus;
import uz.uzinfocom.app.modules.patient.application.query.dto.detail.PatientDetailResponse;
import uz.uzinfocom.app.platform.iam.application.shared.dto.AuditResponse;

import java.util.UUID;

@Schema(description = "Полные детальные сведения о форме №058.")
public record Form058DetailResponse(
        @Schema(description = "Идентификатор формы.")
        Long id,

        @Schema(description = "UUID формы.")
        UUID uuid,

        @Schema(description = "Текущий статус формы в её жизненном цикле.")
        FormStatus status,

        @Schema(description = "Источник поступления формы.")
        String source,

        @Schema(description = "Идентификатор организации-отправителя.")
        Long senderOrganizationId,

        @Schema(description = "Идентификатор организации-получателя.")
        Long receiverOrganizationId,

        @Schema(description = "Диагностические сведения.")
        Form058DiagnosisDetailResponse diagnosisInfo,

        @Schema(description = "Клинические сведения.")
        Form058ClinicalDetailResponse clinicalInfo,

        @Schema(description = "Ключевые даты.")
        Form058DateDetailResponse dateInfo,

        @Schema(description = "Географическое место выявления заболевания.")
        Form058LocationDetailResponse location,

        @Schema(description = "Эпидемиологические сведения.")
        Form058EpidemicDetailResponse epidemicInfo,

        @Schema(description = "Сведения об учёте/регистрации формы.")
        Form058ReportDetailResponse reportInfo,

        @Schema(description = "Признак наличия привязанных к форме карт.")
        Boolean hasLinkedCards,

        @Schema(description = "Идентификатор привязанной карты (устаревшее поле для одиночной привязки).")
        Long assignedCardId,

        @Schema(description = "Сведения об аннулировании/отклонении утверждения.")
        Form058CancellationDetailResponse cancellationInfo,

        @Schema(description = "Сведения об утверждении.")
        Form058ApprovalDetailResponse approvalInfo,

        @Schema(description = "Сведения об удалении.")
        Form058DeleteDetailResponse deleteInfo,

        @Schema(description = "Полные сведения о пациенте.")
        PatientDetailResponse patient,

        @Schema(description = "Аудит-сведения (кто и когда создал/изменил запись).")
        AuditResponse audit
) {
}
