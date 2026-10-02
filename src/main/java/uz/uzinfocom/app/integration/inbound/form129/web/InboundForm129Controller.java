package uz.uzinfocom.app.integration.inbound.form129.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.app.integration.inbound.common.web.InboundCallerContext;
import uz.uzinfocom.app.integration.inbound.common.web.InboundIntegrationClientResolver;
import uz.uzinfocom.app.integration.inbound.common.web.InboundSourceResolver;
import uz.uzinfocom.app.integration.inbound.common.web.dto.InboundFormSubmissionResponse;
import uz.uzinfocom.app.integration.inbound.form129.application.InboundForm129Mapper;
import uz.uzinfocom.app.modules.form129.application.command.create.CreateForm129Command;
import uz.uzinfocom.app.modules.form129.application.command.create.CreateForm129Result;
import uz.uzinfocom.app.modules.form129.application.command.create.CreateForm129Service;
import uz.uzinfocom.app.platform.integrationclient.domain.IntegrationScope;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;

/**
 * Accepts Form129 from any registered source through one generic endpoint —
 * DMED included, with no separate hardcoded DMED contract (unlike
 * Form058/Form0581, which each also carry a DMED-specific fixed-shape
 * endpoint). See {@code InboundForm0581Controller} for the identical
 * caller-resolution pattern this mirrors.
 */
@Tag(
        name = "Integration - Form 129",
        description = "Приём формы №129 от внешних и внутренних систем — как через зарегистрированных "
                + "интеграционных клиентов, так и через SSO/DHP-аутентифицированных вызывающих."
)
@RestController
@RequestMapping(ApiPaths.Integration.FORM129)
@RequiredArgsConstructor
public class InboundForm129Controller {

    private final InboundForm129Mapper inboundForm129Mapper;
    private final InboundSourceResolver inboundSourceResolver;
    private final InboundIntegrationClientResolver inboundIntegrationClientResolver;
    private final CreateForm129Service createForm129Service;

    @Operation(
            summary = "Создать форму №129",
            description = "Создаёт форму №129 от имени организации, определяемой по вызывающему: для "
                    + "интеграционного клиента — организация, закреплённая за ним при регистрации; для "
                    + "SSO/DHP-вызывающего — его текущая выбранная организация. Организация-отправитель не "
                    + "может быть передана в теле запроса."
    )
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public InboundFormSubmissionResponse create(
            @Parameter(description = "Название системы-отправителя.", required = true)
            @PathVariable String source,
            @Valid @RequestBody InboundCreateForm129Request request
    ) {
        InboundCallerContext.requireScope(IntegrationScope.FORM129_SUBMIT);
        InboundCallerContext.requireMatchingSourceKey(source);

        String resolvedSource = inboundSourceResolver.resolve(source);
        Long senderOrganizationId = InboundCallerContext.resolveSenderOrganizationId();
        Long sourceIntegrationClientId = inboundIntegrationClientResolver.resolveSourceIntegrationClientId();
        CreateForm129Command command = inboundForm129Mapper.toCommand(
                request, resolvedSource, senderOrganizationId, sourceIntegrationClientId);
        CreateForm129Result result = createForm129Service.create(command);

        return new InboundFormSubmissionResponse(result.id(), result.uuid(), result.status().name());
    }
}
