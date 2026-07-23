package uz.uzinfocom.app.modules.act.application.query.dto.detail;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.modules.act.application.query.dto.detail.act155.Act155SampleResponse;
import uz.uzinfocom.app.modules.act.application.query.dto.detail.embedded.ActInstitutionResponse;
import uz.uzinfocom.app.modules.act.domain.enums.ActStatus;
import uz.uzinfocom.app.modules.act.domain.enums.ActType;
import uz.uzinfocom.app.modules.card.application.query.dto.CardMiniResponse;
import uz.uzinfocom.app.platform.iam.application.shared.dto.AuditResponse;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "Акт 155 — далолатнома по отбору проб (полные сведения).")
public record Act155DetailResponse(
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
        LocalDate selectedDate,
        String samplerFullName,
        String samplerPosition,
        String objectRepresentativeFullName,
        String objectRepresentativePosition,
        String additionalInfo,
        List<Act155SampleResponse> samples,

        AuditResponse audit
) implements ActDetailResponse {
}
