package uz.uzinfocom.app.integration.inbound.dmed.form058.web;

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
import uz.uzinfocom.app.integration.inbound.common.web.InboundFinalDiagnosisResolver;
import uz.uzinfocom.app.integration.inbound.common.web.InboundFormIdentifier;
import uz.uzinfocom.app.integration.inbound.common.web.InboundIntegrationClientResolver;
import uz.uzinfocom.app.integration.inbound.common.web.InboundSenderResolver;
import uz.uzinfocom.app.integration.inbound.dmed.common.web.DmedConfirmFormRequest;
import uz.uzinfocom.app.integration.inbound.common.web.dto.InboundFormSubmissionResponse;
import uz.uzinfocom.app.integration.inbound.dmed.form058.application.DmedForm058Mapper;
import uz.uzinfocom.app.integration.inbound.dmed.form058.application.DmedForm058Validator;
import uz.uzinfocom.app.modules.form058.application.command.approve.ApproveForm058Service;
import uz.uzinfocom.app.modules.form058.application.command.update.UpdateForm058Result;
import uz.uzinfocom.app.modules.form058.application.command.create.CreateForm058Command;
import uz.uzinfocom.app.modules.form058.application.command.create.CreateForm058Result;
import uz.uzinfocom.app.modules.form058.application.command.create.CreateForm058Service;
import uz.uzinfocom.app.platform.integrationclient.domain.IntegrationScope;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;

/**
 * DMED's dedicated form058 submission endpoint — a fixed, flat request shape
 * (as opposed to the generic {@code /integration/v1/{source}/form-058}
 * endpoint's entity-mirroring nested structure), kept separate because DMED
 * already integrates against this exact contract. Everything else about the
 * inbound-integration surface (multi-token-type auth, X-Organization-Id
 * requirement, scope gating, source-key matching) applies identically here -
 * only the request DTO and its mapper differ.
 */
@Tag(
        name = "Integration - DMED Form 058",
        description = "Приём формы №058 от DMED — фиксированный (плоский) формат запроса."
)
@RestController
@RequestMapping(ApiPaths.Integration.DMED_FORM058)
@RequiredArgsConstructor
public class DmedForm058Controller {

    private static final String SOURCE = "DMED";

    private final DmedForm058Mapper dmedForm058Mapper;
    private final DmedForm058Validator dmedForm058Validator;
    private final InboundIntegrationClientResolver inboundIntegrationClientResolver;
    private final CreateForm058Service createForm058Service;
    private final ApproveForm058Service approveForm058Service;
    private final InboundFinalDiagnosisResolver inboundFinalDiagnosisResolver;
    private final InboundSenderResolver inboundSenderResolver;

    @Operation(
            summary = "Создать форму №058 (DMED)",
            description = "Создаёт форму №058 от имени организации, определяемой по вызывающему: для "
                    + "интеграционного клиента — организация, закреплённая за ним при регистрации; для "
                    + "SSO/DHP-вызывающего — его текущая выбранная организация. Организация-отправитель "
                    + "не может быть передана в теле запроса."
    )
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public InboundFormSubmissionResponse create(@Valid @RequestBody DmedCreateForm058Request request) {
        InboundCallerContext.requireScope(IntegrationScope.FORM058_SUBMIT);
        InboundCallerContext.requireMatchingSourceKey(SOURCE);
        dmedForm058Validator.validate(request);

        Long senderOrganizationId = InboundCallerContext.resolveSenderOrganizationId();
        Long sourceIntegrationClientId = inboundIntegrationClientResolver.resolveSourceIntegrationClientId();
        CreateForm058Command command = dmedForm058Mapper.toCommand(
                request, SOURCE.toUpperCase(), senderOrganizationId, sourceIntegrationClientId);
        CreateForm058Result result = createForm058Service.create(command);

        return new InboundFormSubmissionResponse(result.id(), result.uuid(), result.status().name());
    }

    @Operation(
            summary = "Подтвердить форму №058 (DMED)",
            description = "Подтверждает форму №058 окончательным диагнозом (МКБ-10): статус становится "
                    + "APPROVED. Форма указывается по id или uuid; подтвердить может только организация-"
                    + "отправитель формы (для интеграционного клиента — organizationUuid и submittedBy в теле; "
                    + "для SSO/DHP-вызывающего — его выбранная организация), после принятия формы получателем."
    )
    @PostMapping("/{idOrUuid}/confirm")
    @PreAuthorize("isAuthenticated()")
    public InboundFormSubmissionResponse confirm(
            @Parameter(description = "Идентификатор (id) или UUID формы.", required = true)
            @PathVariable String idOrUuid,
            @Valid @RequestBody DmedConfirmFormRequest request
    ) {
        InboundCallerContext.requireScope(IntegrationScope.FORM058_CONFIRM);
        InboundCallerContext.requireMatchingSourceKey(SOURCE);
        inboundSenderResolver.resolveSenderOrganizationId(request.organizationUuid(), request.submittedBy());

        InboundFormIdentifier identifier = InboundFormIdentifier.parse(idOrUuid);
        InboundFinalDiagnosisResolver.ResolvedDiagnosis diagnosis =
                inboundFinalDiagnosisResolver.resolve(request.finalIcd10Code(), request.finalIcd10Name());

        UpdateForm058Result result = approveForm058Service.confirm(
                identifier.id(), identifier.uuid(), diagnosis.code(), diagnosis.name());
        return new InboundFormSubmissionResponse(result.id(), result.uuid(), result.status().name());
    }
}
