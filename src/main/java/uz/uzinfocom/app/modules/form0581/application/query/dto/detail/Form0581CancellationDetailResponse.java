package uz.uzinfocom.app.modules.form0581.application.query.dto.detail;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Сведения об аннулировании/отклонении утверждения формы №058-1.")
public record Form0581CancellationDetailResponse(
        @Schema(description = "Причина аннулирования формы.")
        String cancelReason,

        @Schema(description = "Идентификатор пользователя, аннулировавшего форму.")
        Long canceledBy,

        @Schema(description = "Дата и время аннулирования формы.")
        Instant canceledAt,

        @Schema(description = "Причина отклонения утверждения формы.")
        String notApprovedReason
) {
}
