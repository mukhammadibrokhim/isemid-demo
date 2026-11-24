package uz.uzinfocom.app.orchestration.webhook.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.platform.audit.domain.AuditEntityType;
import uz.uzinfocom.app.orchestration.webhook.domain.OutboundWebhookDispatchStatus;

import java.time.Instant;

@Schema(description = "Одна попытка (и история повторов) доставки исходящего webhook-уведомления.")
public record OutboundWebhookDispatchResponse(
        Long id,
        Long integrationClientId,
        AuditEntityType entityType,
        Long entityId,
        String oldStatus,
        String newStatus,
        OutboundWebhookDispatchStatus status,
        int attemptCount,
        Instant nextAttemptAt,
        Instant lastAttemptedAt,

        @Schema(description = "Текст последней ошибки доставки, если была.", nullable = true)
        String lastError,

        @Schema(description = "HTTP-статус последней попытки, если была.", nullable = true)
        Integer lastHttpStatus,

        Instant createdAt,
        Instant updatedAt
) {
}
