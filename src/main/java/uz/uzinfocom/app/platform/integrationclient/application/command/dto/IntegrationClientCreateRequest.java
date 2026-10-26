package uz.uzinfocom.app.platform.integrationclient.application.command.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import uz.uzinfocom.app.platform.integrationclient.domain.IntegrationAuthType;
import uz.uzinfocom.app.platform.integrationclient.domain.IntegrationScope;

import java.util.List;

@Schema(description = "Запрос на регистрацию нового интеграционного клиента.")
public record IntegrationClientCreateRequest(

        @Schema(description = "Человекочитаемое название интеграции (например, название внешней системы).",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "{integration-client.name.required}")
        @Size(max = 255, message = "{integration-client.name.size}")
        String name,

        @Schema(description = "Способ аутентификации клиента. CLIENT_CREDENTIALS — clientId/clientSecret "
                + "обмениваются на Bearer JWT; API_KEY — статический ключ в заголовке X-Api-Key; "
                + "BASIC_AUTH — HTTP Basic (clientId как логин); IP_ALLOWLIST — без каких-либо учётных "
                + "данных, клиент идентифицируется только по allowedIps (в этом случае поле allowedIps "
                + "обязательно и не может быть пустым); EXTERNAL_TOKEN — M2M-токен (client_credentials) SSO/DHP, "
                + "клиент сопоставляется по providerKey + externalClientId.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "{integration-client.auth-type.required}")
        IntegrationAuthType authType,

        @Schema(description = "Ключ маршрута — сегмент URL, по которому этот клиент вызывает интеграционный "
                + "API, например \"dmed\" для POST /integration/dmed/form-058. Уникален для каждого клиента.",
                example = "dmed", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "{integration-client.source-key.required}")
        @Size(max = 64, message = "{integration-client.source-key.size}")
        @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_-]*$", message = "{integration-client.source-key.pattern}")
        String sourceKey,

        @Schema(description = "Права доступа, предоставляемые клиенту.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty(message = "{integration-client.scopes.required}")
        List<IntegrationScope> scopes,

        @Schema(description = "IPv4-адреса или CIDR-блоки, с которых разрешено обращаться этому клиенту "
                + "(например, \"10.0.5.0/24\"). Для authType=IP_ALLOWLIST обязателен и не может быть пустым "
                + "— это единственный способ идентифицировать такого клиента. Для остальных типов — "
                + "дополнительное ограничение; не указано или пусто — ограничений нет.",
                example = "10.0.5.0/24", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        List<@NotBlank(message = "{integration-client.allowed-ips.invalid}") String> allowedIps,

        @Schema(description = "Только для authType=EXTERNAL_TOKEN: ключ провайдера авторизации (например, sso или dhp), "
                + "выпускающего M2M-токен. Организации клиента берутся из справочника этого провайдера.",
                example = "sso", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Size(max = 50, message = "{integration-client.provider-key.size}")
        String providerKey,

        @Schema(description = "Только для authType=EXTERNAL_TOKEN: идентификатор клиента из M2M-токена "
                + "(claim client_id, azp или sub).", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Size(max = 255, message = "{integration-client.external-client-id.size}")
        String externalClientId
) {
}
