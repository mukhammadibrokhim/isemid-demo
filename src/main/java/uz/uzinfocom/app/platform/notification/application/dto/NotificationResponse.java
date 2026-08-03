package uz.uzinfocom.app.platform.notification.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.platform.audit.domain.AuditEntityType;
import uz.uzinfocom.app.platform.notification.domain.NotificationType;

import java.time.Instant;

@Schema(description = "Уведомление текущего пользователя.")
public record NotificationResponse(
        @Schema(description = "Идентификатор уведомления.")
        Long id,

        @Schema(description = "Тип уведомления.")
        NotificationType type,

        @Schema(description = "Тип связанной сущности.")
        AuditEntityType entityType,

        @Schema(description = "Идентификатор связанной сущности.")
        Long entityId,

        @Schema(description = "Идентификатор организации, к которой относится уведомление.")
        Long organizationId,

        @Schema(description = "Текст уведомления, локализованный под текущего пользователя.")
        String message,

        @Schema(description = "Признак прочтения.")
        boolean read,

        @Schema(description = "Дата и время прочтения.")
        Instant readAt,

        @Schema(description = "Дата и время события.")
        Instant occurredAt
) {
}
