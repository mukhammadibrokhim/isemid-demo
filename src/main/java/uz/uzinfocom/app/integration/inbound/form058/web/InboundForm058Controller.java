package uz.uzinfocom.app.integration.inbound.form058.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.app.integration.inbound.common.audit.InboundFormType;
import uz.uzinfocom.app.integration.inbound.common.audit.InboundSubmissionAuditService;
import uz.uzinfocom.app.integration.inbound.common.web.InboundCallerContext;
import uz.uzinfocom.app.integration.inbound.common.web.InboundConfirmFormRequest;
import uz.uzinfocom.app.integration.inbound.common.web.InboundFinalDiagnosisResolver;
import uz.uzinfocom.app.integration.inbound.common.web.InboundFormIdentifier;
import uz.uzinfocom.app.integration.inbound.common.web.InboundIntegrationClientResolver;
import uz.uzinfocom.app.integration.inbound.common.web.InboundSenderResolver;
import uz.uzinfocom.app.integration.inbound.common.web.InboundSourceResolver;
import uz.uzinfocom.app.integration.inbound.common.web.dto.InboundFormSubmissionResponse;
import uz.uzinfocom.app.integration.inbound.form058.application.InboundForm058Mapper;
import uz.uzinfocom.app.integration.inbound.form058.application.InboundForm058Validator;
import uz.uzinfocom.app.modules.form058.application.command.approve.ApproveForm058Service;
import uz.uzinfocom.app.modules.form058.application.command.update.UpdateForm058Result;
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
    private final InboundSenderResolver inboundSenderResolver;
    private final InboundSubmissionAuditService inboundSubmissionAuditService;
    private final InboundIntegrationClientResolver inboundIntegrationClientResolver;
    private final CreateForm058Service createForm058Service;
    private final ApproveForm058Service approveForm058Service;
    private final InboundFinalDiagnosisResolver inboundFinalDiagnosisResolver;

    @Operation(
            summary = "Создать форму №058",
            description = "Создаёт форму №058 от имени организации, определяемой по вызывающему: для "
                    + "интеграционного клиента — организация, закреплённая за ним при регистрации; для "
                    + "SSO/DHP-вызывающего — его текущая выбранная организация (как и в обычном "
                    + "фронтенд-потоке). Для клиента EXTERNAL_TOKEN (SSO/DHP M2M-токен) организация-отправитель "
                    + "указывается в теле (senderOrganizationUuid) и должна входить в его список разрешённых организаций."
    )
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public InboundFormSubmissionResponse create(
            @Parameter(description = "Название системы-отправителя.", required = true)
            @PathVariable String source,
            @Valid @RequestBody InboundCreateForm058Request request,
            HttpServletRequest httpRequest
    ) {
        InboundCallerContext.requireScope(IntegrationScope.FORM058_SUBMIT);
        InboundCallerContext.requireMatchingSourceKey(source);
        inboundForm058Validator.validate(request);

        String resolvedSource = inboundSourceResolver.resolve(source);
        Long senderOrganizationId = inboundSenderResolver.resolveSenderOrganizationId(
                request.senderOrganizationUuid(), request.submittedBy());
        Long sourceIntegrationClientId = inboundIntegrationClientResolver.resolveSourceIntegrationClientId();
        CreateForm058Command command = inboundForm058Mapper.toCommand(
                request, resolvedSource, senderOrganizationId, sourceIntegrationClientId);
        CreateForm058Result result = createForm058Service.create(command);

        inboundSubmissionAuditService.record(
                InboundFormType.FORM058, result.id(), senderOrganizationId, request.submittedBy(), httpRequest.getRemoteAddr());
        return new InboundFormSubmissionResponse(result.id(), result.uuid(), result.status().name());
    }

    @Operation(
            summary = "Подтвердить форму №058",
            description = "Подтверждает ранее отправленную форму №058 окончательным диагнозом (МКБ-10): статус "
                    + "формы становится APPROVED. Форма указывается по id или uuid. Подтвердить может только "
                    + "организация-отправитель формы, и только после принятия формы получателем. Повторная "
                    + "отправка того же диагноза для уже подтверждённой формы возвращает текущее состояние."
    )
    @PostMapping("/{idOrUuid}/confirm")
    @PreAuthorize("isAuthenticated()")
    public InboundFormSubmissionResponse confirm(
            @Parameter(description = "Название системы-отправителя.", required = true)
            @PathVariable String source,
            @Parameter(description = "Идентификатор (id) или UUID формы.", required = true)
            @PathVariable String idOrUuid,
            @Valid @RequestBody InboundConfirmFormRequest request
    ) {
        InboundCallerContext.requireScope(IntegrationScope.FORM058_CONFIRM);
        InboundCallerContext.requireMatchingSourceKey(source);

        InboundFormIdentifier identifier = InboundFormIdentifier.parse(idOrUuid);
        InboundFinalDiagnosisResolver.ResolvedDiagnosis diagnosis =
                inboundFinalDiagnosisResolver.resolve(request.finalIcd10Code(), request.finalIcd10Name());
        inboundSenderResolver.resolveSenderOrganizationId(request.organizationUuid(), request.submittedBy());

        UpdateForm058Result result = approveForm058Service.confirm(
                identifier.id(), identifier.uuid(), diagnosis.code(), diagnosis.name());
        return new InboundFormSubmissionResponse(result.id(), result.uuid(), result.status().name());
    }
}
