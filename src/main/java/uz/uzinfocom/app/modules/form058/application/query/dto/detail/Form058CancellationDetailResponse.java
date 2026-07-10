package uz.uzinfocom.app.modules.form058.application.query.dto.detail;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Сведения об аннулировании/отклонении утверждения формы №058.")
public record Form058CancellationDetailResponse(
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
