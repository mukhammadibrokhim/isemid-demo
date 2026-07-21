package uz.uzinfocom.app.integration.inbound.oauth.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.app.integration.inbound.oauth.application.IntegrationTokenService;
import uz.uzinfocom.app.integration.inbound.oauth.web.dto.IntegrationTokenRequest;
import uz.uzinfocom.app.integration.inbound.oauth.web.dto.IntegrationTokenResponse;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;

@Tag(
        name = "Integration - OAuth",
        description = "Обмен client_id/client_secret зарегистрированного интеграционного клиента "
                + "на короткоживущий токен доступа."
)
@RestController
@RequestMapping(ApiPaths.Integration.OAUTH_TOKEN)
@RequiredArgsConstructor
public class IntegrationTokenController {

    private final IntegrationTokenService integrationTokenService;

    @Operation(
            summary = "Получить токен доступа",
            description = "Единственный публичный (без аутентификации) эндпоинт интеграционного API — "
                    + "обменивает client_id/client_secret на JWT для последующих запросов."
    )
    @PostMapping
    public IntegrationTokenResponse issueToken(@Valid @RequestBody IntegrationTokenRequest request) {
        return integrationTokenService.issueToken(request);
    }
}
