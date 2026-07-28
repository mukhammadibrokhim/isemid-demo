package uz.uzinfocom.app.platform.auth.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * One request shape for every login provider, regardless of which OAuth2
 * grant it speaks - which fields are actually required depends on the
 * resolved provider's grant type (see {@code LoginProvider} implementations),
 * not on this DTO. Deliberately no {@code @NotBlank} here: only
 * authorization_code providers exist today (all three fields required), but
 * a future grant with a different field set (e.g. password, or
 * client_credentials) would otherwise be unable to share this DTO without
 * every other grant's requests failing validation for fields it doesn't use.
 */
@Schema(description = "Данные для обмена на токен доступа через провайдера (path-параметр {provider}). "
        + "Набор обязательных полей зависит от гранта, который использует конкретный провайдер.")
public record LoginRequest(

        @Schema(description = "Код авторизации, полученный после редиректа со страницы логина провайдера.")
        String code,

        @Schema(description = "PKCE code_verifier, использованный при редиректе на страницу логина провайдера.")
        String codeVerifier,

        @Schema(description = "redirect_uri, использованный при редиректе на страницу логина провайдера - "
                + "должен точно совпадать с тем значением.")
        String redirectUri
) {
}
