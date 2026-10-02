package uz.uzinfocom.app.platform.integrationclient.application.command.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import uz.uzinfocom.app.platform.integrationclient.domain.IntegrationAuthType;

import java.util.List;

@Schema(description = "Учётные данные вновь зарегистрированного интеграционного клиента. Ровно одно из "
        + "clientSecret/apiKey/basicAuthSecret заполнено, в зависимости от authType (null для "
        + "IP_ALLOWLIST) — возвращается единственный раз, повторно получить будет нельзя.")
public record IntegrationClientCreateResponse(
        Long id,
        String clientId,

        IntegrationAuthType authType,

        @Schema(description = "Секрет клиента для authType=CLIENT_CREDENTIALS. Открытым текстом, только "
                + "один раз, при создании.")
        String clientSecret,

        @Schema(description = "Ключ для authType=API_KEY — передаётся в заголовке X-Api-Key. Открытым "
                + "текстом, только один раз, при создании.")
        String apiKey,

        @Schema(description = "Пароль для authType=BASIC_AUTH — логин это clientId. Открытым текстом, "
                + "только один раз, при создании.")
        String basicAuthSecret,

        String name,

        @Schema(description = "Сегмент URL этого клиента, например /integration/dmed/form-058.")
        String sourceKey,

        Long organizationId,
        List<String> scopes,

        @Schema(description = "IPv4-адреса/CIDR-блоки, с которых разрешено обращаться клиенту. Пусто — "
                + "ограничений нет.")
        List<String> allowedIps
) {
}
