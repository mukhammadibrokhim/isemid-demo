package uz.uzinfocom.app.platform.integrationclient.application.command.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import uz.uzinfocom.app.platform.integrationclient.domain.OutboundHttpMethod;
import uz.uzinfocom.app.platform.integrationclient.domain.OutboundWebhookAuthType;

@Schema(description = "Настройка исходящего webhook интеграционного клиента — куда и как отправлять "
        + "уведомления об изменении статуса поданных им форм.")
public record IntegrationClientWebhookUpdateRequest(

        @Schema(description = "URL для отправки уведомлений. Обязателен и должен быть HTTPS, если active=true.",
                example = "https://partner.example.com/callbacks/isemid")
        String callbackUrl,

        @Schema(description = "HTTP-метод для отправки уведомления.")
        OutboundHttpMethod httpMethod,

        @Schema(description = "Схема аутентификации при обращении к callbackUrl.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "{integration-client.webhook.auth-type.required}")
        OutboundWebhookAuthType authType,

        @Schema(description = "Имя пользователя — только для authType=BASIC_AUTH.")
        String username,

        @Schema(description = "Имя заголовка — только для authType=API_KEY_HEADER.")
        String headerName,

        @Schema(description = "Секрет в открытом виде (пароль/токен/значение заголовка). Никогда не "
                + "возвращается обратно — только принимается при создании/смене. Если оставить пустым при "
                + "обновлении, ранее сохранённый секрет остаётся без изменений.")
        String secret,

        @Schema(description = "true — webhook включён и будет вызываться при изменении статуса; false — "
                + "отключён.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "{integration-client.webhook.active.required}")
        Boolean active
) {
}
