package uz.uzinfocom.app.integration.inbound.form0581.web;

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
import uz.uzinfocom.app.integration.inbound.common.web.InboundSourceResolver;
import uz.uzinfocom.app.integration.inbound.common.web.dto.InboundFormSubmissionResponse;
import uz.uzinfocom.app.integration.inbound.form0581.application.InboundForm0581Mapper;
import uz.uzinfocom.app.integration.inbound.form0581.application.InboundForm0581Validator;
import uz.uzinfocom.app.modules.form0581.application.command.create.CreateForm0581Command;
import uz.uzinfocom.app.modules.form0581.application.command.create.CreateForm0581Result;
import uz.uzinfocom.app.modules.form0581.application.command.create.CreateForm0581Service;
import uz.uzinfocom.app.platform.integrationclient.domain.IntegrationScope;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;

@Tag(
        name = "Integration - Form 058-1",
        description = "Приём формы №058-1 от внешних и внутренних систем — как через зарегистрированных "
                + "интеграционных клиентов, так и через SSO/DHP-аутентифицированных вызывающих."
)
@RestController
@RequestMapping(ApiPaths.Integration.FORM0581)
@RequiredArgsConstructor
public class InboundForm0581Controller {

    private final InboundForm0581Mapper inboundForm0581Mapper;
    private final InboundForm0581Validator inboundForm0581Validator;
    private final InboundSourceResolver inboundSourceResolver;
    private final CreateForm0581Service createForm0581Service;

    @Operation(
            summary = "Создать форму №058-1",
            description = "Создаёт форму №058-1 от имени организации, определяемой по вызывающему: для "
                    + "интеграционного клиента — организация, закреплённая за ним при регистрации; для "
                    + "SSO/DHP-вызывающего — его текущая выбранная организация (как и в обычном "
                    + "фронтенд-потоке). Организация-отправитель не может быть передана в теле запроса."
    )
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public InboundFormSubmissionResponse create(
            @Parameter(description = "Название системы-отправителя.", required = true)
            @PathVariable String source,
            @Valid @RequestBody InboundCreateForm0581Request request
    ) {
        InboundCallerContext.requireScopeIfIntegrationClient(IntegrationScope.FORM0581_SUBMIT);
        InboundCallerContext.requireMatchingSourceKey(source);
        inboundForm0581Validator.validate(request);

        String resolvedSource = inboundSourceResolver.resolve(source);
        Long senderOrganizationId = InboundCallerContext.resolveSenderOrganizationId();
        CreateForm0581Command command = inboundForm0581Mapper.toCommand(request, resolvedSource, senderOrganizationId);
        CreateForm0581Result result = createForm0581Service.create(command);

        return new InboundFormSubmissionResponse(result.id(), result.uuid(), result.status().name());
    }
}
