package uz.uzinfocom.app.integration.inbound.dmed.form0581.web;

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
import uz.uzinfocom.app.integration.inbound.dmed.common.web.DmedConfirmFormRequest;
import uz.uzinfocom.app.integration.inbound.common.web.dto.InboundFormSubmissionResponse;
import uz.uzinfocom.app.integration.inbound.dmed.form0581.application.DmedForm0581Mapper;
import uz.uzinfocom.app.integration.inbound.dmed.form0581.application.DmedForm0581Validator;
import uz.uzinfocom.app.modules.form0581.application.command.approve.ApproveForm0581Service;
import uz.uzinfocom.app.modules.form0581.application.command.update.UpdateForm0581Result;
import uz.uzinfocom.app.modules.form0581.application.command.create.CreateForm0581Command;
import uz.uzinfocom.app.modules.form0581.application.command.create.CreateForm0581Result;
import uz.uzinfocom.app.modules.form0581.application.command.create.CreateForm0581Service;
import uz.uzinfocom.app.platform.integrationclient.domain.IntegrationScope;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;

/**
 * DMED's dedicated form058-1 submission endpoint — a fixed, flat request
 * shape (as opposed to the generic {@code /integration/v1/{source}/form-058-1}
 * endpoint's entity-mirroring nested structure), kept separate because DMED
 * already integrates against this exact contract. Everything else about the
 * inbound-integration surface (multi-token-type auth, X-Organization-Id
 * requirement, scope gating, source-key matching) applies identically here -
 * only the request DTO and its mapper differ.
 */
@Tag(
        name = "Integration - DMED Form 058-1",
        description = "Приём формы №058-1 от DMED — фиксированный (плоский) формат запроса."
)
@RestController
@RequestMapping(ApiPaths.Integration.DMED_FORM0581)
@RequiredArgsConstructor
public class DmedForm0581Controller {

    private static final String SOURCE = "DMED";

    private final DmedForm0581Mapper dmedForm0581Mapper;
    private final DmedForm0581Validator dmedForm0581Validator;
    private final InboundIntegrationClientResolver inboundIntegrationClientResolver;
    private final CreateForm0581Service createForm0581Service;
    private final ApproveForm0581Service approveForm0581Service;
    private final InboundFinalDiagnosisResolver inboundFinalDiagnosisResolver;

    @Operation(
            summary = "Создать форму №058-1 (DMED)",
            description = "Создаёт форму №058-1 от имени организации, определяемой по вызывающему: для "
                    + "интеграционного клиента — организация, закреплённая за ним при регистрации; для "
                    + "SSO/DHP-вызывающего — его текущая выбранная организация. Организация-отправитель "
                    + "не может быть передана в теле запроса."
    )
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public InboundFormSubmissionResponse create(@Valid @RequestBody DmedCreateForm0581Request request) {
        InboundCallerContext.requireScope(IntegrationScope.FORM0581_SUBMIT);
        InboundCallerContext.requireMatchingSourceKey(SOURCE);
        dmedForm0581Validator.validate(request);

        Long senderOrganizationId = InboundCallerContext.resolveSenderOrganizationId();
        Long sourceIntegrationClientId = inboundIntegrationClientResolver.resolveSourceIntegrationClientId();
        CreateForm0581Command command = dmedForm0581Mapper.toCommand(
                request, SOURCE.toUpperCase(), senderOrganizationId, sourceIntegrationClientId);
        CreateForm0581Result result = createForm0581Service.create(command);

        return new InboundFormSubmissionResponse(result.id(), result.uuid(), result.status().name());
    }

    @Operation(
            summary = "Подтвердить форму №058-1 (DMED)",
            description = "Подтверждает форму №058-1 окончательным диагнозом (МКБ-10): статус становится "
                    + "APPROVED. Форма указывается по id или uuid; подтвердить может только организация-"
                    + "отправитель формы (определяется по вызывающему), после принятия формы получателем."
    )
    @PostMapping("/{idOrUuid}/confirm")
    @PreAuthorize("isAuthenticated()")
    public InboundFormSubmissionResponse confirm(
            @Parameter(description = "Идентификатор (id) или UUID формы.", required = true)
            @PathVariable String idOrUuid,
            @Valid @RequestBody DmedConfirmFormRequest request
    ) {
        InboundCallerContext.requireScope(IntegrationScope.FORM0581_CONFIRM);
        InboundCallerContext.requireMatchingSourceKey(SOURCE);
        InboundCallerContext.resolveSenderOrganizationId();

        InboundFormIdentifier identifier = InboundFormIdentifier.parse(idOrUuid);
        InboundFinalDiagnosisResolver.ResolvedDiagnosis diagnosis =
                inboundFinalDiagnosisResolver.resolve(request.finalIcd10Code(), request.finalIcd10Name());

        UpdateForm0581Result result = approveForm0581Service.confirm(
                identifier.id(), identifier.uuid(), diagnosis.code(), diagnosis.name());
        return new InboundFormSubmissionResponse(result.id(), result.uuid(), result.status().name());
    }
}
