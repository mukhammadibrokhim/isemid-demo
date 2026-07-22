package uz.uzinfocom.app.integration.inbound.dmed.form058.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.app.integration.inbound.common.web.InboundCallerContext;
import uz.uzinfocom.app.integration.inbound.common.web.dto.InboundFormSubmissionResponse;
import uz.uzinfocom.app.integration.inbound.dmed.form058.application.DmedForm058Mapper;
import uz.uzinfocom.app.integration.inbound.dmed.form058.application.DmedForm058Validator;
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
    private final CreateForm058Service createForm058Service;

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
        InboundCallerContext.requireScopeIfIntegrationClient(IntegrationScope.FORM058_SUBMIT);
        InboundCallerContext.requireMatchingSourceKey(SOURCE);
        dmedForm058Validator.validate(request);

        Long senderOrganizationId = InboundCallerContext.resolveSenderOrganizationId();
        CreateForm058Command command = dmedForm058Mapper.toCommand(request, SOURCE.toUpperCase(), senderOrganizationId);
        CreateForm058Result result = createForm058Service.create(command);

        return new InboundFormSubmissionResponse(result.id(), result.uuid(), result.status().name());
    }
}
