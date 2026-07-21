package uz.uzinfocom.app.integration.inbound.oauth.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Запрос на получение токена доступа для интеграционного клиента "
        + "(client_credentials grant).")
public record IntegrationTokenRequest(

        @Schema(description = "Идентификатор клиента, выданный при регистрации интеграции.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "{integration.token.client-id.required}")
        String clientId,

        @Schema(description = "Секрет клиента, выданный при регистрации интеграции.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "{integration.token.client-secret.required}")
        String clientSecret,

        @Schema(description = "Тип запрашиваемого гранта. Поддерживается только client_credentials.",
                example = "client_credentials", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "{integration.token.grant-type.required}")
        String grantType
) {
    private static final String CLIENT_CREDENTIALS_GRANT = "client_credentials";

    @Schema(hidden = true)
    @AssertTrue(message = "{integration.token.grant-type.unsupported}")
    public boolean isGrantTypeSupported() {
        return grantType == null || CLIENT_CREDENTIALS_GRANT.equals(grantType);
    }
}
