package uz.uzinfocom.app.platform.ssoproxy.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Токен доступа, полученный от провайдера аутентификации.")
public record LoginResponse(

        @Schema(description = "Токен доступа (JWT), выданный провайдером - используется как Bearer-токен "
                + "в последующих запросах к API.")
        String accessToken,

        @Schema(description = "Токен обновления, если провайдер его выдал.")
        String refreshToken,

        @Schema(description = "Тип токена.", example = "Bearer")
        String tokenType,

        @Schema(description = "Срок действия токена в секундах.")
        Long expiresIn,

        @Schema(description = "Права доступа, предоставленные токеном (через пробел).")
        String scope
) {
}
