package uz.uzinfocom.app.platform.integrationclient.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.platform.integrationclient.application.command.IntegrationClientCommandService;
import uz.uzinfocom.app.platform.integrationclient.application.command.dto.IntegrationClientCreateRequest;
import uz.uzinfocom.app.platform.integrationclient.application.command.dto.IntegrationClientCreateResponse;
import uz.uzinfocom.app.platform.integrationclient.application.query.IntegrationClientQueryService;
import uz.uzinfocom.app.platform.integrationclient.application.query.dto.IntegrationClientResponse;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.ApiResponse;

import java.util.List;

@Tag(
        name = "Admin - Integration Clients",
        description = "API для регистрации и управления интеграционными клиентами — внешними системами, "
                + "которым разрешено напрямую отправлять данные (форма №058, №058-1 и т. д.) через "
                + "отдельный интеграционный API."
)
@RestController
@RequestMapping(ApiPaths.IntegrationClient.ROOT)
@RequiredArgsConstructor
public class IntegrationClientController {

    private final IntegrationClientQueryService integrationClientQueryService;
    private final IntegrationClientCommandService integrationClientCommandService;
    private final MessageResolver messageResolver;

    @Operation(
            summary = "Получить список интеграционных клиентов",
            description = "Возвращает все зарегистрированные интеграционные клиенты. Секреты не возвращаются."
    )
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<IntegrationClientResponse>> findAll() {
        return ApiResponse.success(messageResolver.resolve("common.success"), integrationClientQueryService.findAll());
    }

    @Operation(
            summary = "Получить интеграционного клиента по идентификатору",
            description = "Возвращает детальную информацию об интеграционном клиенте. Секрет не возвращается."
    )
    @GetMapping(ApiPaths.IntegrationClient.BY_ID)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<IntegrationClientResponse> getById(
            @Parameter(description = "Внутренний идентификатор клиента.", required = true)
            @PathVariable @Positive Long id
    ) {
        return ApiResponse.success(messageResolver.resolve("common.success"), integrationClientQueryService.getById(id));
    }

    @Operation(
            summary = "Зарегистрировать интеграционного клиента",
            description = "Создаёт нового интеграционного клиента, привязанного к одной организации. "
                    + "clientSecret возвращается в ответе только один раз — сохраните его сейчас."
    )
    @PostMapping
    @PreAuthorize("@adminAccessGuard.isAdmin()")
    public ApiResponse<IntegrationClientCreateResponse> create(
            @Valid @RequestBody IntegrationClientCreateRequest request
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.created"),
                integrationClientCommandService.create(request)
        );
    }

    @Operation(
            summary = "Отозвать интеграционного клиента",
            description = "Деактивирует клиента — новые токены он получить не сможет. Уже выданные "
                    + "токены остаются действительными до истечения их срока действия."
    )
    @PostMapping(ApiPaths.IntegrationClient.REVOKE)
    @PreAuthorize("@adminAccessGuard.isAdmin()")
    public ApiResponse<Void> revoke(
            @Parameter(description = "Внутренний идентификатор клиента.", required = true)
            @PathVariable @Positive Long id
    ) {
        integrationClientCommandService.revoke(id);
        return ApiResponse.success(messageResolver.resolve("common.updated"), null);
    }
}
