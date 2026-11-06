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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.orchestration.webhook.application.OutboundWebhookDispatchService;
import uz.uzinfocom.app.orchestration.webhook.application.query.OutboundWebhookDispatchQueryService;
import uz.uzinfocom.app.orchestration.webhook.application.query.dto.OutboundWebhookDispatchFilterRequest;
import uz.uzinfocom.app.orchestration.webhook.application.query.dto.OutboundWebhookDispatchResponse;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.ApiResponse;
import uz.uzinfocom.app.shared.dto.response.PagedResponse;
import uz.uzinfocom.app.shared.dto.response.PagedResponseAssembler;

/**
 * Dev-panel-only operational visibility for the outbound status-change
 * webhook queue - dispatch status, failure history, manual retry. No
 * admin-facing equivalent: unlike {@code IntegrationClientController}, this
 * is purely operational, not something an org admin configures, so it exists
 * only under {@code /v1/dev/**} (see {@code DevPanelSecurityConfig}). Reads
 * are open to any authenticated dev-panel account; the retry action requires
 * {@code ROLE_DEV_ADMIN}.
 */
@Tag(
        name = "Dev Panel - Outbound Webhook Dispatches",
        description = "Статус доставки исходящих status-change webhook-уведомлений интеграционным клиентам, "
                + "история попыток и ручной повтор."
)
@RestController
@RequestMapping(ApiPaths.Dev.ROOT)
@RequiredArgsConstructor
public class DevOutboundWebhookDispatchController {

    private final OutboundWebhookDispatchQueryService outboundWebhookDispatchQueryService;
    private final OutboundWebhookDispatchService outboundWebhookDispatchService;
    private final MessageResolver messageResolver;
    private final PagedResponseAssembler pagedResponseAssembler;

    @Operation(
            summary = "Получить список исходящих webhook-задач",
            description = "Возвращает постраничный список задач доставки исходящих webhook-уведомлений с "
                    + "фильтрацией по клиенту, типу сущности и статусу."
    )
    @GetMapping(ApiPaths.Dev.WEBHOOK_DISPATCHES)
    public PagedResponse<OutboundWebhookDispatchResponse> findAll(
            @ParameterObject @Valid @ModelAttribute OutboundWebhookDispatchFilterRequest request,
            HttpServletRequest httpRequest
    ) {
        Page<OutboundWebhookDispatchResponse> responses = outboundWebhookDispatchQueryService.findAll(request);
        return pagedResponseAssembler.toResponse(responses, messageResolver.resolve("common.success"), httpRequest);
    }

    @Operation(
            summary = "Получить исходящую webhook-задачу по идентификатору",
            description = "Возвращает детальную информацию о задаче доставки, включая текст последней ошибки."
    )
    @GetMapping(ApiPaths.Dev.WEBHOOK_DISPATCH_BY_ID)
    public ApiResponse<OutboundWebhookDispatchResponse> getById(
            @Parameter(description = "Внутренний идентификатор задачи.", required = true)
            @PathVariable @Positive Long id
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.success"), outboundWebhookDispatchQueryService.getById(id));
    }

    @Operation(
            summary = "Повторить доставку немедленно",
            description = "Сбрасывает задачу в PENDING с нулевым числом попыток и назначает немедленную "
                    + "повторную доставку — независимо от того, ожидала ли она очередной запланированной "
                    + "попытки или уже была исчерпана (EXHAUSTED)."
    )
    @PostMapping(ApiPaths.Dev.WEBHOOK_DISPATCH_RETRY)
    @PreAuthorize("hasRole('DEV_ADMIN')")
    public ApiResponse<Void> retryNow(
            @Parameter(description = "Внутренний идентификатор задачи.", required = true)
            @PathVariable @Positive Long id
    ) {
        outboundWebhookDispatchService.retryNow(id);
        return ApiResponse.success(messageResolver.resolve("common.updated"), null);
    }
}
