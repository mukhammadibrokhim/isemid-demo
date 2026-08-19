package uz.uzinfocom.app.platform.ssoproxy.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.app.platform.ssoproxy.application.LoginService;
import uz.uzinfocom.app.platform.ssoproxy.web.dto.LoginRequest;
import uz.uzinfocom.app.platform.ssoproxy.web.dto.LoginResponse;
import uz.uzinfocom.app.platform.ssoproxy.web.dto.LogoutRequest;
import uz.uzinfocom.app.platform.ssoproxy.web.dto.RefreshTokenRequest;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;

@Tag(
        name = "Auth",
        description = "Обмен учётных данных пользователя на токен доступа через внешнего провайдера "
                + "аутентификации (SSO, DHP и т.п.), независимо от того, какой OAuth2-грант использует "
                + "конкретный провайдер. Публичные (без Bearer-токена) эндпоинты - см. RouteAccessPolicy."
)
@RestController
@RequestMapping(ApiPaths.Auth.ROOT)
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;

    @Operation(
            summary = "Войти через провайдера",
            description = "Обменивает code/codeVerifier/redirectUri (authorization_code + PKCE) на токен "
                    + "доступа, вызывая token-эндпоинт указанного провайдера (path-параметр {provider}, "
                    + "например \"sso-web\" или \"dhp-web\") с client_id/client_secret, настроенными на "
                    + "бэкенде. Новый провайдер, говорящий на уже поддержанном гранте, добавляется "
                    + "конфигурацией, без изменения этого контроллера."
    )
    @PostMapping(ApiPaths.Auth.LOGIN)
    public LoginResponse login(
            @PathVariable(ApiPaths.Auth.PROVIDER) String provider,
            @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {
        return loginService.login(provider, request, httpRequest.getRemoteAddr(), httpRequest.getHeader("User-Agent"));
    }

    @Operation(
            summary = "Обновить токен через провайдера",
            description = "Обменивает refreshToken на новый access-токен (grant_type=refresh_token), "
                    + "вызывая тот же token-эндпоинт указанного провайдера с тем же client_id/client_secret, "
                    + "что и /login. Провайдер может отдать новый refreshToken вместо старого (ротация) - "
                    + "фронтенду следует всегда сохранять то значение refreshToken, что пришло в последнем "
                    + "ответе."
    )
    @PostMapping(ApiPaths.Auth.REFRESH)
    public LoginResponse refresh(
            @PathVariable(ApiPaths.Auth.PROVIDER) String provider,
            @RequestBody RefreshTokenRequest request
    ) {
        return loginService.refresh(provider, request);
    }

    @Operation(
            summary = "Выйти через провайдера",
            description = "Заносит переданные access/refresh-токены в локальный чёрный список - "
                    + "проверяется на каждом аутентифицированном запросе и в /refresh - и, если у "
                    + "провайдера (path-параметр {provider}) настроен revoke-url/logout-url, лучшим "
                    + "образом отзывает их и там же. Чёрный список - это то, что реально прекращает "
                    + "действие токена на этом бэкенде: и SSO, и DHP валидируются локально по подписи, "
                    + "поэтому один только вызов provider logout/revoke не помешал бы уже выданному "
                    + "токену остаться рабочим здесь до истечения его срока."
    )
    @PostMapping(ApiPaths.Auth.LOGOUT)
    public void logout(
            @PathVariable(ApiPaths.Auth.PROVIDER) String provider,
            @RequestBody LogoutRequest request
    ) {
        loginService.logout(provider, request);
    }
}
