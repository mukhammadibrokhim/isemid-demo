package uz.uzinfocom.app.integration.inbound.oauth.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Токен доступа для интеграционного клиента.")
public record IntegrationTokenResponse(

        @Schema(description = "Токен доступа (JWT) для последующих запросов к интеграционному API.")
        String accessToken,

        @Schema(description = "Тип токена.", example = "Bearer")
        String tokenType,

        @Schema(description = "Срок действия токена в секундах.")
        long expiresIn,

        @Schema(description = "Права доступа, предоставленные токеном (через пробел).")
        String scope
) {
}
