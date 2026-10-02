package uz.uzinfocom.app.platform.devpanel.web;

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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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

/**
 * Dev-panel mirror of {@code IntegrationClientController} - same command/query
 * services and the same DB table, reached via the {@code DevUser} Basic-Auth
 * chain instead of an SSO admin token. Reads are open to any authenticated
 * dev-panel account (see {@code DevPanelSecurityConfig}); writes require
 * {@code ROLE_DEV_ADMIN} - there is no true delete here (revoke is reversible
 * via {@code PUT .../{id}} with {@code active=true}), so nothing is restricted
 * to {@code ROLE_DEV_SUPER_ADMIN}.
 */
@Tag(
        name = "Dev Panel - Integration Clients",
        description = "Registration and management of integration clients — same table as "
                + "/v1/admin/integration-clients — reachable from the developer monitoring panel."
)
@RestController
@RequestMapping(ApiPaths.Dev.ROOT)
@RequiredArgsConstructor
public class DevIntegrationClientController {

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
    @GetMapping(ApiPaths.Dev.INTEGRATION_CLIENTS)
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
    @GetMapping(ApiPaths.Dev.INTEGRATION_CLIENT_BY_ID)
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
    @GetMapping(ApiPaths.Dev.INTEGRATION_CLIENT_SOURCE_KEYS)
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
    @PostMapping(ApiPaths.Dev.INTEGRATION_CLIENTS)
    @PreAuthorize("hasRole('DEV_ADMIN')")
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
    @PutMapping(ApiPaths.Dev.INTEGRATION_CLIENT_BY_ID)
    @PreAuthorize("hasRole('DEV_ADMIN')")
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
    @PostMapping(ApiPaths.Dev.INTEGRATION_CLIENT_REVOKE)
    @PreAuthorize("hasRole('DEV_ADMIN')")
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
    @PutMapping(ApiPaths.Dev.INTEGRATION_CLIENT_ALLOWED_IPS)
    @PreAuthorize("hasRole('DEV_ADMIN')")
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
    @PutMapping(ApiPaths.Dev.INTEGRATION_CLIENT_WEBHOOK)
    @PreAuthorize("hasRole('DEV_ADMIN')")
    public ApiResponse<Void> updateWebhook(
            @Parameter(description = "Внутренний идентификатор клиента.", required = true)
            @PathVariable @Positive Long id,
            @Valid @RequestBody IntegrationClientWebhookUpdateRequest request
    ) {
        integrationClientCommandService.updateWebhook(id, request);
        return ApiResponse.success(messageResolver.resolve("common.updated"), null);
    }
}
