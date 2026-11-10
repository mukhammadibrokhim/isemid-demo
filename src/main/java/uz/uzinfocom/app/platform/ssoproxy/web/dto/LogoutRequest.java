package uz.uzinfocom.app.platform.ssoproxy.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Токены, которые нужно аннулировать при выходе из системы через провайдера "
        + "(path-параметр {provider}). Оба поля попадают в локальный чёрный список; хотя бы одно из них "
        + "должно быть передано.")
public record LogoutRequest(

        @Schema(description = "Access-токен, который нужно занести в чёрный список и (если у провайдера "
                + "настроен revoke-url) отозвать на его стороне.")
        String accessToken,

        @Schema(description = "Refresh-токен, который нужно занести в чёрный список и (если у провайдера "
                + "настроен revoke-url) отозвать на его стороне. Без этого /v1/auth/refresh/{provider} тем "
                + "же refresh-токеном остаётся рабочим после выхода из системы - см. TokenBlacklistService.")
        String refreshToken
) {
}
