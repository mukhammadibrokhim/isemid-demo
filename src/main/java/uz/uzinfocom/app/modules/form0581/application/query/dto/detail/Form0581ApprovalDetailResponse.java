package uz.uzinfocom.app.modules.form0581.application.query.dto.detail;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Сведения об утверждении формы №058-1.")
public record Form0581ApprovalDetailResponse(
        @Schema(description = "Идентификатор пользователя, утвердившего форму.")
        Long approvedBy,

        @Schema(description = "Идентификатор организации, утвердившей форму.")
        Long approvedOrganizationId,

        @Schema(description = "Дата и время утверждения формы.")
        Instant approvedAt,

        @Schema(description = "ФИО пользователя, утвердившего форму.")
        String approvedFullName,

        @Schema(description = "UUID организации, утвердившей форму.")
        UUID approvedOrgUuid
) {
}
