package uz.uzinfocom.app.platform.integrationclient.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.platform.integrationclient.application.command.IntegrationClientCommandService;
import uz.uzinfocom.app.platform.integrationclient.application.command.dto.IntegrationClientAllowedIpsUpdateRequest;
import uz.uzinfocom.app.platform.integrationclient.application.command.dto.IntegrationClientCreateRequest;
import uz.uzinfocom.app.platform.integrationclient.application.command.dto.IntegrationClientCreateResponse;
import uz.uzinfocom.app.platform.integrationclient.application.command.dto.IntegrationClientUpdateRequest;
import uz.uzinfocom.app.platform.integrationclient.application.command.dto.IntegrationClientWebhookUpdateRequest;
import uz.uzinfocom.app.platform.integrationclient.application.query.IntegrationClientQueryService;
import uz.uzinfocom.app.platform.integrationclient.application.query.dto.IntegrationClientFilterRequest;
import uz.uzinfocom.app.platform.integrationclient.application.query.dto.IntegrationClientResponse;
import uz.uzinfocom.app.platform.integrationclient.application.query.dto.IntegrationClientSourceKeyLookupRequest;
import uz.uzinfocom.app.platform.integrationclient.application.query.dto.IntegrationClientTableResponse;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.ApiResponse;
import uz.uzinfocom.app.shared.dto.response.PagedResponse;
import uz.uzinfocom.app.shared.dto.response.PagedResponseAssembler;

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
    private final PagedResponseAssembler pagedResponseAssembler;

    @Operation(
            summary = "Получить список интеграционных клиентов",
            description = "Возвращает постраничный список зарегистрированных интеграционных клиентов с "
                    + "фильтрацией. Возвращаются только поля, нужные для таблицы — полная информация "
                    + "доступна через GET .../{id}. Секреты не возвращаются."
    )
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public PagedResponse<IntegrationClientTableResponse> findAll(
            @ParameterObject @Valid @ModelAttribute IntegrationClientFilterRequest request,
            HttpServletRequest httpRequest
    ) {
        Page<IntegrationClientTableResponse> responses = integrationClientQueryService.findAll(request);
        return pagedResponseAssembler.toResponse(responses, messageResolver.resolve("common.success"), httpRequest);
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
            summary = "Получить source-ключи активных клиентов для выбора",
            description = "Возвращает краткий отсортированный список уникальных sourceKey активных "
                    + "интеграционных клиентов — для заполнения параметра {source} эндпоинтов "
                    + "/integration/v1/{source}/** (см. Integration - Form 058 и т. д.) значением из "
                    + "реального списка. Предназначен для выпадающих списков: результат ограничен "
                    + "параметром limit (по умолчанию 20, максимум 50) — при большем числе "
                    + "зарегистрированных источников используйте search для сужения выборки."
    )
    @GetMapping(ApiPaths.IntegrationClient.SOURCE_KEYS)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<String>> getActiveSourceKeys(
            @ParameterObject @Valid @ModelAttribute IntegrationClientSourceKeyLookupRequest request
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                integrationClientQueryService.listActiveSourceKeys(request)
        );
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
            summary = "Обновить интеграционного клиента",
            description = "Обновляет название, права доступа и активность клиента. clientId, authType, "
                    + "sourceKey, organizationId и учётные данные не изменяются — для их смены отзовите "
                    + "клиента и зарегистрируйте нового. В отличие от отзыва, active обратим — можно "
                    + "снова включить клиента, выставив active=true."
    )
    @PutMapping(ApiPaths.IntegrationClient.BY_ID)
    @PreAuthorize("@adminAccessGuard.isAdmin()")
    public ApiResponse<Void> update(
            @Parameter(description = "Внутренний идентификатор клиента.", required = true)
            @PathVariable @Positive Long id,
            @Valid @RequestBody IntegrationClientUpdateRequest request
    ) {
        integrationClientCommandService.update(id, request);
        return ApiResponse.success(messageResolver.resolve("common.updated"), null);
    }

    @Operation(
            summary = "Отозвать интеграционного клиента",
            description = "Деактивирует клиента немедленно — на любом следующем запросе, любым способом "
                    + "аутентификации (Bearer JWT, API-ключ, Basic Auth, IP allow-list), клиент будет "
                    + "отклонён, даже если у него уже был выдан ещё не истёкший токен. Необратимо через "
                    + "этот эндпоинт — чтобы снова включить клиента, используйте PUT .../{id} с active=true."
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

    @Operation(
            summary = "Обновить список разрешённых IP-адресов",
            description = "Заменяет список IPv4-адресов/CIDR-блоков, с которых разрешено обращаться этому "
                    + "клиенту. Пустой список или null снимает ограничение."
    )
    @PutMapping(ApiPaths.IntegrationClient.ALLOWED_IPS)
    @PreAuthorize("@adminAccessGuard.isAdmin()")
    public ApiResponse<Void> updateAllowedIps(
            @Parameter(description = "Внутренний идентификатор клиента.", required = true)
            @PathVariable @Positive Long id,
            @Valid @RequestBody IntegrationClientAllowedIpsUpdateRequest request
    ) {
        integrationClientCommandService.updateAllowedIps(id, request);
        return ApiResponse.success(messageResolver.resolve("common.updated"), null);
    }

    @Operation(
            summary = "Настроить исходящий webhook клиента",
            description = "Задаёт URL, HTTP-метод и схему аутентификации, по которым клиент получает "
                    + "уведомления об изменении статуса поданных им форм. При active=true URL обязателен и "
                    + "должен быть HTTPS; секрет возвращается только признаком наличия, никогда — значением."
    )
    @PutMapping(ApiPaths.IntegrationClient.WEBHOOK)
    @PreAuthorize("@adminAccessGuard.isAdmin()")
    public ApiResponse<Void> updateWebhook(
            @Parameter(description = "Внутренний идентификатор клиента.", required = true)
            @PathVariable @Positive Long id,
            @Valid @RequestBody IntegrationClientWebhookUpdateRequest request
    ) {
        integrationClientCommandService.updateWebhook(id, request);
        return ApiResponse.success(messageResolver.resolve("common.updated"), null);
    }
}
