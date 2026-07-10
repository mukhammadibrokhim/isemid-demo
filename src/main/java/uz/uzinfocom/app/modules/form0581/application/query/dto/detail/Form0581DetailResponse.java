package uz.uzinfocom.app.modules.form0581.application.query.dto.detail;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.modules.form0581.domain.enums.Form0581Status;
import uz.uzinfocom.app.modules.patient.application.query.dto.detail.PatientDetailResponse;
import uz.uzinfocom.app.platform.iam.application.shared.dto.AuditResponse;

import java.util.List;
import java.util.UUID;

@Schema(description = "Полные детальные сведения о форме №058-1.")
public record Form0581DetailResponse(
        @Schema(description = "Идентификатор формы.")
        Long id,

        @Schema(description = "UUID формы.")
        UUID uuid,

        @Schema(description = "Текущий статус формы в её жизненном цикле.")
        Form0581Status status,

        @Schema(description = "Источник поступления формы.")
        String source,

        @Schema(description = "Идентификатор организации-отправителя.")
        Long senderOrganizationId,

        @Schema(description = "Идентификатор организации-получателя.")
        Long receiverOrganizationId,

        @Schema(description = "Диагностические сведения.")
        Form0581DiagnosisDetailResponse diagnosisInfo,

        @Schema(description = "Сведения о происшествии.")
        Form0581IncidentDetailResponse incidentInfo,

        @Schema(description = "Сведения о животном.")
        Form0581AnimalDetailResponse animalInfo,

        @Schema(description = "Сведения о владельце животного.")
        Form0581AnimalOwnerDetailResponse animalOwnerInfo,

        @Schema(description = "Признак того, что в этом же происшествии пострадали и другие лица.")
        Boolean otherPeopleInjured,

        @Schema(description = "Список иных пострадавших лиц.")
        List<Form0581OtherInjuredPersonDetailResponse> otherInjuredPeople,

        @Schema(description = "Сведения о госпитализации пациента.")
        Form0581HospitalizationDetailResponse hospitalizationInfo,

        @Schema(description = "Сведения об учёте/регистрации формы.")
        Form0581ReportDetailResponse reportInfo,

        @Schema(description = "Сведения об аннулировании/отклонении утверждения.")
        Form0581CancellationDetailResponse cancellationInfo,

        @Schema(description = "Сведения об утверждении.")
        Form0581ApprovalDetailResponse approvalInfo,

        @Schema(description = "Сведения об удалении.")
        Form0581DeleteDetailResponse deleteInfo,

        @Schema(description = "Полные сведения о пациенте.")
        PatientDetailResponse patient,

        @Schema(description = "Аудит-сведения (кто и когда создал/изменил запись).")
        AuditResponse audit
) {
}
