package uz.uzinfocom.app.platform.iam.application.shared.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Информация об аудите записи.")
public record AuditResponse(

        @Schema(description = "Дата и время создания записи.")
        Instant createdAt,

        @Schema(description = "Пользователь, создавший запись.", nullable = true)
        AuditUserResponse createdBy,

        @Schema(description = "Дата и время последнего обновления записи.")
        Instant updatedAt,

        @Schema(description = "Пользователь, последним обновивший запись.", nullable = true)
        AuditUserResponse updatedBy
) {
}