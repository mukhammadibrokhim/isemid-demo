package uz.uzinfocom.app.platform.auth.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Refresh-токен для обновления access-токена без повторного прохождения "
        + "authorization_code + PKCE флоу.")
public record RefreshTokenRequest(

        @Schema(description = "Refresh-токен, полученный в поле refreshToken предыдущего успешного "
                + "ответа /v1/auth/login/{provider} или /v1/auth/refresh/{provider}.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String refreshToken
) {
}
