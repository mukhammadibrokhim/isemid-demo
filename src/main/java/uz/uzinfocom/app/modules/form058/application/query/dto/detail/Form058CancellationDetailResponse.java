package uz.uzinfocom.app.modules.form058.application.query.dto.detail;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Сведения об аннулировании формы №058 (отправителем) или отклонении её приёма (получателем) — используют одно и то же поле причины.")
public record Form058CancellationDetailResponse(
        @Schema(description = "Причина аннулирования/отклонения формы.")
        String cancelReason,

        @Schema(description = "Идентификатор пользователя, закрывшего форму.")
        Long canceledBy,

        @Schema(description = "Дата и время аннулирования/отклонения формы.")
        Instant canceledAt
) {
}
