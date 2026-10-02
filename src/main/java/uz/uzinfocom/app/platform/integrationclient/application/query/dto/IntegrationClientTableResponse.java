package uz.uzinfocom.app.platform.integrationclient.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.platform.integrationclient.domain.IntegrationAuthType;

import java.time.Instant;

@Schema(description = "Краткая информация об интеграционном клиенте для табличного списка.")
public record IntegrationClientTableResponse(
        Long id,
        String clientId,
        String name,
        IntegrationAuthType authType,
        String sourceKey,
        Long organizationId,
        String organizationName,
        boolean active,

        @Schema(description = "Дата и время последнего успешного обмена токена этим клиентом.", nullable = true)
        Instant lastUsedAt
) {
}
