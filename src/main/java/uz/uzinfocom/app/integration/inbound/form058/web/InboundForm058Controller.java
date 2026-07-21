package uz.uzinfocom.app.integration.inbound.form058.web;

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
import uz.uzinfocom.app.integration.inbound.form058.application.InboundForm058Mapper;
import uz.uzinfocom.app.integration.inbound.form058.application.InboundForm058Validator;
import uz.uzinfocom.app.modules.form058.application.command.create.CreateForm058Command;
import uz.uzinfocom.app.modules.form058.application.command.create.CreateForm058Result;
import uz.uzinfocom.app.modules.form058.application.command.create.CreateForm058Service;
import uz.uzinfocom.app.platform.integrationclient.domain.IntegrationScope;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;

@Tag(
        name = "Integration - Form 058",
        description = "Приём формы №058 от внешних и внутренних систем — как через зарегистрированных "
                + "интеграционных клиентов, так и через SSO/DHP-аутентифицированных вызывающих."
)
@RestController
@RequestMapping(ApiPaths.Integration.FORM058)
@RequiredArgsConstructor
public class InboundForm058Controller {

    private final InboundForm058Mapper inboundForm058Mapper;
    private final InboundForm058Validator inboundForm058Validator;
    private final InboundSourceResolver inboundSourceResolver;
    private final CreateForm058Service createForm058Service;

    @Operation(
            summary = "Создать форму №058",
            description = "Создаёт форму №058 от имени организации, определяемой по вызывающему: для "
                    + "интеграционного клиента — организация, закреплённая за ним при регистрации; для "
                    + "SSO/DHP-вызывающего — его текущая выбранная организация (как и в обычном "
                    + "фронтенд-потоке). Организация-отправитель не может быть передана в теле запроса."
    )
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public InboundFormSubmissionResponse create(
            @Parameter(description = "Название системы-отправителя.", required = true)
            @PathVariable String source,
            @Valid @RequestBody InboundCreateForm058Request request
    ) {
        InboundCallerContext.requireScopeIfIntegrationClient(IntegrationScope.FORM058_SUBMIT);
        InboundCallerContext.requireMatchingSourceKey(source);
        inboundForm058Validator.validate(request);

        String resolvedSource = inboundSourceResolver.resolve(source);
        Long senderOrganizationId = InboundCallerContext.resolveSenderOrganizationId();
        CreateForm058Command command = inboundForm058Mapper.toCommand(request, resolvedSource, senderOrganizationId);
        CreateForm058Result result = createForm058Service.create(command);

        return new InboundFormSubmissionResponse(result.id(), result.uuid(), result.status().name());
    }
}
