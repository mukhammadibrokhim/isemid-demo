package uz.uzinfocom.app.platform.integrationclient.application.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.platform.integrationclient.domain.OutboundHttpMethod;
import uz.uzinfocom.app.platform.integrationclient.domain.OutboundWebhookAuthType;

@Schema(description = "Настройка исходящего webhook клиента. Секрет никогда не возвращается ни в каком "
        + "виде — только признак того, что он задан.")
public record IntegrationClientWebhookResponse(
        String callbackUrl,
        OutboundHttpMethod httpMethod,
        OutboundWebhookAuthType authType,
        String username,
        String headerName,
        boolean active,

        @Schema(description = "true — секрет сохранён (значение не раскрывается).")
        boolean secretConfigured
) {
}
