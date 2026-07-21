package uz.uzinfocom.app.platform.integrationclient.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.platform.iam.application.shared.dto.AuditResponse;

import java.time.Instant;
import java.util.List;

@Schema(description = "Сведения об интеграционном клиенте. Секрет никогда не возвращается после создания.")
public record IntegrationClientResponse(
        Long id,
        String clientId,
        String name,
        String sourceKey,
        Long organizationId,
        String organizationName,
        List<String> scopes,
        boolean active,

        @Schema(description = "Дата и время последнего успешного обмена токена этим клиентом.", nullable = true)
        Instant lastUsedAt,

        AuditResponse audit
) {
}
